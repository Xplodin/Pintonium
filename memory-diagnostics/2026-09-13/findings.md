# BeanPack Invincible RAM investigation — September 13, 2026

The observed startup spike is dominated by temporary allocation and an expanded Java heap. Resource/model loading is a substantial measured contributor, with additional recipe-processing churn. This capture did not show the Java heap exceeding its configured maximum or establish an ongoing memory leak.

## Measurements

Live process 36764, Cleanroom 0.6.13-alpha, GraalVM Community Java 25.0.4, ZGC. The pack contains 434 top-level mod jars (not the count of Forge mod IDs).

| Measurement | Observed value |
| --- | --- |
| Configured and effective maximum heap | 20,576 MiB = 20.094 GiB |
| Initial heap | 256 MiB |
| High heap use during capture | About 19 GiB |
| Heap immediately after several late collections | About 8.6–9.3 GiB |
| Whole-process peak working set observed through report preparation | About 22.46 GiB |
| Physical RAM still available during late loading | About 24–25 GiB |

Example: at 14:28:15, garbage collection reduced heap occupancy from approximately 19.0 GiB to 8.6 GiB. This repeated during late loading. Committed heap capacity stayed near 19 GiB even when much of it was unused.

The launcher's 20,576 MB setting is respected. `-Xmx` limits the object heap; process memory also includes class metadata, compiled code, thread stacks, native libraries and rendering buffers. Native-memory tracking was not enabled, so the non-heap portion has not been attributed precisely. The loading-screen code in the installed Cleanroom jar computes heap usage from `Runtime.totalMemory() - Runtime.freeMemory()` and compares it with `Runtime.maxMemory()`. A reported loading-screen value materially above 20,576 MiB was not reproduced by this capture. Windows working-set figures and loading-screen heap figures are different measures.

ZGC's active soft maximum equals the hard maximum, and its verified unused-memory return delay is 300 seconds. This permits the heap to grow toward the configured cap during sustained allocation. `AlwaysPreTouch` is enabled, but with `Xms256m` it does not mean the entire 20 GiB maximum was preallocated at launch. [Oracle ZGC guidance](https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector2.html)

## Where the allocations come from

The five-minute recording ended at about 14:28:56, before startup finished. Percentages below are estimated allocation pressure in that interval, not retained memory or guaranteed recoverable RAM. Categories use the full sampled stack and are mutually exclusive.

| Identified path | Share of filtered allocation sample weights |
| --- | ---: |
| StellarCore resource discovery/binding, including work delegated to other resource packs | 20.05% |
| Other VintageFix model, texture and resource paths | 17.35% |
| CraftTweaker `replaceAllOccurences` processing | 7.33% |
| Tinkers ore-dictionary melting registration | 4.92% |
| StellarCore preallocated state-mapper maps | 2.58% |
| Remaining paths | 47.76% |

1. **StellarCore and VintageFix overlap in two documented areas.** `ParallelModelLoader=true` runs alongside VintageFix `mixin.dynamic_resources=true`; `ResourceExistStateCache=true` runs alongside VintageFix `mixin.resourcepacks=true`. Both sets of code appear in live stacks, and the log repeatedly reports models loading outside StellarCore's concurrent state. Several model errors also occur, but these errors have not individually been proven to result from this overlap. The installed StellarCore jar contains custom resource integration code, so an actual comparison run is particularly important. [StellarCore compatibility notes](https://github.com/NovaEngineering-Source/StellarCore#compatibility-note)

2. **An exact avoidable allocation path is visible in installed bytecode.** With parallel model loading enabled, StellarCore's `MixinStateMapperBase.injectInit` constructs a `NonBlockingIdentityHashMap(8192)` for each state mapper. VintageFix's `ModelLocationInformation.init` triggers this path repeatedly. The alternative branch uses a default-size fastutil map. The recording repeatedly samples allocations from these large preallocated tables. The measured cumulative allocation is not the amount held simultaneously.

