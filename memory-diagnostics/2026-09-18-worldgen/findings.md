# BeanPack world-generation lag — September 18, 2026

Captured the live game, PID 6852, using a finite Java Flight Recorder profile from 21:35:42 to 21:36:42 EDT. Recording has stopped. No mods or game settings were changed.

## Main finding

The sampled server is heavily occupied with chunk generation and population. There is also substantial background recipe calculation and temporary-object allocation. This minute does not show heap allocation starvation or long stop-the-world garbage collection pauses.

Server execution samples: 3,759. Inclusive stack shares below overlap: parent generators include child work and sometimes generation triggered by block access. These are sampled execution shares, not percentages of total wall time, isolated mod costs, or predicted speedups from disabling a mod.

| Sampled path | Server samples |
| --- | ---: |
| ChunkProviderServer chunk provision | 68.16% |
| Forge generateWorld | 47.81% |
| Depths Update generation wrapper | 31.58% |
| Depths Update lush caves | 15.67% |
| TAIGA patched common overworld ore generation | 15.03% |
| Depths Update dripstone caves | 13.78% |
| Better Caves map generation | 12.58% |
| Mekanism QIO recipe catalog capture | 12.32% |
| Depths Update cave noise generation | 10.77% |

Generation also appears below mob-spawn block checks. Inspect block access and neighbor notifications before assuming every generator cost is its own direct computation. Galacticraft's otherModGenerate frame is a dispatch wrapper and does not establish Galacticraft as the expensive generator.

## Memory

- Hard Java heap limit: 12,192 MiB (11.906 GiB).
- Initial heap snapshot: 11,048 MiB used; later snapshot: 11,228 MiB.
- After-collection heap occupancy during recording: 10.230–10.852 GiB, mean 10.503 GiB.
- 92 completed collections, triggered by allocation rate (64) and high usage (28).
- Zero ZAllocationStall events. Maximum GC pause 0.080 ms; all pause phases summed to 4.167 ms.
- Approximately 76.99 GiB of sampled cumulative allocations in 60 seconds, about 1.28 GiB/second. This is temporary allocation throughput, NOT 77 GiB held in RAM. First allocation sample from each thread was excluded to reduce startup-attribution bias.
- Windows reported approximately 30.1 GiB physical RAM available at initial inspection. The game process working set was approximately 15.2 GiB; this is a different metric from Java heap use.

Overlapping allocation-stack signatures:

| Path present in allocation stack | Estimated allocation share |
| --- | ---: |
| Matter Overdrive RegisterItemsFromRecipes | 33.48% |
| Depths Update | 22.85% |
| Retro Sophisticated Backpacks | 21.95% |
| Better Caves | 12.60% |
| JourneyMap | 9.71% |
| Chunk render meshing | 5.47% |
| QIO processing | 3.62% |
| AE2 CompassService | 2.18% |

The large Matter Overdrive stacks traverse CraftTweaker recipe outputs, ItemStack copying, and backpack capability serialization/deserialization. Much of the backpack cost is nested under this recipe scan, not evidence that placed backpacks independently cause the lag. Its sampled total is approximately 25.8 GiB of cumulative allocation over this minute. Duration beyond this capture and whether the scan eventually settles are not established.

The profile does not identify ownership of the approximately 10.5 GiB surviving collections or establish a memory leak. That would require a separate retained-heap investigation, with potentially disruptive heap capture. An older September 13 recording had allocation stalls, but that result should not be substituted for this fresh recording.

## Next targeted work

1. Inspect Depths Update cave decoration and TAIGA ore placement for unnecessary neighbor updates or generation of adjacent chunks. Compare any patch using the same seed, route, settings, and fresh chunks in a separate test world.
2. Address Matter Overdrive's background recipe scan and its repeated copying of capability-heavy items. The installed MatterOverdrive.cfg has automatic_calculation and automatic matter calculation from recipe enabled. Disabling these changes automatic matter values, so it is a diagnostic experiment with gameplay consequences, not a free optimization.
3. Reprofile after background Matter Overdrive and QIO catalog work finishes to separate ongoing worldgen from initialization overhead.
4. Generating the intended exploration area ahead of play can move terrain-generation work out of exploration; it will not itself fix retained memory use or background recipe work.

Do not expect simply adding RAM to remove measured generator CPU cost. ZGC does require headroom for concurrent allocation, but this capture shows it kept up. Reference: https://docs.oracle.com/en/java/javase/17/gctuning/z-garbage-collector1.html

Evidence: worldgen.jfr, server-summary.txt, stalls-allocations.txt, WorldgenSummary.java. The existing AnalyzeStutterJfr.java in diagnostics/stutter-cdpI61PWTi was reused for GC and allocation analysis. Render distance was 8; the mods folder contained 418 JARs.
