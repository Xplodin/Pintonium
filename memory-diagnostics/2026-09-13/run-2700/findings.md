# Why the new launch still passes 16 GiB

Follow-up: the user subsequently tested disabling VintageFix resource-pack mixins. The setting took effect but also disabled its fast asset-path helper, exposing significant vanilla formatting churn. See [the next comparison](../run-13572/findings.md) before treating this configuration change as a proven RAM improvement.

This run directly reproduced heap occupancy above 16 GiB, followed by normal garbage collections reducing occupancy to about 4.9 GiB. The current configuration permits a 20.094 GiB heap. Sustained startup allocations plus ZGC's heap-size policy explain the measured spikes; the evidence does not show that this stage requires 16 GiB of persistent objects.

## Current configuration and measurements

Process 2700 started at 14:34:48 on September 13, 2026. `ParallelModelLoader=false` is now applied. VintageFix dynamic resources remain enabled. The large StellarCore state-mapper allocation category seen in the earlier recording is absent from this recording; this does not quantify total savings because capture intervals differ.

| Measurement | Value |
| --- | --- |
| Effective maximum heap | 20,576 MiB / 20.094 GiB |
| ZGC soft maximum | Same as maximum heap |
| Committed heap at the first inspection | 18,390 MiB / 17.96 GiB |
| Used heap at that inspection | 9,354 MiB / 9.13 GiB |
| Live-object histogram at approximately 14:39:31 | 4,196,461,128 bytes / 3.91 GiB |
| Number of objects in that histogram | 52,639,620 |
| Normal collection at 14:41:35 | 18.4 GiB before → 4.9 GiB after |
| Normal collection at 14:41:51 | 19.0 GiB before → 4.9 GiB after |
| Whole-process peak working set through report preparation | About 22.23 GiB |

The histogram requests a collection and counts shallow object sizes. The complete command took about 3.8 seconds; that wall time is not all a stop-the-world pause. Subsequent *normal* collections independently showed the same small post-GC occupancy. Heap occupancy includes allocation since the last collection and differs from the sum of live object sizes.

ZGC reuses the committed space between collections. Its verified `ZUncommitDelay=300` means a region must be unused for five minutes before it is eligible to be returned to Windows; repeatedly reused regions remain committed. The current `SoftMaxHeapSize` equals the 20.1 GiB hard cap. Removing an allocation hotspot does not automatically lower that budget. [Oracle ZGC documentation](https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector2.html)

## Remaining work generating the spikes

The completed three-minute recording includes approximately 50,000 allocation samples after conservatively excluding the first sample from each thread. Its sampled allocation-pressure categories were:

- 23.97%: StellarCore resource discovery/binding, including delegated resource-pack work.
- 15.61%: other VintageFix model/texture/resource paths.
- 11.44%: CraftTweaker `replaceAllOccurences` processing.
- 48.98%: other paths.

These percentages describe temporary allocations during this interval, not ownership of retained RAM. They cannot be directly compared with the first run's percentages because the recordings cover different portions of startup and this run includes a diagnostic collection.

**A specific additional reload comes from `Beyonder-1.2.jar`.** Its package name is `com.matoon.herosmp`. A live client-thread stack shows `ClientProxy.init` calling Minecraft's full resource refresh, then texture/model rebuilding and StellarCore resource discovery. Inspection of the installed jar confirms that `ClientProxy.init` calls `Minecraft.func_110436_a()` after spawn-egg color registration. Its `preInit` also installs `HungerGamesMusicResourcePack`, so a code change must preserve activation of this music resource pack rather than blindly deleting the reload.

The log records a 16,384 × 16,384 texture atlas at 14:38:30 and another atlas construction at 14:40:06. Item-baking passes finished in 23.05 and 17.73 seconds. The full reload repeats significant pack-wide work; it is not evidence that Beyonder alone retains all the memory. A 16,384² RGBA8 base texture would be 1 GiB, but actual GPU format, driver memory and mip levels have not been measured, so this is not an attribution of 1 GiB of system RAM.

The live histogram counted 1,184,695 baked quads, 108,083 vanilla texture-atlas sprites, 2,515,328 model vertices, and 597,636 item stacks. The largest shallow categories were byte arrays (about 1.15 GiB), int arrays (about 0.78 GiB), and object-reference arrays (about 0.35 GiB). Array ownership cannot be assigned to individual mods from a class histogram alone.

**The second resource-pack overlap remains configured.** StellarCore `ResourceExistStateCache=true` and VintageFix `mixin.resourcepacks=true` are both active. Repeated `CachedResourcePath` creation appears under StellarCore's `MutableResourcePackBindings.canDiscover` in this profile. StellarCore's compatibility guidance calls for VintageFix's resource-pack mixins to be disabled when retaining StellarCore's existence cache. The installed StellarCore contains custom resource integration, so verify any change with a restart and rendering check. [StellarCore compatibility guidance](https://github.com/NovaEngineering-Source/StellarCore#compatibility-note)

## Targeted next steps

1. Test `mixin.resourcepacks=false` in `config/vintagefix.properties`, retaining `mixin.dynamic_resources=true` and the now-disabled StellarCore parallel model loader. This is the remaining configuration comparison proposed in the first report.
2. Investigate registering Beyonder's music resource pack early enough for an existing resource refresh, or refreshing only what is required. Verify custom music, sounds, textures, and spawn-egg colors before adopting such a code change.
3. If the goal is a lower memory footprint, test `-XX:SoftMaxHeapSize=12g` with the current hard maximum retained. This is a soft target and can be exceeded. A strict *heap* ceiling of 16 GiB requires `-Xmx16384m`; whole-process RAM can still exceed that because of native overhead. Neither option has been tested here, and this capture does not establish later gameplay or maximum-world-loading requirements. Tune one variable at a time rather than using peak RAM as proof of a leak.

The investigation did not edit the pack configuration or mod jars. It did request one live-object histogram/diagnostic collection. The three-minute JFR completed and temporary GC logging was disabled. Evidence is in `loading.jfr`, `allocations.txt`, `live-histogram.txt`, `gc-summary.txt`, `gc.log`, `threads.txt`, and `herosmp-client-bytecode.txt` in this directory.
