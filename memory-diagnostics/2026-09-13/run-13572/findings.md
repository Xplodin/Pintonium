# VintageFix resource-pack setting comparison

September 13, 2026. Process 13572 started at 14:50:32. The user disabled `mixin.resourcepacks`; the startup log explicitly confirms that VintageFix skipped its resource-pack mixins. `mixin.dynamic_resources=true`, StellarCore `ParallelModelLoader=false`, and StellarCore `ResourceExistStateCache=true` remain in effect.

The change is active and removes the previously sampled VintageFix cache path. It is not yet a demonstrated reduction in total peak RAM. This run exposes a cost of disabling the entire feature group: vanilla resource-path formatting is restored.

## Profile evidence

The completed three-minute profile has 49,866 allocation samples after excluding the first sample per thread, using the same conservative filter as the previous runs.

| Stack signature | Previous run, resources enabled | This run, resources disabled |
| --- | ---: | ---: |
| `CachedResourcePath` | 4,453 samples | 0 samples |
| `fastHasResource` | 5,311 samples | 0 samples |
| `NonBlockingIdentityHashMap.initialize` | 0 samples | 0 samples |

These are counts of sampled stacks, not object counts or a controlled whole-startup allocation comparison. The capture intervals cover different startup stages.

The first non-JDK frame for **27.61% of sampled allocation weight** is now vanilla `AbstractResourcePack.func_110592_c`, the helper constructing asset paths with `String.format`. The stacks include `Formatter`, format-specifier objects, temporary arrays and string builders beneath StellarCore resource discovery. In total, 13,856 sampled stacks include `String.format` somewhere in the call chain.

Inspection of the installed VintageFix jar shows that its now-disabled `MixinAbstractResourcePack` replaces this formatting operation with direct string construction. Disabling `mixin.resourcepacks` therefore removes both its conflicting resource caches and its useful path-construction optimization. The mod describes this configuration group as improving resource-pack and mod-JAR access. [VintageFix configuration documentation](https://github.com/embeddedt/VintageFix/wiki/Config-options#mixinresourcepacks)

The installed VintageFix configuration plugin selects options by the mixin's package name. It does not provide an ordinary per-class enable override for this helper. An invented `mixin.resourcepacks.MixinAbstractResourcePack=true` setting would not be a verified way to retain only that optimization.

## Memory remains dominated by growth and reuse during loading

- Maximum heap remains 20,576 MiB / 20.094 GiB.
- At 14:54:05, Java reported 3,178 MiB used in 18,862 MiB committed: most committed heap was unused at that instant.
- Normal garbage collection at 14:55:58 reduced occupancy from about 18.0 GiB to 6.8 GiB.
- Several earlier normal collections left roughly 3–5 GiB occupied.
- The process peak through report preparation was about 21.78 GiB, including memory outside the heap.

Startup was still in progress. These measurements do not establish the final idle footprint or later peak, and the differing recording stages prevent attributing the difference from the previous process peak solely to this setting. No forced collection was requested in this run.

## Next useful optimization

The targeted code-level candidate is to retain a fast, semantically equivalent `assets/<namespace>/<path>` helper while leaving the incompatible VintageFix caching group disabled. That would address the newly measured formatting churn without restoring the original overlap. A patch should be verified against normal and unusual resource locations, and tested during texture/model loading before claiming savings.

Beyonder's extra full resource reload, documented in the previous run, remains another candidate. Separately, lower heap growth requires a lower GC target or heap limit; these settings still allow 20.1 GiB. Neither a formatting patch nor JVM tuning was applied during this comparison.

Evidence: `loading.jfr`, `allocations.txt`, `samples.csv`, `gc-summary.txt`, `gc.log`, the saved configuration files, and `mixin-config-plugin.txt`. The earlier recording was reanalyzed into `previous-run-reanalysis.txt` for the stack-presence comparison. Recording and sampling completed, and temporary GC logging was turned off.
