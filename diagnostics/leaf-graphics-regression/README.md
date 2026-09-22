# Leaf graphics regression

Run `./diagnostics/leaf-graphics-regression/run.ps1` after the 1.12.2 production build.

The probe executes compiled method bodies extracted from the cached Minecraft
1.12.2 / Forge 14.23.5.2860 jar: `BlockLeaves.setGraphicsLevel`, `isOpaqueCube`,
`getRenderLayer`, and `Block.shouldSideBeRendered`, `doesSideBlockRendering`,
`isOpaqueCube`. It also executes the built Pintonium graphics quality resolver
and new reload redirect. ASM remaps those method owners and argument types into
small boot-free collaborators; none of those tested method bodies are rewritten
in Java or reimplemented by the test.

The six global Fast/Fancy + leaves Default/Fancy/Fast combinations use an
independent truth table. Baseline vanilla reload behavior must reproduce the two
conflicting option combinations. The patched resolver must keep leaf opacity,
leaf layer and all six neighboring full-block faces consistent. The loop updates
the same two leaf objects repeatedly, also exercising changes between settings.

The probe checks the compiled development annotation, the production SRG method
and field selectors, production mixin discovery metadata, and that vanilla's actual
reload method contains both matching leaf updates.

This is a headless method-level regression, not a full Mixin launch or visual
rendering test. World access, block states, bounding boxes, positions, enums,
option storage and enum-switch storage are minimal collaborators. It does not
boot Forge, apply third-party transformers, create an OpenGL context, test modded
leaf overrides, or interact with the running game. Those integration behaviors
still need an in-game check after restart/reload.
