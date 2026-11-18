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
