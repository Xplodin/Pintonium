# BeanUtils: TAIGA descending-worldgen notification fix

## Finding

The live September 18 worldgen JFR contains 3,759 server execution samples. TAIGA `Generator.generateOreDescending` appears in 564. In 562 of those samples, the stack passes through the two-argument `World.setBlockState`, `markAndNotifyBlock`, neighbor notifications and `ChunkProviderServer` chunk provision. Below these calls the profile shows additional Depths Update / Better Caves generation. This is strong evidence of notification-triggered cascading chunk generation in that captured run, not merely slow vertical scans. Inclusive stack shares overlap; this does not predict a 15% FPS gain.

Installed versions inspected: BeanUtils 2.1.7 and TAIGA-Replant-Replant(1.0.0-beta). BeanUtils already adds the negative-Y descending passes in MixinWorldGen; MixinGenerator overwrites the separate stone-variant generator. Neither currently fixes the two placement calls in generateOreDescending.

## Integration

1. Copy `src/main/java/com/bean/beanutils/mixin/MixinTaigaDescendingGeneration.java` into the corresponding BeanUtils source directory.
2. Add `"MixinTaigaDescendingGeneration"` to the existing `mixins` array in `mixins.beanutils.taiga.json`, retaining all existing entries and the current optional-TAIGA loader logic. The supplied JSON mirrors the installed 2.1.7 configuration with this one entry added; merge the entry if your source version differs.
3. Build with BeanUtils' normal Forge reobfuscation process. This source uses MCP Minecraft method names, while the TAIGA overwrite has `remap=false` because its public method is not a Minecraft-mapped name. No new annotation refmap target string is required.
4. Test in a copied instance/world with fresh chunks before replacing the normal BeanUtils JAR. Do not install a second BeanUtils JAR alongside the first.

This is source for integration, not a built replacement mod. The current BeanUtils source project was not available during preparation, so a full BeanUtils build and actual runtime Mixin application have not been verified.

## What changes

Only TAIGA's descending generator placements change from implicit flags 3 (`NOTIFY_NEIGHBORS | SEND_TO_CLIENTS`) to flags 18 (`SEND_TO_CLIENTS | NO_OBSERVERS`). Forge's definitions:
https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/common/util/Constants.java

Flag 2 alone suppresses ordinary neighbor notification but leaves observer notification enabled. Avoid a global World.setBlockState patch: normal block placement, fluids, machines and redstone still need notifications.

The mixin preserves TAIGA's existing method signature, loops, random calls, host checks, write order, Y ranges, and boundary behavior—including its existing minY-1 probe. Existing BeanUtils negative-Y passes, ore counts and biome choices remain as implemented. No scan optimization, async generation, direct chunk write, or ore-density reduction is bundled in this first fix.

## Validation

The headless test extracts and executes the original descending method directly from the installed TAIGA JAR using ASM, then compares it with the proposed mixin method using simulated block storage. Across **8,400 cases and 10,355 placements**, it verifies identical read counts, ordered writes, resulting block maps and final RNG state. Tests cover negative chunk coordinates, negative Y, equal min/max bounds, zero attempts, all-lava columns, all-air columns and mixed bedrock/lava/stone hosts. Every original write uses 3; every patched write uses 18.

Result: `../regression-result.txt`. Harness and test-only Minecraft classes: `../tests/`. The mixin compiled against real Forge 2860 constants and MixinBooter 10.7 annotations, with test substitutes for Minecraft World/BlockPos/IBlockState. This does not substitute for a reobfuscated BeanUtils build or real fluid/light/chunk testing.

## Runtime checks

- Compare the same seed, fresh ungenerated area, route, render distance and JVM settings, with identical BeanUtils except this patch.
- Confirm logs apply the mixin and do not report overwrite conflicts. Check both with and without TAIGA if BeanUtils supports optional TAIGA.
- Inspect basalt/lava surfaces and Eezo near negative-Y bedrock; include Nether descending ore passes. Observe lava updates, lighting, and save/reload behavior.
- Repeat the worldgen profile. Check time under `Generator.generateOreDescending -> World.setBlockState -> notifyNeighbors... -> ChunkProviderServer`, and compare tick spikes and chunk throughput.
- Retain the old JAR and test-world copy for rollback.

Suppressing notifications intentionally changes immediate neighbor/fluid/observer behavior. Preventing recursive chunk generation can also change generation order and therefore other mods' world results. The tests establish isolated-generator placement/RNG equivalence, not bit-identical whole worlds. Lighting, block callbacks or other generators may still load chunks; this is not a universal no-cascade guarantee.

## Other options

Pre-generating the exploration area moves the generation work out of active exploration, but does not correct this code path. Lowering TAIGA ore counts changes world content and is not needed for this targeted fix. In the installed BeanUtils code, the deep basalt pass uses `Math.max(1, BASALT_VAL / 2)`, so setting the basalt count to zero does not fully disable that pass. Do not use ore-count reduction as the first workaround.
