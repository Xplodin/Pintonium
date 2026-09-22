# BeanPack loading-memory fix

**Merged into BeanUtils 2.1.5 at 16:54 on September 13, 2026.** The installed replacement is `mods/beanutils-2.1.5.jar`. The standalone memory-fix jar and BeanUtils 2.1.4 were moved to `codex-backups/beanutils-memoryfix-merge-20260913-165400/`. Do not reinstall the standalone memory-fix jar alongside BeanUtils 2.1.5. Its source and verification now also live in `C:\Users\somet\OneDrive\Documents\cofh-neg-y-patch-src`. To undo the merge, close Minecraft, move BeanUtils 2.1.5 out of `mods`, and restore both original jars from that backup. The older instructions below describe the original standalone installation.

Installed September 13, 2026 for the local BeanPack Invincible instance, Minecraft 1.12.2 / Cleanroom 0.6.13-alpha / GraalVM Java 25.

**Startup fix at 16:39:** The launcher allocation was subsequently lowered to 12,192 MiB, below the fixed 12 GiB (12,288 MiB) soft target. The JVM rejected that combination before Minecraft loaded. The fixed SoftMaxHeapSize flag has now been removed; the soft maximum follows the launcher's hard allocation automatically. The resource-path patch and 30-second uncommit delay remain installed. A separate launch of the same Java executable with the corrected flags succeeded. The current launcher allocation is 12,192 MiB; earlier figures below describe the initial installation and diagnostic runs.

## What changed

- Added `mods/!beanpack-resource-path-memoryfix-1.0.jar` to the instance. This small custom coremod replaces the exact vanilla `AbstractResourcePack` asset-path formatter with direct string construction. It introduces no resource cache. If the expected method has already changed, it leaves the class untouched.
- Initially added `-XX:SoftMaxHeapSize=12g -XX:ZUncommitDelay=30` to `config/relauncher.json`. The fixed soft target was later removed as described above. ZGC can still return eligible unused heap regions after a 30-second delay instead of the default 300 seconds. This is not a guaranteed total-process memory limit.
- Existing VintageFix `mixin.resourcepacks=false`, VintageFix `mixin.dynamic_resources=true`, and StellarCore `ParallelModelLoader=false` settings were preserved.

Instance: `C:\Users\somet\curseforge\minecraft\Instances\BeanPack Invincible`

CurseForge's allocation at initial installation was **20,576 MiB**, approximately **20.1 GiB**; it was subsequently lowered to **12,192 MiB**. A soft target can be exceeded up to the hard allocation and must never exceed that allocation. To cap the loading screen's Java heap at 16 GiB, this profile's CurseForge memory allocation would be **16,384 MB**. Windows process memory can still exceed a heap limit because Java also uses memory outside the heap. A lower hard limit can cause an out-of-memory failure if a workload needs more live memory.

## Why this addresses the profile

Earlier startup recordings showed large temporary allocation bursts: normal garbage collection reduced roughly 18–19 GiB heap occupancy to much smaller live sets. This does not establish a retained-memory leak.

With VintageFix's resource-pack group disabled, vanilla resource-path formatting became the first non-JDK frame for 27.61% of the sampled allocation weight in one three-minute recording. The group had supplied both caching changes and the useful formatter optimization. The new coremod retains only the latter optimization. Allocation samples are not retained-memory measurements, and this percentage does not predict a matching reduction in peak RAM.

The pack also performs an extra full resource reload from Beyonder. That remains unchanged because skipping it without preserving its resource-pack registration could break its assets.

## Verification and next launch

The build test transformed the real mapped Minecraft class, verified that exactly one existing method changed, and executed the original and patched methods with JVM bytecode verification enabled. All 15,300 comparisons matched, including Unicode, percent signs, empty strings, null getter results, and three format locales. Null arguments still throw the same exception. Idempotence, conservative matching, and coremod class dispatch also passed.

The installed jar's SHA-256 matches the tested build:

`666391C358B5A451130784C98288135BA72DC30B6BC8BFC9D7EC51E372F17841`

The pack's GraalVM accepted both new JVM flags. A subsequent launch (PID 40404) confirmed the transformation and both flags. A 60-second live recording captured 588 allocation stacks through the resource-path helper, with zero calls through its old String.format path. The implementation is active. At 15:35:18, heap usage was 9.63 GiB and process working set was 16.87 GiB, with a 16.94 GiB process peak so far. Loading was still underway. This is encouraging compared with earlier 21–22 GiB process peaks, but a completed-startup comparison and isolation of the patch's effect from JVM tuning remain unverified. Detailed evidence is in `../memory-diagnostics/2026-09-13/run-40404-patched/findings.md`.

Launch BeanPack again. Successful transformation writes this line to `logs/latest.log`:

`[BeanPack Memory] Replaced asset-path String.format with direct string construction.`

If the log instead says the formatter was unrecognized, the transformer left it unchanged. Record the startup peak and the memory a minute after reaching the menu to compare both loading and settled memory use.

## Undo

Close Minecraft, move `mods/!beanpack-resource-path-memoryfix-1.0.jar` out of the `mods` folder, and restore `config/relauncher.json` from:

`C:\Users\somet\curseforge\minecraft\Instances\BeanPack Invincible\codex-backups\memory-fix-20260913-151259\relauncher.json`

If you have made other relauncher changes since this installation, remove only `-XX:SoftMaxHeapSize=12g` and `-XX:ZUncommitDelay=30` from its `args` field instead of restoring the whole file. Restart Minecraft after undoing the changes.

## Rebuild

Run `./build.ps1` in this directory. It uses the locally installed Cleanroom, Foundation, ASM, and Minecraft jars; compiles Java 8 bytecode; runs the verification; and packages a jar in a new timestamped `build` directory. It does not install the jar automatically. Source is specific to this instance's modern Cleanroom/ASM runtime, not a tested generic Forge distribution.

ZGC flag behavior: [Oracle ZGC tuning documentation](https://docs.oracle.com/en/java/javase/21/gctuning/z-garbage-collector2.html).
