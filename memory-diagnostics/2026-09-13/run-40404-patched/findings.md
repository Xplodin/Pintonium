# First live check of the resource-path patch

September 13, 2026. PID 40404, started 15:26:26. Loading was still underway at the final snapshot, 15:35:18.

The startup log confirms the coremod loaded and transformed the asset-path formatter at 15:26:51. The running JVM confirms SoftMaxHeapSize=12 GiB and ZUncommitDelay=30. Its hard heap limit remains 20,576 MiB.

A finite 60-second JFR recording from 15:33:06 to 15:34:06 captured 12,363 allocation samples after excluding the first sample per thread. Resource lookups were active: 588 sampled allocation stacks included AbstractResourcePack.func_110592_c. None of those stacks included String.format; 559 included StringBuilder/AbstractStringBuilder. The earlier unpatched recording had 13,681 resource-path samples, all including String.format. This confirms removal of the intended formatter path. Different stages and recording durations mean these raw counts are not a performance ratio.

The resource-path helper still constructs the required result string and accounts for 7.39% of sampled allocation weight in the new interval, compared with 27.61% in the earlier interval. Different workloads prevent interpreting this as an exact reduction. Current sampled work also includes substantial Mekanism recipe/NBT comparisons and texture decoding.

Latest memory at 15:35:18:

- Heap used: 9,860 MiB / 9.63 GiB.
- Heap committed: 14,146 MiB / 13.81 GiB.
- Whole-process working set: 16.87 GiB.
- Whole-process peak so far: 16.94 GiB, versus roughly 21–22 GiB during earlier launches.

The combined changes show a promising partial-run result, and the specific code optimization is active. A completed-startup comparison and isolated A/B run have not been performed. The heap soft target can be exceeded, and the current process memory is still above 16 GiB. No forced garbage collection was used for these measurements. The recording finished and no JFR recording remains active.

Evidence: patch-check.jfr, allocations.txt, path-comparison.txt, memory-before.json, memory-after.json, memory-latest.json, threads.txt, and the current game log. The source of CheckResourcePathSamples.java is saved alongside the recordings.
