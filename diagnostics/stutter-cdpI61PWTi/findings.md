# Freezing and stuttering: findings outside QIO

The strongest immediate mechanism is heap allocation starvation. The clearest additional mod workload in the supplied profile is Mystcraft baseline generation during play. Entity and world ticking add substantial steady work. These findings do not establish which mod owns the retained heap; QIO may still contribute to that memory pressure.

## Evidence and limits

Input: `C:\Users\somet\Downloads\cdpI61PWTi.sparkprofile`, SHA-256 `A3D95CD844C423B018C2080B97EA8D7207304D17FB55FB94CC01FE3C9B5B812D`.

The Flare execution profile covers September 13, 2026, 20:33:31–20:34:16 EDT (45.703 seconds), at a nominal 4 ms interval. It sampled only **Server thread**, Java thread ID 498. Although the platform metadata says CLIENT because this is an integrated client/server game, the selected thread was the server. This profile alone cannot attribute client rendering or GPU stalls.

Decoded locally using the installed Flare 0.8.0 protobuf classes. The original file was neither changed nor uploaded. Tree reconstruction follows [Flare's exporter](https://github.com/CleanroomMC/Flare/blob/master/src/main/java/com/cleanroommc/flare/util/ProtoUtil.java) and the [Spark schema](https://github.com/lucko/spark-viewer/blob/master/proto/spark.proto).

The older timing windows are internally inconsistent: the full tree has 1,019 parent/child time inconsistencies. For example, MinecraftServer.run totals 103,740 ms beneath a Thread.runWith parent totaling 38,408 ms. Therefore full-recording percentages printed in `profile-analysis.txt` are explicitly marked unreliable and were not used for the ranking below. The exact cause of the inconsistent export was not established.

The final window, key 178934605 (approximately 20:34:10–20:34:16), has 4,976 ms of weighted server samples, matching its root and leaf totals, and **zero** parent/child inconsistencies. Percentages below refer only to this short, internally consistent segment. They are sampled-stack shares, not whole-recording CPU percentages or proof of freeze duration.

A separate finite 60-second JFR recording of the running game (PID 36148) covered 20:40:07–20:41:07. The log shows world saving and shutdown during this recording. It is useful evidence of memory stalls and exit-time work, but not a controlled recording of uninterrupted gameplay. The recording finished; no JFR recording remained active. No forced garbage collection, heap dump, mod changes, or configuration changes were performed.

## 1. Heap starvation directly stalls both main threads

The uploaded profile reports 12,756,975,616 bytes used out of 12,784,238,592 bytes committed. The running JVM's hard limit was 12,192 MiB. A live check shortly afterward showed **12,190 / 12,192 MiB used**.

The follow-up JFR recorded:

| Measurement | Result |
| --- | --- |
| ZGC allocation-stall events, all threads | 2,650 |
| Client-thread allocation stalls | 175 events; 41.208 seconds summed; longest 15.065 seconds |
| Server-thread allocation stalls | 188 events; 40.274 seconds summed; longest 15.065 seconds |
| Completed GC events | 240; 236 caused by allocation stalls |
| After-GC heap occupancy | Minimum 9.498 GiB, mean 11.840 GiB, maximum 11.906 GiB |
| Stop-the-world GC phase pauses | 755 events; 11.231 ms summed; maximum 0.295 ms |

The long freezes are allocation waits, not long stop-the-world collection pauses. Threads across rendering, saving, and mod updates wait for heap space simultaneously. Their stall totals overlap; do not add different threads' seconds together as wall-clock freeze duration. An allocation site caught waiting is not necessarily the owner of the memory filling the heap.

This workload does not demonstrate that a 10 GiB whole-process budget is viable. Lowering the heap further without reducing retained data or concurrent work is likely to make the stalls worse. Earlier small live sets during startup did not establish gameplay memory requirements. Extra heap could be used as a diagnostic if the user's memory budget permits, but it would not identify or fix a retained-memory problem.

## 2. Mystcraft baseline profiling during gameplay

The final consistent Spark window attributes **660 ms / 13.26%** of server samples to:

`InstabilityDataCalculator.onServerTick → stepChunkGeneration → WorldProviderMystDummy.generateNextChunk`

This is additional background terrain-generation work, outside QIO call stacks. All Mystcraft-containing stacks together account for 13.34% in that segment.

The installed `config/mystcraft/core.cfg` confirms:

```text
B:client.persave=true
I:tickrate.minimum=5
B:useconfigs=false
```

The configuration's own comment states that `client.persave=true` runs baseline profiling during play, while `false` runs it during game startup with the loading bar. A targeted first non-QIO test is to set `B:client.persave=false` and restart, accepting longer startup to move this work out of gameplay. This recommendation does not disable the instability system or substitute unverified balance data. The setting was not changed during this investigation.

## 3. Loaded entities and world/block ticking

Profile window statistics report approximately **1,339–1,367 entities**, **3,331–3,378 block entities**, and **353–374 chunks** loaded. Counts establish scale, not individual ownership of RAM or CPU cost.

In the last consistent segment:

- World entity-update subtree: about 16.16%; individual entity ticking accounts for about 15.84%.
- WorldServer's block/weather tick path (`func_147456_g`): about 8.52%.
- Smaller contributors include AoA mobs, Quark crabs, HBM living-entity effects, Galacticraft checks, and The One Probe requests. None individually establishes another QIO-sized offender in this short segment.
- Curvy Pipes is about 0.8% here, so this sample does not support blaming it as the primary source of the freezes.

Useful tests are reducing unnecessary forced-loaded areas and excess entities while keeping the memory limit fixed, then repeating a gameplay capture. Do not infer that all 3,378 block entities are ticking or that every loaded chunk is forced-loaded.

## 4. Secondary findings, with scope limits

**Repeated Yoyos/Tinkers model failures:** The client log repeatedly reports `Could not load multimodel yoyos:tools/yoyo.tcon#inventory`; 68 matching errors occur from 20:33 through 20:39. This is a client-side retry/error path worth checking if frame-time spikes remain, but the supplied server-only profile does not quantify its render cost or prove that each error causes a freeze. A useful controlled test would remove that item's rendering from the visible inventory/overlay scene rather than disabling content blindly.

**Ender IO recipe rebuild on world exit:** The follow-up JFR shows significant allocations in `AlloyRecipeManager.rebuild` and `TriItemLookup.addRecipe`. The complete call stack goes through `GameData.revertToFrozen → FMLClientHandler.serverStopped`, so this is confirmed exit-time work. The method named `EnderIO.onServerStarting` also appears in that event path; its name alone must not be interpreted as a world-start event or a continuous gameplay tick. This can add delay when leaving a world, but is not established as the cause of ordinary movement stutters.

**Capability work beneath QIO:** Baubles, capability attachment, and item-copy frames are often nested under QIO recipe processing in the supplied profile. They should not be counted as independent non-QIO causes merely because another mod's name appears in the stack.

## Recommended order

1. Move Mystcraft baseline profiling to startup and repeat a gameplay test.
2. Reduce unnecessary loaded entities/areas and identify what keeps the heap occupied. Do not lower the allocation further while it is saturating.
3. Capture client and server together during normal play, excluding world exit, to measure any remaining rendering/model spikes and confirm which server costs persist after QIO work settles.

Supporting files: `last-window-analysis.txt`, `last-window/stacks.csv`, `windows.txt`, `live-jfr-analysis.txt`, `live-stutter.jfr`, `live-threads.txt`, and the two Java analyzers. Full-window Spark percentages are retained only as a diagnostic of the inconsistent input timing totals.