3. **Recipe rewriting produces significant temporary data.** There are 19 `recipes.replaceAllOccurences` calls across seven scripts. JFR shows `CraftTweaker.onPostInit → ActionReplaceAllOccurences.setCurrentModifiedRecipe → MCRecipeWrapper.getIngredients2D → MCOreDictEntry.getFromIngredient`, creating ingredient arrays and wrappers. Eight calls are in `scripts/lucraft.zs`; three of those specify a particular recipe output. Review broad replacements first and preserve their recipe semantics when optimizing.

4. **Tinkers performs a full melting-recipe rescan.** At 14:28:41, the log reports that the mod list changed and recipes are being rescanned. A thread dump confirms the client inside `registerRecipeOredictMelting → TinkerRegistry.getMelting → MeltingRecipe.matches → ListUtil.getListFrom`, with a StellarCore hook also present. This is another measured source of transient allocations. A subsequent unchanged launch may avoid some rescanning, but that was not tested.

The resource manager reloaded five times by 14:25:59. VintageFix later reported item-baking passes lasting approximately 27 and 24 seconds. Multiple processing passes compound allocation pressure. The modest external resource-pack sizes alone do not justify blaming them, and compressed file size is not a reliable measure of runtime RAM use.

## First comparison to run

Keep the same 20,576 MiB heap limit and the same mods initially, so the effect can be isolated. Close the game normally, back up the two settings files, and test these changes separately:

1. In `config/stellar_core.cfg`, change `B:ParallelModelLoader=true` to `B:ParallelModelLoader=false`. Keep VintageFix dynamic resources enabled. This also selects the smaller state-mapper allocation branch and removes the documented parallel/dynamic model-loading overlap.
2. On a subsequent comparison, in `config/vintagefix.properties`, change `mixin.resourcepacks=true` to `mixin.resourcepacks=false` while retaining StellarCore's resource existence cache. This follows StellarCore's documented compatibility configuration and targets the repeated `CachedResourcePath` work seen underneath StellarCore resource discovery. VintageFix dynamic resources remain enabled.

These are test candidates, not measured fixes. Compare peak heap occupancy, post-GC occupancy, process working set, load time, and missing-model errors through the same startup stages. Restore a setting if model loading or rendering regresses. The configuration snapshots in `baseline-config` preserve this investigation's starting values. No pack settings, scripts or mod jars were edited during diagnosis.

After those comparisons, optimize broad CraftTweaker recipe replacements without changing outputs or ingredients. Consider a lower ZGC soft target only after establishing the normal post-load footprint; lowering the hard cap prematurely could increase collection work or cause allocation stalls. Switching collectors or changing multiple JVM flags at once would make the first comparison harder to interpret.

## Evidence and limits

- `loading.jfr`: completed five-minute local recording; no upload was made.
- `samples.csv`: 26 roughly ten-second heap/process/system snapshots.
- `gc-live.log` and `gc-summary.txt`: collection history.
- `allocations-filtered.txt`: allocation stack analysis, including representative call stacks.
- `stellar-statemapper-bytecode.txt` and `splash-bytecode.txt`: inspection of the installed jars.
- `threads.txt`: later startup thread snapshot.
- `AnalyzeAllocations.java`: reproducible analysis; run with `loading.jfr skip-first`.

JFR was attached after startup began. One first allocation sample attributed about 55.65 GiB of cumulative weight to a single Lucraft reader allocation; bytecode and sample count do not support treating that as a Lucraft allocation hotspot. The report conservatively discards the first allocation sample for each thread. This avoids misleading attribution from a known class of recording-start sampling issues. Sample weights estimate cumulative allocation pressure, not individual object sizes. [OpenJDK event metadata](https://github.com/openjdk/jdk/blob/master/src/hotspot/share/jfr/metadata/metadata.xml)

No allocation stalls or out-of-memory errors were found in the captured interval. This does not rule out problems before recording began, later in startup, or in a loaded world. No full heap dump or forced garbage collection was requested. The flight recording completed, the finite sampler ended, and temporary GC file logging was turned off after collection.
