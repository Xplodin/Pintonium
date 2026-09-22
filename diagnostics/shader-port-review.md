# Actinium / AUSM port review — 2026-09-18

Scope: Pintonium Minecraft 1.12.2, existing workspace at 6574e89 plus the user's pending entity-rendering and version edits. Those edits were preserved. This is a small initial batch, not full parity with either project.

## Sources inspected

- Actinium `cf3ba541ba0698af94dbebf5142528ab7961f586`: https://github.com/DHJComical/Actinium
- AUSM `a27ee39b4ca9b475e29bc2a01f027dde49de6799`: https://github.com/MtcLuna05/AUSM
- Actinium's THIRD_PARTY_NOTICES distinguishes imported Iris/Celeritas/Angelica LGPL code from its GPL root. AUSM's checked-out LICENSE is LGPL-3.0. Do not assume the root license establishes every file's provenance.

## Implemented initial batch

### Mutable uniform cache snapshots

Actinium a24ad226af4a9f3f906037692fd7da3556818e16 records Celeritas upstream 927e7a09 as the source of this correction:
https://github.com/DHJComical/Actinium/commit/a24ad226af4a9f3f906037692fd7da3556818e16

Our Vector2Uniform and Vector4ArrayUniform had the same aliasing bug: caching the supplier's mutable object makes later comparisons miss in-place changes. Cache an owned copy instead. Preserve Pintonium's notifier overloads and Vector2Uniform's initial upload (including an initial zero vector), rather than copying Actinium's older class wholesale. No pipeline, shader selection, rendering order, or shader-pack rewrite is introduced.

### Distant Horizons VAO initialization

Actinium cf3ba54 records Angelica #2090 / 8f981510 as its source:
https://github.com/DHJComical/Actinium/commit/cf3ba541ba0698af94dbebf5142528ab7961f586

Removed the redundant constructor-time glVertexAttribPointer call in the 1.12.2 IrisGenericRenderProgram. At that point the class has not bound the DH VBO. Its existing bindVertexBuffer method binds the VBO and defines attribute 0 with stride 12 before drawing. Attribute enabling and that binding path remain intact. The modern implementation is outside this batch.

## Candidates deliberately not merged

| Candidate | Local assessment |
| --- | --- |
| AUSM 35f16a1 attachment-only blending | Our overrideBufferBlend already changes only its indexed target; copying AUSM's global enable/disable changes is not justified. Our GL state service also differs. |
| AUSM 0f64751 Gnetum HUD/crosshair | Worth a dedicated reproduction because Gnetum is installed, but AUSM restores its own world-image/HUD boundaries. No equivalent defect established here. |
| AUSM 81ac3cb hand-depth, bloom, texture-cache fixes | Substantial ownership and pipeline differences. Test hand translucency, shader toggles, texture units and framebuffers before adapting. |
| AUSM a27ee39 Advanced Rocketry skies | Pack-specific Complementary source rewriting and provider checks. Not a generic Galacticraft fix; do not apply to every space dimension or to the current Euphoria pack blindly. |
| Actinium cf3ba54 DH far-boundary correction | Our ChunkTracker also requires neighbors, but the camera-distance shim feeds more than the shader far uniform. Requires a separately scoped transition test; do not globally subtract from render distance. |
| Actinium a24ad22 shadow near/far plane removal | Our inspected AdvancedShadowCullingFrustum already has no depthNear/depthFar optimization to remove. |
| Actinium renderer/GLSM replacement | Would overlap our terrain renderer, legacy entity bridges, and existing compatibility work. Not suitable for a small patch batch. |

## Validation and release boundary

The headless regression harness compiles the actual production uniform classes against a test-only GL upload recorder and real JOML. It failed on the original vector implementation, then independently failed on the original array implementation with the vector fixed. Both pass after correction, including notifier updates and suppression of unchanged uploads. This checks upload decisions and values, not driver rendering. Run `diagnostics/shader-port-tests/run.ps1` with JAVA_HOME set to a JDK and the project's JOML dependency cached.

Build command: `gradlew.bat "-Ptarget_versions=1.12.2" :common-shaders:test :forge122:1.12.2:packageJar --console=plain`, using the installed JDK 21. The unquoted PowerShell property initially selected the wrong targets; JDK 25 subsequently failed initializing the compiler. Neither required changing project settings. See shader-port-build-java21.log for the successful retry's final result.

Before promotion into the active modpack, compare against the current JAR in a separate test instance: shaders off; Complementary/Euphoria, Solas and Lumina; shader reload and on/off toggles; entity damage tint, armor/glint and block entities; first-person hand; translucent water; Gnetum HUD/crosshair and inventory previews; DH near/far transitions and window resizing. Build/test success does not establish visual parity. No active instance JAR was replaced.

These rendering fixes are not a fix for the earlier measured world-generation CPU workload.

Final validation: JDK 21 build succeeded in 55 seconds. All eight existing shader tests passed (four AlphaTestTransformer, four DHTransformer), and the mutable-uniform regression harness passed. `git diff --check` passed. The remapper emitted missing-entry warnings for several mixins; their runtime impact has not been established, so this is a test artifact, not a visually validated release.

Artifact: `build/libs/2.4.2-dev/pintonium-forge-1.12.2-2.4.2-dev.jar`. It includes the user's pre-existing workspace changes. The existing version was preserved. Packaged DH bytecode was inspected to confirm the vertex pointer is now configured only in bindVertexBuffer.
