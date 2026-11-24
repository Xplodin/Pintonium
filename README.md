<img src="modern/src/main/resources/icon.png" width="128">

# Pintonium

Pintonium is a free and open-source performance & shaders mod for Minecraft clients. It is a fork of Celeritas, which
itself is based on Embeddium, the last FOSS-licensed version of Sodium, and Oculus 1.7.

I maintain this mod for personal use & experimentation and make the source code available for other projects and
developers who may be interested. There is also no guarantee of active maintenance, including bugfixes
or ports to any newer Minecraft versions. That said, the code remains
LGPL-3.0, so other projects under a compatible license (including Embeddium) should feel free to incorporate bugfixes
and features they find useful. That said, expect minimal support, and many possible bugs due to limited testing.

**Important note:** There are currently no official Pintonium binary releases. If you download a precompiled
Pintonium .jar file from any 3rd party source, we cannot provide any support for such files, and you do so at your own
risk.

## Project layout

Pintonium uses the [Stonecutter](https://codeberg.org/stonecutter/stonecutter) toolchain to reduce the effort required
to support individual Minecraft versions. Additionally, as much core rendering code as possible is fully abstracted
from Minecraft within a `:common` project.

## How to build

The fastest way to build for exactly one version target is to run `./gradlew -Ptarget_versions=<version> packageJar`.
This command avoids configuring as many Minecraft targets as possible. The resulting jar file will be available
in `build/libs/<pintonium version>`.

Note: the `target_versions` property accepts a standard Stonecutter predicate, so you can also use syntax like
`./gradlew -Ptarget_versions="<1.8.9"`.

You can also explicitly target a project with regular Gradle syntax, e.g. `./gradlew :forge122:1.12.2:packageJar`
or `./gradlew :forge1710:packageJar`, but this will configure other subprojects even if they're not used.

To build for every Minecraft version at once, execute `./gradlew packageJar`.

## How to use

Pintonium generally requires a "modernized" environment on older Minecraft versions, and will not run out-of-the-box
with a default modded Minecraft instance. Newer Minecraft versions ship with the necessary dependencies and will not
require any custom setup.

For legacy (pre-1.13) versions, your game instance must provide LWJGL 3, and in some cases must also be running Java 21.
Most of these versions do not have a ready-to-download launcher profile available that meets these requirements out of
the box, except for Forge 1.7.10 (lwjgl3ify) and Forge 1.12.2 (lwjgl3ify or Cleanroom Loader).

The Ornithe versions (pre-1.7) are especially experimental and have not yet been tested outside of a development environment
at all.

For modern (1.13+) versions, the final mod jar should run as-is in a standard instance for that version (e.g. Java 17
or 21 are not required, unless the underlying Minecraft version itself requires them).

## Mod post-processing API

The legacy Forge shader renderer exposes callbacks after shader-pack output, scene depth and camera matrices,
and managed GL scopes for fullscreen effects and world geometry. See the [integration guide](docs/post-processing-api.md)
for registration examples, the depth/state contract, and validation instructions.

## License

Pintonium is licensed under the Lesser GNU General Public License version 3, as it uses code from its upstream fork, Iris 1.7,
Sodium 0.5.11-, and other FOSS projects.

Portions of the option screen code are based on Reese's Sodium Options by FlashyReese, and are used under the terms of
the [MIT license](https://opensource.org/license/mit), located in `src/main/resources/licenses/rso.txt`.

This project does not include and has no plans to include any code from Sodium 0.6+ or 0.5.12+, as these versions of
Sodium are not available under a free and open-source license.
Please reach out to @embeddedt on Discord if you have concerns regarding the license of any code in this project.


## API

# Post-processing API (legacy Forge)

Pintonium exposes a supported insertion point after shader-pack composition/final output and before the HUD.
It works with packs that omit the `final` program too. This implementation is in the legacy Forge renderer;
other platforms return an empty capability. Callbacks currently run only with an active shader pipeline.
Keep your usual vanilla rendering path when shaders are disabled or the capability is absent.

## Register once

Run registration during client initialization, after Pintonium's API is available. Keep and close the returned
registration when disabling your integration. Never register from a per-frame event.

```java
import org.taumc.celeritas.api.v0.CeleritasShadersApi;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingApi;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingContext;

private PostProcessingApi.Registration atmosphere;

public void initializeClient() {
    CeleritasShadersApi.getInstance().getPostProcessing().ifPresent(api -> {
        atmosphere = api.onFinalPassComplete("snowblizzard:atmosphere", frame -> {
            renderAtmosphere(frame);
            frame.withWorldState(() -> renderCameraRelativeSnow(frame.getPartialTicks()));
        });
    });
}

public void disableIntegration() {
    if (atmosphere != null) {
        atmosphere.close();
        atmosphere = null;
    }
}
```

Callbacks execute on the Minecraft render thread in registration order. Registration/unregistration is
thread-safe; changes while callbacks are dispatching affect the next frame. Registrations survive pack
reloads and dimension changes. Each callback receives its own context, valid only during that invocation.
Matrix getters return independent, read-only, direct `FloatBuffer` copies with 16 column-major floats,
positioned at zero, ready for `glLoadMatrix` or `glUniformMatrix4`. They expose no shaded JOML types.
Do not retain contexts or texture IDs between invocations.
Use your mod loader's optional-dependency mechanism before referencing these classes if Pintonium is optional.
The extension requires a Pintonium build containing this API; capability discovery handles platforms, not older jars.

## Fullscreen effects

The callback starts with a depth-free framebuffer that writes directly to Minecraft's finished color texture.
This avoids sampling a depth texture attached to the active draw framebuffer. There is no scene-depth blit.
`copyColorTexture()` snapshots the current output into a separate RGBA8 texture whenever called, including
previous effects and previous draws in this callback. Copy before binding input textures. It preserves the
current documented state, and reallocates its storage after resize. A copy is only performed on request.

For example, assuming your mod already compiled a GLSL program and looked up its uniform locations:

```java
private void renderAtmosphere(PostProcessingContext frame) {
    int depth = frame.getFinalDepthTexture();
    if (depth < 0) return;
    int scene = frame.copyColorTexture();

    GL20.glUseProgram(fogProgram);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, scene);
    GL20.glUniform1i(sceneUniform, 0);
    GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, depth);
    GL20.glUniform1i(depthUniform, 1);
    // Upload camera matrices/inverses and weather parameters as needed by your shader.
    frame.drawFullscreenQuad();
}
```

`drawFullscreenQuad()` supplies clip-space positions [-1,1] and bottom-left-origin UVs [0,1]. A GLSL 1.20
compatibility vertex shader can use `gl_Position = gl_Vertex;` and `gl_MultiTexCoord0.xy` for UVs. This helper
uses immediate-mode vertices; shaders requiring custom vertex attributes should draw their own geometry.
Mods still own compilation, uniforms, and deletion of their shader programs. No shader-source merging occurs.

If you need several fullscreen stages, call `copyColorTexture()` between stages. The returned texture is
reused, so previous snapshots are overwritten. Do not attach the snapshot as an output or alter renderer-owned
texture parameters. Bind your own sampler objects if you need different filtering.

## World geometry and depth

The 1.12 shader path preserves scene depth across vanilla's pre-hand depth clear and captures pre-hand
depth before that point. Hand projection uses the same 0.125 clip-depth scaling as the repository's modern
and 1.7 hand renderers, so the hand can coexist with world depth without the vanilla clear. The unshaded
vanilla path keeps its usual clear and hand projection.

`withWorldState(action)` binds Minecraft's main framebuffer, including its existing depth attachment, loads
captured world camera matrices, enables `GL_DEPTH_TEST` with `GL_LEQUAL`, and keeps depth writes disabled.
Draw camera-relative vertices: subtract the interpolated camera position from world coordinates once.
Blending, lighting, and alpha testing start disabled; enable whichever your snow renderer needs.
The previous state is restored even when the action throws. `withNeutralState(action)` similarly provides
a nested fullscreen scope. Every callback already has an outer neutral scope.

- `getFinalDepthTexture()` is scene `depthtex0`, containing whatever actually wrote depth during world/hand
  rendering. It is normalized hardware depth, not linear distance. Hand projection/depth conventions may
  differ from world geometry; do not reconstruct hand distance using the world projection blindly.
- `getDepthTextureNoHand()` is the pipeline's pre-hand snapshot (`depthtex2`). Comparing it with final depth
  can help identify hand pixels, but is not a guaranteed segmentation mask. It returns -1 if no pre-hand
  snapshot was captured this frame, rather than returning stale data.
- Translucent objects that do not write depth, shader-generated geometry, refraction, TAA, and other screen
  warps may not correspond exactly to scene depth. This API cannot synthesize missing depth.
- Never sample final scene depth while inside `withWorldState`: it is attached to that draw framebuffer.
  Sample it in the default/neutral fullscreen scope instead.
- The matrices describe the captured world camera, including Minecraft camera transforms. They do not
  reproduce arbitrary vertex displacement or projection changes implemented inside shader-pack GLSL.
- Geometry drawn here comes after pack processing. It receives no pack lighting, bloom, temporal filtering,
  or tone mapping. Use normal world rendering hooks when those interactions are required.

## GL state contract

On neutral entry: the main color image is bound through a color-only framebuffer for read and draw; viewport
is the full framebuffer; program is 0; a private VAO is bound; array/element buffers are 0; depth test, depth
writes, blend, cull, alpha test, lighting, fog, scissor, stencil testing, color logic, polygon offset fill,
rasterizer discard, and framebuffer sRGB conversion are disabled. Polygon mode is FILL and depth range [0,1]. Color writes are
enabled, color is white, blend equations are ADD with ONE/ZERO factors. Fixed-function 2D texturing is disabled,
all 2D textures/samplers are unbound, texture unit 0 is active, and projection/model-view matrices are identity, with model-view
selected. A mod using fixed-function textured geometry should enable texture 2D itself.

Each callback and helper scope restores compatibility-profile server/client attributes, projection/model-view
matrix tops and matrix mode, program, read/draw framebuffer, VAO, array/element/pixel buffers, 2D texture bindings,
active texture, and sampler bindings (when OpenGL 3.3 is available). Vanilla caches are synchronized for blend
enable/factors, depth enable/function/write mask, alpha enable/function/reference, cull/lighting/fog enables,
color mask/current color, active texture, and 2D texture enable/bindings. Use raw OpenGL for state outside that
cache list, or restore any additional Minecraft cache values yourself.

This is state isolation, not a sandbox: do not modify/delete renderer-owned GL objects, change attachments,
reload packs from a callback, leave `glBegin`/`glEnd` or matrix/attribute stacks unbalanced, or alter texture
matrices, buffer-object contents, indexed buffer bindings, image bindings, or transform-feedback state without
restoring them yourself. Those operations are outside the managed contract. Programs and textures owned by
the mod remain the mod's responsibility.

Ordinary exceptions, assertion errors, and linkage failures are logged once per registration and subsequent
callbacks continue after state restoration. Failed effects retry next frame. Fatal VM errors and thread
termination are propagated. A partial draw performed before an exception is not rolled back.

With no registered effects, the render hook performs no GL queries, allocations, or copies. GL resources are
allocated lazily and released on pipeline destruction. State isolation has a per-effect GL-query cost; combine
related passes in one registration and use nested scopes only when needed.

## Validation in Minecraft

Build using JDK 21 (the repository's current Lombok version does not support the host's default JDK 25):

```powershell
.\gradlew.bat '-Ptarget_subprojects=forge122' '-Ptarget_versions=1.12.2' `
  :common-shaders:test :forge122:1.12.2:packageJar
```

The opt-in native smoke test uses a hidden GLFW compatibility-profile window and the real Minecraft
`GlStateManager`. It checks driver/cache restoration after exceptions, fullscreen color-copy chaining,
scene-depth sampling, world depth occlusion, context lifetime, resize, and resource recreation:

```powershell
.\gradlew.bat '-Ptarget_subprojects=forge122' '-Ptarget_versions=1.12.2' `
  -I scripts/post-processing-api/smoke.gradle :forge122:1.12.2:postProcessingSmoke
```

For visual integration testing, register a small effect and check: a pack with and without a final program;
first/third-person cameras and hand occlusion; snow behind solid blocks; resize/fullscreen and FOV changes;
pack reload and dimension transitions; two effects in order; a deliberately throwing callback followed by a
working one; clean HUD rendering afterward; and the vanilla fallback when shaders are disabled. Check the
OpenGL debug log for errors, especially depth/color feedback. Build/unit checks do not replace these checks.


## Credits

* The CaffeineMC team, for developing Sodium 0.5.11 & older, and making it open source
* Asek3, for developing Rubidium, the original port of Sodium 0.5 to Forge
* CelestialAbyss, for developing the Embeddium logo (which is reused here aside from recoloring), and input-Here for some very good visual touchups
* Ven ([@basdxz](https://github.com/basdxz)), for help with translucency sorting, suggesting the general approach for async occlusion culling, and other suggestions during development
* XFactHD, Pepper, and anyone else I've forgotten to mention, for providing valuable code insights

[![YourKit logo](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/)

YourKit supports open source projects with innovative and intelligent tools
for monitoring and profiling Java and .NET applications.
YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>,
<a href="https://www.yourkit.com/.net/profiler/">YourKit .NET Profiler</a>,
and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.

Special thanks to YourKit for providing a free license for my various open-source Minecraft projects.
