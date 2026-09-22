package net.irisshaders.iris.pipeline;

import com.google.common.primitives.Ints;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.program.Program;
import net.irisshaders.iris.gl.program.ProgramBuilder;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.CommonShadowRenderer;
import net.irisshaders.iris.shadows.ShadowCompositeRenderer;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.RenderTargetStateListener;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.embeddedt.embeddium.compat.mc.MCShaderInstance;
import org.embeddedt.embeddium.compat.mc.MCVertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.taumc.celeritas.interfaces.IRenderTargetExt;
import org.embeddedt.embeddium.impl.gl.shader.ShaderType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static net.irisshaders.iris.IrisLogging.IRIS_LOGGER;
import static org.embeddedt.embeddium.compat.mc.MinecraftVersionShimService.MINECRAFT_SHIM;

public class VintageIrisRenderingPipeline extends CommonIrisRenderingPipeline {
    private static final ShaderKey[] NO_SHADER_KEYS = new ShaderKey[0];

    @Nullable
    private Program vintageEntityProgram;
    @Nullable
    private GlFramebuffer vintageEntityFramebuffer;
    @Nullable
    private GlFramebuffer vintageEntityFallbackFramebuffer;
    @Nullable
    private BlendModeOverride vintageEntityBlendOverride;
    private List<BufferBlendOverride> vintageEntityBufferBlendOverrides = Collections.emptyList();
    private boolean vintageEntityRenderingActive;

    @Nullable
    private Program vintageEntityCompatProgram;
    @Nullable
    private GlFramebuffer vintageEntityCompatFramebuffer;
    @Nullable
    private BlendModeOverride vintageEntityCompatBlendOverride;
    private List<BufferBlendOverride> vintageEntityCompatBufferBlendOverrides = Collections.emptyList();
    private boolean vintageEntityCompatRenderingActive;
    private boolean vintageEntityCompatBridgeLogged;
    private boolean vintageEntityShaderBridgeLogged;
    private boolean vintageEntityPreferShaderProgram;
    private boolean vintageEntityClampLowerBodySkyLight;
    private int vintageEntityColorUniformLocation = -1;
    private int vintageEntityCompatColorUniformLocation = -1;

    @Nullable
    private Program vintageBlockEntityCompatProgram;
    @Nullable
    private GlFramebuffer vintageBlockEntityCompatFramebufferBeforeTranslucent;
    @Nullable
    private GlFramebuffer vintageBlockEntityCompatFramebufferAfterTranslucent;
    @Nullable
    private BlendModeOverride vintageBlockEntityCompatBlendOverride;
    private List<BufferBlendOverride> vintageBlockEntityCompatBufferBlendOverrides = Collections.emptyList();
    private boolean vintageBlockEntityCompatRenderingActive;
    private boolean vintageBlockEntityUsesShaderPackProgram;
    private boolean vintageBlockEntityBridgeLogged;

    @Nullable
    private Program vintageParticleCompatProgram;
    @Nullable
    private GlFramebuffer vintageParticleCompatFramebufferBeforeTranslucent;
    @Nullable
    private GlFramebuffer vintageParticleCompatFramebufferAfterTranslucent;
    @Nullable
    private BlendModeOverride vintageParticleCompatBlendOverride;
    private List<BufferBlendOverride> vintageParticleCompatBufferBlendOverrides = Collections.emptyList();
    private boolean vintageParticleCompatRenderingActive;

    @Nullable
    private Program vintageBeaconBeamProgram;
    @Nullable
    private GlFramebuffer vintageBeaconBeamFramebufferBeforeTranslucent;
    @Nullable
    private GlFramebuffer vintageBeaconBeamFramebufferAfterTranslucent;
    @Nullable
    private BlendModeOverride vintageBeaconBeamBlendOverride;
    private List<BufferBlendOverride> vintageBeaconBeamBufferBlendOverrides = Collections.emptyList();
    private boolean vintageBeaconBeamRenderingActive;
    private boolean vintageBeaconBeamBridgeLogged;

    @Nullable
    private Program vintageHandCompatProgram;
    @Nullable
    private GlFramebuffer vintageHandCompatFramebufferBeforeTranslucent;
    @Nullable
    private GlFramebuffer vintageHandCompatFramebufferAfterTranslucent;
    @Nullable
    private BlendModeOverride vintageHandCompatBlendOverride;
    private List<BufferBlendOverride> vintageHandCompatBufferBlendOverrides = Collections.emptyList();
    private boolean vintageHandCompatRenderingActive;
    private boolean vintageHandCompatBridgeLogged;

    @Nullable
    private Program vintageLineProgram;
    @Nullable
    private GlFramebuffer vintageLineFramebufferBeforeTranslucent;
    @Nullable
    private GlFramebuffer vintageLineFramebufferAfterTranslucent;
    @Nullable
    private BlendModeOverride vintageLineBlendOverride;
    private List<BufferBlendOverride> vintageLineBufferBlendOverrides = Collections.emptyList();
    private boolean vintageLineRenderingActive;
    private boolean vintageLineBridgeLogged;

    public VintageIrisRenderingPipeline(ProgramSet programSet) {
        super(programSet);
        MINECRAFT_SHIM.populateBlockIds(this.pack);
        this.blockIdsNeedPopulation = false;
        this.createVintageEntityProgram();
        this.createVintageEntityCompatibilityProgram();
        this.createVintageBlockEntityCompatibilityProgram();
        this.createVintageParticleCompatibilityProgram();
        this.createVintageBeaconBeamProgram();
        this.createVintageHandCompatibilityProgram();
        this.createVintageLineProgram();
        this.vintageEntityFallbackFramebuffer = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, new int[] {0});
    }

    @Override
    protected void updateMCFBInfo() {
        Framebuffer main = Minecraft.getMinecraft().getFramebuffer();
        IRenderTargetExt ext = (IRenderTargetExt) main;

        this.mainFBWidth = main.framebufferWidth;
        this.mainFBHeight = main.framebufferHeight;
        this.mainFBDepthTextureId = ext.getIris$depthTextureId();
        this.mainFBDepthBufferVersion = ext.iris$getDepthBufferVersion();
    }

    @Override
    protected void renderHorizon() {
        // TODO: Port HorizonRenderer to 1.12 once the sky phase is wired precisely.
    }

    @Override
    protected boolean checkShadowUsesImages() {
        return false;
    }

    @Override
    public void addDebugText(List<String> messages) {
        messages.add("[Iris] 1.12 shader pipeline: composite/final MVP");
        messages.add("[Iris] Terrain shader overrides: Pintonium chunk renderer bridge enabled");
        messages.add("[Iris] Vanilla terrain face shading disabled: "
                + WorldRenderingSettings.INSTANCE.shouldDisableDirectionalShading());
        if (this.shadowRenderer == null) {
            messages.add("[Iris] Shadow Maps: not used by shader pack or not implemented on 1.12 yet");
        }
    }

    @Override
    protected void destroyHorizonRenderer() {
        // No horizon renderer is allocated in the MVP pipeline.
    }

    @Override
    protected @Nullable CommonShadowRenderer createShadowRenderer(CommonIrisRenderingPipeline pipeline, ProgramSource programSource,
            PackDirectives packDirectives, ShadowRenderTargets shadowRenderTargets, ShadowCompositeRenderer shadowCompositeRenderer,
            CustomUniforms customUniforms, boolean separateHardwareSamplers) {
        // TODO: Port the 1.12 shadow terrain renderer after terrain program overrides exist.
        return null;
    }

    private void createVintageEntityProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.Entities).orElse(null);
        if (source == null || !source.isValid()) {
            return;
        }

        try {
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName(),
                    source.getSourceNullable(ShaderType.VERTEX),
                    source.getSourceNullable(ShaderType.GEOMETRY),
                    source.getSourceNullable(ShaderType.FRAGMENT),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            CommonUniforms.addCommonUniforms(builder, this.pack.getIdMap(), this.packDirectives, this.updateNotifier, FogMode.PER_VERTEX);
            this.customUniforms.assignTo(builder);
            this.addGbufferOrShadowSamplers(builder, builder, () -> this.isBeforeTranslucent ? this.flippedAfterPrepare : this.flippedAfterTranslucent,
                    false, true, true, false);

            int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
            this.vintageEntityProgram = builder.build();
            this.vintageEntityColorUniformLocation = GL20.glGetUniformLocation(
                    this.vintageEntityProgram.getProgramId(), "entityColor");
            this.customUniforms.mapholderToPass(builder, this.vintageEntityProgram);
            this.vintageEntityFramebuffer = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare,
                    drawBuffers);
            this.vintageEntityBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.Entities.getBlendModeOverride());
            this.vintageEntityBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
            this.vintageEntityClampLowerBodySkyLight = this.celeritas$isColorAndAuxBufferLayout(drawBuffers);
        } catch (RuntimeException e) {
            if (this.vintageEntityProgram != null) {
                this.vintageEntityProgram.delete();
            }

            this.vintageEntityProgram = null;
            this.vintageEntityFramebuffer = null;
            this.vintageEntityBlendOverride = null;
            this.vintageEntityBufferBlendOverrides = Collections.emptyList();
            this.vintageEntityClampLowerBodySkyLight = false;
            this.vintageEntityColorUniformLocation = -1;
            IRIS_LOGGER.warn("Failed to create the 1.12 entity shader bridge. Entities will use vanilla rendering for this shader pack.", e);
        }
    }

    private void createVintageEntityCompatibilityProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.Entities).orElse(null);
        if (source == null || !source.isValid()) {
            return;
        }

        try {
            int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
            boolean solasStyleEntityLayout = this.celeritas$isColorAndAuxBufferLayout(drawBuffers);
            boolean pbrFresnelEntityLayout = this.celeritas$isPbrFresnelLayout(source, drawBuffers);
            // If the pack entity program cannot be used, keep the fallback restrained for
            // Solas-style color+aux layouts so the generic bridge does not over-brighten mobs.
            boolean useDirectEntityLightmapColor = solasStyleEntityLayout;
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_legacy_compat",
                    this.celeritas$getLegacyCompatibilityVertexSource(),
                    null,
                    this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, true, true, useDirectEntityLightmapColor, true, true, false, solasStyleEntityLayout, pbrFresnelEntityLayout),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            builder.addExternalSampler(IrisSamplers.ALBEDO_TEXTURE_UNIT, "tex", "texture", "gtexture");
            builder.addExternalSampler(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, "lightmap");
            this.celeritas$addLegacyCompatibilityUniforms(builder);

            this.vintageEntityCompatProgram = builder.build();
            this.vintageEntityCompatColorUniformLocation = GL20.glGetUniformLocation(
                    this.vintageEntityCompatProgram.getProgramId(), "entityColor");
            this.vintageEntityCompatFramebuffer = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageEntityCompatBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.Entities.getBlendModeOverride());
            this.vintageEntityCompatBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
            this.vintageEntityPreferShaderProgram = this.celeritas$prefersShaderPackEntityProgram(source);
        } catch (RuntimeException e) {
            if (this.vintageEntityCompatProgram != null) {
                this.vintageEntityCompatProgram.delete();
            }

            this.vintageEntityCompatProgram = null;
            this.vintageEntityCompatFramebuffer = null;
            this.vintageEntityCompatBlendOverride = null;
            this.vintageEntityCompatBufferBlendOverrides = Collections.emptyList();
            this.vintageEntityPreferShaderProgram = false;
            this.vintageEntityCompatColorUniformLocation = -1;
            IRIS_LOGGER.warn("Failed to create the 1.12 legacy entity compatibility shader. Trying the shader pack entity program instead.", e);
        }
    }

    private void createVintageBlockEntityCompatibilityProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.Block).orElse(null);
        if (source == null || !source.isValid()) {
            return;
        }

        int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
        boolean pbrFresnelBlockLayout = this.celeritas$isPbrFresnelLayout(source, drawBuffers);
        if (this.celeritas$isColorAndAuxBufferLayout(drawBuffers)
                && this.celeritas$tryCreateVintageShaderPackBlockEntityProgram(source, drawBuffers)) {
            return;
        }

        try {
            boolean solasStyleBlockLayout = this.celeritas$isColorAndAuxBufferLayout(drawBuffers);
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_block_entity_compat",
                    this.celeritas$getLegacyCompatibilityVertexSource(),
                    null,
                    this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, false, false, !solasStyleBlockLayout, true, false, false, solasStyleBlockLayout, pbrFresnelBlockLayout),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            builder.addExternalSampler(IrisSamplers.ALBEDO_TEXTURE_UNIT, "tex", "texture", "gtexture");
            builder.addExternalSampler(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, "lightmap");
            this.celeritas$addLegacyCompatibilityUniforms(builder);

            this.vintageBlockEntityCompatProgram = builder.build();
            this.vintageBlockEntityCompatFramebufferBeforeTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageBlockEntityCompatFramebufferAfterTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterTranslucent, drawBuffers);
            this.vintageBlockEntityCompatBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.Block.getBlendModeOverride());
            this.vintageBlockEntityCompatBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
            this.vintageBlockEntityUsesShaderPackProgram = false;
        } catch (RuntimeException e) {
            this.celeritas$clearVintageBlockEntityProgram();
            IRIS_LOGGER.warn("Failed to create the 1.12 legacy block entity compatibility shader. Block entities will use vanilla rendering for this shader pack.", e);
        }
    }

    private boolean celeritas$tryCreateVintageShaderPackBlockEntityProgram(ProgramSource source, int[] drawBuffers) {
        try {
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_block_entity",
                    source.getSourceNullable(ShaderType.VERTEX),
                    source.getSourceNullable(ShaderType.GEOMETRY),
                    source.getSourceNullable(ShaderType.FRAGMENT),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            CommonUniforms.addCommonUniforms(builder, this.pack.getIdMap(), this.packDirectives, this.updateNotifier, FogMode.PER_VERTEX);
            this.customUniforms.assignTo(builder);
            this.addGbufferOrShadowSamplers(builder, builder,
                    () -> this.isBeforeTranslucent ? this.flippedAfterPrepare : this.flippedAfterTranslucent,
                    false, true, true, false);

            this.vintageBlockEntityCompatProgram = builder.build();
            this.customUniforms.mapholderToPass(builder, this.vintageBlockEntityCompatProgram);
            this.vintageBlockEntityCompatFramebufferBeforeTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageBlockEntityCompatFramebufferAfterTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterTranslucent, drawBuffers);
            this.vintageBlockEntityCompatBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.Block.getBlendModeOverride());
            this.vintageBlockEntityCompatBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source, drawBuffers);
            this.vintageBlockEntityUsesShaderPackProgram = true;
            return true;
        } catch (RuntimeException e) {
            this.celeritas$clearVintageBlockEntityProgram();
            IRIS_LOGGER.warn("Failed to create the shader pack block program for 1.12 block entities. Falling back to the generic compatibility bridge.", e);
            return false;
        }
    }

    private void celeritas$clearVintageBlockEntityProgram() {
        if (this.vintageBlockEntityCompatProgram != null) {
            this.vintageBlockEntityCompatProgram.delete();
        }

        this.vintageBlockEntityCompatProgram = null;
        this.vintageBlockEntityCompatFramebufferBeforeTranslucent = null;
        this.vintageBlockEntityCompatFramebufferAfterTranslucent = null;
        this.vintageBlockEntityCompatBlendOverride = null;
        this.vintageBlockEntityCompatBufferBlendOverrides = Collections.emptyList();
        this.vintageBlockEntityUsesShaderPackProgram = false;
    }

    private void createVintageParticleCompatibilityProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.Particles).orElse(null);
        if (source == null || !source.isValid()) {
            return;
        }

        try {
            int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_particle_compat",
                    this.celeritas$getLegacyCompatibilityVertexSource(),
                    null,
                    this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, false, false, false, false, false),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            builder.addExternalSampler(IrisSamplers.ALBEDO_TEXTURE_UNIT, "tex", "texture", "gtexture");
            builder.addExternalSampler(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, "lightmap");
            this.celeritas$addLegacyCompatibilityUniforms(builder);

            this.vintageParticleCompatProgram = builder.build();
            this.vintageParticleCompatFramebufferBeforeTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageParticleCompatFramebufferAfterTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterTranslucent, drawBuffers);
            this.vintageParticleCompatBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.Particles.getBlendModeOverride());
            this.vintageParticleCompatBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
        } catch (RuntimeException e) {
            if (this.vintageParticleCompatProgram != null) {
                this.vintageParticleCompatProgram.delete();
            }

            this.vintageParticleCompatProgram = null;
            this.vintageParticleCompatFramebufferBeforeTranslucent = null;
            this.vintageParticleCompatFramebufferAfterTranslucent = null;
            this.vintageParticleCompatBlendOverride = null;
            this.vintageParticleCompatBufferBlendOverrides = Collections.emptyList();
            IRIS_LOGGER.warn("Failed to create the 1.12 legacy particle compatibility shader. Particles will use vanilla rendering for this shader pack.", e);
        }
    }

    private void createVintageBeaconBeamProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.BeaconBeam).orElse(null);
        if (source == null || !source.isValid()) {
            return;
        }

        try {
            int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_beam",
                    source.getSourceNullable(ShaderType.VERTEX),
                    source.getSourceNullable(ShaderType.GEOMETRY),
                    source.getSourceNullable(ShaderType.FRAGMENT),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            CommonUniforms.addCommonUniforms(builder, this.pack.getIdMap(), this.packDirectives, this.updateNotifier, FogMode.PER_VERTEX);
            this.customUniforms.assignTo(builder);
            this.addGbufferOrShadowSamplers(builder, builder, () -> this.isBeforeTranslucent ? this.flippedAfterPrepare : this.flippedAfterTranslucent,
                    false, true, true, false);

            this.vintageBeaconBeamProgram = builder.build();
            this.customUniforms.mapholderToPass(builder, this.vintageBeaconBeamProgram);
            this.vintageBeaconBeamFramebufferBeforeTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageBeaconBeamFramebufferAfterTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterTranslucent, drawBuffers);
            this.vintageBeaconBeamBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.BeaconBeam.getBlendModeOverride());
            this.vintageBeaconBeamBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
        } catch (RuntimeException e) {
            if (this.vintageBeaconBeamProgram != null) {
                this.vintageBeaconBeamProgram.delete();
            }

            this.vintageBeaconBeamProgram = null;
            this.vintageBeaconBeamFramebufferBeforeTranslucent = null;
            this.vintageBeaconBeamFramebufferAfterTranslucent = null;
            this.vintageBeaconBeamBlendOverride = null;
            this.vintageBeaconBeamBufferBlendOverrides = Collections.emptyList();
            IRIS_LOGGER.warn("Failed to create the 1.12 beacon beam shader bridge. Crystal beams will use the active vanilla render state.", e);
        }
    }

    private void createVintageHandCompatibilityProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.HandWater).orElse(null);
        boolean usingHandWaterSource = source != null && source.isValid();
        if (source == null || !source.isValid()) {
            source = this.resolver.resolve(ProgramId.Hand).orElse(null);
        }

        if (source == null || !source.isValid()) {
            return;
        }

        try {
            int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
            if (usingHandWaterSource) {
                ProgramSource waterSource = this.resolver.resolve(ProgramId.Water).orElse(null);
                drawBuffers = this.celeritas$mergeDrawBuffers(drawBuffers, waterSource);
            }
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_hand_compat",
                    this.celeritas$getLegacyCompatibilityVertexSource(),
                    null,
                    this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, true, false, false, false, false),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            builder.addExternalSampler(IrisSamplers.ALBEDO_TEXTURE_UNIT, "tex", "texture", "gtexture");
            builder.addExternalSampler(IrisSamplers.LIGHTMAP_TEXTURE_UNIT, "lightmap");
            this.celeritas$addLegacyCompatibilityUniforms(builder);

            this.vintageHandCompatProgram = builder.build();
            this.vintageHandCompatFramebufferBeforeTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageHandCompatFramebufferAfterTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterTranslucent, drawBuffers);
            this.vintageHandCompatBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.HandWater.getBlendModeOverride());
            this.vintageHandCompatBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
        } catch (RuntimeException e) {
            if (this.vintageHandCompatProgram != null) {
                this.vintageHandCompatProgram.delete();
            }

            this.vintageHandCompatProgram = null;
            this.vintageHandCompatFramebufferBeforeTranslucent = null;
            this.vintageHandCompatFramebufferAfterTranslucent = null;
            this.vintageHandCompatBlendOverride = null;
            this.vintageHandCompatBufferBlendOverrides = Collections.emptyList();
            IRIS_LOGGER.warn("Failed to create the 1.12 legacy hand compatibility shader. First-person held items will use vanilla rendering for this shader pack.", e);
        }
    }

    private void createVintageLineProgram() {
        ProgramSource source = this.resolver.resolve(ProgramId.Basic).orElse(null);
        if (source == null || !source.isValid()) {
            return;
        }

        try {
            int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);
            ProgramBuilder builder = ProgramBuilder.begin(
                    source.getName() + "_celeritas_line",
                    source.getSourceNullable(ShaderType.VERTEX),
                    source.getSourceNullable(ShaderType.GEOMETRY),
                    source.getSourceNullable(ShaderType.FRAGMENT),
                    IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);

            CommonUniforms.addCommonUniforms(builder, this.pack.getIdMap(), this.packDirectives, this.updateNotifier, FogMode.PER_VERTEX);
            this.customUniforms.assignTo(builder);
            this.addGbufferOrShadowSamplers(builder, builder, () -> this.isBeforeTranslucent ? this.flippedAfterPrepare : this.flippedAfterTranslucent,
                    false, true, true, false);

            this.vintageLineProgram = builder.build();
            this.customUniforms.mapholderToPass(builder, this.vintageLineProgram);
            this.vintageLineFramebufferBeforeTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterPrepare, drawBuffers);
            this.vintageLineFramebufferAfterTranslucent = this.renderTargets.createGbufferFramebuffer(this.flippedAfterTranslucent, drawBuffers);
            this.vintageLineBlendOverride = source.getDirectives().getBlendModeOverride().orElse(ProgramId.Basic.getBlendModeOverride());
            this.vintageLineBufferBlendOverrides = this.celeritas$createBufferBlendOverrides(source);
        } catch (RuntimeException e) {
            if (this.vintageLineProgram != null) {
                this.vintageLineProgram.delete();
            }

            this.vintageLineProgram = null;
            this.vintageLineFramebufferBeforeTranslucent = null;
            this.vintageLineFramebufferAfterTranslucent = null;
            this.vintageLineBlendOverride = null;
            this.vintageLineBufferBlendOverrides = Collections.emptyList();
            IRIS_LOGGER.warn("Failed to create the 1.12 selection outline shader bridge. Selection outlines will use the active vanilla render state.", e);
        }
    }

    private void celeritas$addLegacyCompatibilityUniforms(ProgramBuilder builder) {
        builder.uniform1f(UniformUpdateFrequency.PER_FRAME, "celeritasDaylight", this::celeritas$getDaylightFactor);
        builder.uniform1b(UniformUpdateFrequency.PER_FRAME, "celeritasNoSkylightDimension", this::celeritas$isNoSkylightDimension);
        builder.uniformMatrix(UniformUpdateFrequency.PER_FRAME, "gbufferModelViewInverse",
                () -> new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView()).invert());
    }

    private float celeritas$getDaylightFactor() {
        float skyAngle = MINECRAFT_SHIM.getSkyAngle();
        float daylight = 1.0F - ((float) Math.cos(skyAngle * Math.PI * 2.0D) * 2.0F + 0.2F);
        daylight = 1.0F - this.celeritas$clamp01(daylight);
        return this.celeritas$clamp01(daylight);
    }

    private float celeritas$clamp01(float value) {
        if (value < 0.0F) {
            return 0.0F;
        }
        if (value > 1.0F) {
            return 1.0F;
        }

        return value;
    }

    private boolean celeritas$isNoSkylightDimension() {
        Minecraft client = Minecraft.getMinecraft();
        return client.world != null && client.world.provider != null && !client.world.provider.hasSkyLight();
    }

    private int[] celeritas$drawBuffersOrDefault(ProgramSource source) {
        int[] drawBuffers = source.getDirectives().getDrawBuffers();
        return drawBuffers.length == 0 ? new int[] {0} : drawBuffers;
    }

    private int[] celeritas$mergeDrawBuffers(int[] primary, @Nullable ProgramSource secondarySource) {
        List<Integer> merged = new ArrayList<>();
        for (int drawBuffer : primary) {
            if (!merged.contains(drawBuffer)) {
                merged.add(drawBuffer);
            }
        }

        if (secondarySource != null && secondarySource.isValid()) {
            for (int drawBuffer : this.celeritas$drawBuffersOrDefault(secondarySource)) {
                if (!merged.contains(drawBuffer)) {
                    merged.add(drawBuffer);
                }
            }
        }

        int[] drawBuffers = new int[merged.size()];
        for (int i = 0; i < merged.size(); i++) {
            drawBuffers[i] = merged.get(i);
        }

        return drawBuffers;
    }

    private boolean celeritas$prefersShaderPackEntityProgram(ProgramSource source) {
        int[] drawBuffers = this.celeritas$drawBuffersOrDefault(source);

        // Legacy models do not provide tangent/PBR attributes. The pack program can turn
        // their normals into NaNs or black pixels, so use the compatibility program.
        return this.vintageEntityProgram != null && this.vintageEntityFramebuffer != null
                && !this.celeritas$isPbrFresnelLayout(source, drawBuffers);
    }

    private boolean celeritas$isColorAndAuxBufferLayout(int[] drawBuffers) {
        return drawBuffers.length == 2 && drawBuffers[0] == 0 && drawBuffers[1] == 3;
    }

    private boolean celeritas$isPbrFresnelLayout(ProgramSource source, int[] drawBuffers) {
        if (drawBuffers.length != 4 || drawBuffers[0] != 0 || drawBuffers[1] != 3
                || drawBuffers[2] != 6 || drawBuffers[3] != 7) {
            return false;
        }

        String fragmentSource = source.getSourceNullable(ShaderType.FRAGMENT);
        if (fragmentSource == null) {
            return false;
        }

        String lowerSource = fragmentSource.toLowerCase(Locale.ROOT);
        return lowerSource.contains("fresnel")
                && lowerSource.contains("encodenormal")
                && lowerSource.contains("gl_fragdata[3]");
    }

    private List<BufferBlendOverride> celeritas$createBufferBlendOverrides(ProgramSource source) {
        return this.celeritas$createBufferBlendOverrides(source, this.celeritas$drawBuffersOrDefault(source));
    }

    private List<BufferBlendOverride> celeritas$createBufferBlendOverrides(ProgramSource source, int[] drawBuffers) {
        List<BufferBlendOverride> bufferOverrides = new ArrayList<>();
        source.getDirectives().getBufferBlendOverrides().forEach(information -> {
            int index = Ints.indexOf(drawBuffers, information.index());
            if (index > -1) {
                bufferOverrides.add(new BufferBlendOverride(index, information.blendMode()));
            }
        });

        return bufferOverrides;
    }

    private void celeritas$applyBlendOverrides(@Nullable BlendModeOverride blendOverride, List<BufferBlendOverride> bufferBlendOverrides) {
        if (blendOverride != null) {
            blendOverride.apply();
        }

        if (!bufferBlendOverrides.isEmpty()) {
            bufferBlendOverrides.forEach(BufferBlendOverride::apply);
        }
    }

    private void celeritas$restoreBlendOverrides(@Nullable BlendModeOverride blendOverride, List<BufferBlendOverride> bufferBlendOverrides) {
        if (blendOverride != null || !bufferBlendOverrides.isEmpty()) {
            BlendModeOverride.restore();
        }
    }

    private String celeritas$getLegacyCompatibilityVertexSource() {
        return "#version 130\n"
                + "out vec2 vTexCoord;\n"
                + "out vec2 vLightCoord;\n"
                + "out vec4 vColor;\n"
                + "out vec3 vNormal;\n"
                + "void main() {\n"
                + "    gl_Position = ftransform();\n"
                + "    vTexCoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;\n"
                + "    vec2 matrixLightCoord = (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy;\n"
                + "    vec2 rawLightCoord = gl_MultiTexCoord1.xy * 0.00390625 + vec2(0.03125);\n"
                + "    vLightCoord = all(lessThanEqual(abs(matrixLightCoord), vec2(1.5))) ? matrixLightCoord : rawLightCoord;\n"
                + "    vColor = gl_Color;\n"
                + "    vNormal = normalize(gl_NormalMatrix * gl_Normal);\n"
                + "}\n";
    }

    private String celeritas$getLegacyCompatibilityFragmentSource(int[] drawBuffers, boolean entityPass, boolean clampEntityLight, boolean directLightmapColor, boolean attenuateSkyLight, boolean floorNoSkylightEntityLight) {
        return this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, entityPass, clampEntityLight, directLightmapColor, attenuateSkyLight, floorNoSkylightEntityLight, false, false, false);
    }

    private String celeritas$getLegacyCompatibilityFragmentSource(int[] drawBuffers, boolean entityPass, boolean clampEntityLight, boolean directLightmapColor, boolean attenuateSkyLight, boolean floorNoSkylightEntityLight, boolean clearTranslucentAux) {
        return this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, entityPass, clampEntityLight, directLightmapColor, attenuateSkyLight, floorNoSkylightEntityLight, clearTranslucentAux, false, false);
    }

    private String celeritas$getLegacyCompatibilityFragmentSource(int[] drawBuffers, boolean entityPass, boolean clampEntityLight, boolean directLightmapColor, boolean attenuateSkyLight, boolean floorNoSkylightEntityLight, boolean clearTranslucentAux, boolean capBlockLight) {
        return this.celeritas$getLegacyCompatibilityFragmentSource(drawBuffers, entityPass, clampEntityLight, directLightmapColor, attenuateSkyLight, floorNoSkylightEntityLight, clearTranslucentAux, capBlockLight, false);
    }

    private String celeritas$getLegacyCompatibilityFragmentSource(int[] drawBuffers, boolean entityPass, boolean clampEntityLight, boolean directLightmapColor, boolean attenuateSkyLight, boolean floorNoSkylightEntityLight, boolean clearTranslucentAux, boolean capBlockLight, boolean pbrFresnelBlockLayout) {
        StringBuilder source = new StringBuilder();
        source.append("#version 130\n")
                .append("uniform sampler2D tex;\n")
                .append("uniform sampler2D lightmap;\n")
                .append("uniform float celeritasDaylight;\n")
                .append("uniform bool celeritasNoSkylightDimension;\n")
                .append(entityPass ? "uniform vec4 entityColor;\n" : "")
                .append(pbrFresnelBlockLayout ? "uniform mat4 gbufferModelViewInverse;\n" : "")
                .append("const bool celeritasClampEntityLight = ").append(clampEntityLight ? "true" : "false").append(";\n")
                .append("const bool celeritasDirectLightmapColor = ").append(directLightmapColor ? "true" : "false").append(";\n")
                .append("const bool celeritasAttenuateSkyLight = ").append(attenuateSkyLight ? "true" : "false").append(";\n")
                .append("const bool celeritasFloorNoSkylightEntityLight = ").append(floorNoSkylightEntityLight ? "true" : "false").append(";\n")
                .append("const bool celeritasCapBlockLight = ").append(capBlockLight ? "true" : "false").append(";\n")
                .append("in vec2 vTexCoord;\n")
                .append("in vec2 vLightCoord;\n")
                .append("in vec4 vColor;\n")
                .append("in vec3 vNormal;\n")
                .append("void main() {\n")
                .append("    vec4 rawColor = texture2D(tex, vTexCoord) * vColor;\n")
                .append("    vec4 color = rawColor;\n")
                .append("    if (color.a <= 0.001) discard;\n")
                .append("    vec2 lmFactor = clamp((vLightCoord - vec2(0.03125)) * 1.06667, vec2(0.0), vec2(1.0));\n")
                .append("    float compatBlockLight = celeritasClampEntityLight ? min(lmFactor.x, 0.9) : lmFactor.x;\n")
                .append("    if (celeritasCapBlockLight) compatBlockLight = min(compatBlockLight, 0.9333);\n")
                .append("    float compatSkyLight = lmFactor.y * (celeritasAttenuateSkyLight ? celeritasDaylight : 1.0);\n")
                .append("    if (celeritasFloorNoSkylightEntityLight && celeritasNoSkylightDimension) compatSkyLight = max(compatSkyLight, 0.66667);\n")
                .append("    float compatLight = max(compatBlockLight, compatSkyLight);\n")
                .append("    vec2 compatLightCoord = vec2(0.03125 + compatBlockLight * 0.9375, 0.03125 + compatSkyLight * 0.9375);\n")
                .append("    vec3 lightColor = texture2D(lightmap, clamp(compatLightCoord, vec2(0.0), vec2(1.0))).rgb;\n")
                .append("    vec3 compatLighting = celeritasDirectLightmapColor ? lightColor : lightColor * (0.18 + 0.82 * compatLight);\n");

        if (entityPass) {
            source.append("    color.rgb = mix(color.rgb, entityColor.rgb, entityColor.a);\n")
                    // Match the two directional lights and ambient term used by
                    // Minecraft 1.12's fixed-function living-entity renderer.
                    .append("    vec3 compatNormal = normalize(vNormal);\n")
                    .append("    float compatDiffuse0 = max(dot(compatNormal, normalize(vec3(0.2, 1.0, -0.7))), 0.0);\n")
                    .append("    float compatDiffuse1 = max(dot(compatNormal, normalize(vec3(-0.2, 1.0, 0.7))), 0.0);\n")
                    .append("    float compatDirectional = clamp(0.4 + 0.6 * compatDiffuse0 + 0.6 * compatDiffuse1, 0.0, 1.0);\n")
                    .append("    compatLighting *= compatDirectional;\n");
        }

        source
                .append("    color.rgb *= max(compatLighting, vec3(0.015));\n")
                .append("    vec3 translucentMult = mix(vec3(0.666), color.rgb * (1.0 - pow(color.a, 4.0)), color.a);\n");

        for (int i = 0; i < drawBuffers.length; i++) {
            source.append("    gl_FragData[").append(i).append("] = ")
                    .append(this.celeritas$getLegacyCompatibilityOutput(drawBuffers[i], drawBuffers, entityPass, clearTranslucentAux, pbrFresnelBlockLayout))
                    .append(";\n");
        }

        source.append("}\n");
        return source.toString();
    }

    private String celeritas$getLegacyCompatibilityOutput(int drawBuffer, int[] drawBuffers, boolean entityPass, boolean clearTranslucentAux, boolean pbrFresnelBlockLayout) {
        boolean hasRawAlbedoBuffer = Ints.contains(drawBuffers, 1);
        boolean hasWorldNormalBuffer = Ints.contains(drawBuffers, 4);
        boolean hasMaterialBuffer = Ints.contains(drawBuffers, 6);
        boolean hasEntityMaskBuffer = Ints.contains(drawBuffers, 7);

        boolean oldComplementaryLayout = hasRawAlbedoBuffer || hasEntityMaskBuffer;
        boolean complementaryUnboundLayout = hasWorldNormalBuffer && hasMaterialBuffer && !oldComplementaryLayout;
        boolean materialOnlyTranslucencyLayout = hasMaterialBuffer && Ints.contains(drawBuffers, 3)
                && !hasRawAlbedoBuffer && !hasWorldNormalBuffer && !hasEntityMaskBuffer;
        boolean colorAndAuxBufferLayout = this.celeritas$isColorAndAuxBufferLayout(drawBuffers);

        switch (drawBuffer) {
            case 0:
                return "color";
            case 1:
                return "vec4(rawColor.rgb, color.a)";
            case 3:
                if (clearTranslucentAux) {
                    return "vec4(0.0, 0.0, 0.0, 1.0)";
                }
                if (pbrFresnelBlockLayout) {
                    // This PBR contract stores smoothness and half-range sky light in colortex3.
                    return "vec4(0.0, compatSkyLight * 0.5, 0.0, 1.0)";
                }
                if (complementaryUnboundLayout || materialOnlyTranslucencyLayout) {
                    return "vec4(1.0 - translucentMult, 1.0)";
                }
                if (oldComplementaryLayout) {
                    return "vec4(0.0, 0.0, compatSkyLight, 1.0)";
                }
                if (colorAndAuxBufferLayout) {
                    return entityPass ? "vec4(0.0, 0.0, 0.0, 1.0)" : "vec4(normalize(vNormal).xy * 0.5 + 0.5, 0.0, 1.0)";
                }
                return "vec4(normalize(vNormal).xy * 0.5 + 0.5, compatSkyLight * 0.5, 0.0)";
            case 4:
                return "vec4(normalize(vNormal), 1.0)";
            case 6:
                if (pbrFresnelBlockLayout) {
                    // Lumina's 0/3/6/7 PBR layout stores smoothness, material mask,
                    // and sky light here. Treat legacy entities as non-reflective
                    // instead of accidentally decoding a normal as a metal material.
                    return "vec4(0.0, 0.0, compatSkyLight, 1.0)";
                }
                if (oldComplementaryLayout) {
                    return "vec4(normalize(vNormal).xy * 0.5 + 0.5, 0.0, 1.0)";
                }
                return "vec4(0.0, 0.0, compatSkyLight, 1.0)";
            case 7:
                if (pbrFresnelBlockLayout) {
                    // This layout expects a world-space normal. vNormal is in view
                    // space, so undo only the camera transform used by the gbuffer.
                    return "vec4(normalize(mat3(gbufferModelViewInverse) * vNormal), 1.0)";
                }
                return "vec4(1.0)";
            default:
                return "vec4(0.0, 0.0, 0.0, 1.0)";
        }
    }

    public boolean beginVintageEntityRendering() {
        if (this.vintageEntityPreferShaderProgram && this.celeritas$beginVintageShaderPackEntityRendering()) {
            return true;
        }

        if (this.vintageEntityCompatProgram != null && this.vintageEntityCompatFramebuffer != null) {
            if (!this.vintageEntityCompatBridgeLogged) {
                IRIS_LOGGER.info("Using the 1.12 legacy entity compatibility shader bridge.");
                this.vintageEntityCompatBridgeLogged = true;
            }

            this.removePhaseIfNeeded();
            this.vintageEntityCompatFramebuffer.bind();
            GbufferPrograms.beginEntities();
            GbufferPrograms.runPhaseChangeNotifier();
            this.celeritas$applyBlendOverrides(this.vintageEntityCompatBlendOverride, this.vintageEntityCompatBufferBlendOverrides);
            this.vintageEntityCompatProgram.use();
            this.bindVintageEntityLightmap();
            this.vintageEntityCompatRenderingActive = true;
            return true;
        }

        return this.celeritas$beginVintageShaderPackEntityRendering();
    }

    private boolean celeritas$beginVintageShaderPackEntityRendering() {
        if (this.vintageEntityProgram == null || this.vintageEntityFramebuffer == null) {
            return false;
        }

        if (!this.vintageEntityShaderBridgeLogged) {
            IRIS_LOGGER.info("Using the shader pack entity program for the 1.12 entity bridge.");
            this.vintageEntityShaderBridgeLogged = true;
        }

        this.removePhaseIfNeeded();
        this.vintageEntityFramebuffer.bind();
        GbufferPrograms.beginEntities();
        GbufferPrograms.runPhaseChangeNotifier();
        this.celeritas$applyBlendOverrides(this.vintageEntityBlendOverride, this.vintageEntityBufferBlendOverrides);

        this.vintageEntityProgram.use();
        this.bindVintageEntityLightmap();
        this.customUniforms.push(this.vintageEntityProgram);
        this.vintageEntityRenderingActive = true;
        return true;
    }

    public void updateVintageEntityUniforms(@Nullable Entity entity) {
        if (this.vintageEntityCompatRenderingActive) {
            this.vintageEntityCompatProgram.use();
            this.bindVintageEntityLightmap();
            this.celeritas$updateVintageEntityColor(entity, this.vintageEntityCompatColorUniformLocation);
            return;
        }

        if (this.vintageEntityRenderingActive) {
            this.vintageEntityProgram.use();
            this.bindVintageEntityLightmap();
            GbufferPrograms.runFallbackEntityListener();
            this.celeritas$updateVintageEntityColor(entity, this.vintageEntityColorUniformLocation);
        }
    }

    private void celeritas$updateVintageEntityColor(@Nullable Entity entity, int uniformLocation) {
        if (uniformLocation < 0) {
            return;
        }

        // Minecraft 1.12 implements the living-entity damage flash with the
        // fixed-function texture combiner. Shader-pack entity programs bypass that
        // combiner and instead expect OptiFine's entityColor uniform. Mirror the
        // exact vanilla hurt/death overlay here so packs such as Lumina receive it.
        if (entity instanceof EntityLivingBase living
                && (living.hurtTime > 0 || living.deathTime > 0)) {
            IrisRenderSystem.uniform4f(uniformLocation,
                    1.0F, 0.0F, 0.0F, 0.3F);
        } else {
            IrisRenderSystem.uniform4f(uniformLocation,
                    0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    public boolean isVintageEntityCompatibilityRenderingActive() {
        return this.vintageEntityCompatRenderingActive;
    }

    public boolean shouldClampVintageEntityLowerBodySkyLight() {
        return this.vintageEntityRenderingActive && this.vintageEntityClampLowerBodySkyLight;
    }

    public boolean beginVintageEntityFallbackRendering() {
        if (this.vintageEntityFallbackFramebuffer == null) {
            return false;
        }

        this.removePhaseIfNeeded();
        this.vintageEntityFallbackFramebuffer.bind();
        GbufferPrograms.beginEntities();
        GbufferPrograms.runPhaseChangeNotifier();
        Program.unbind();
        return true;
    }

    public void endVintageEntityFallbackRendering() {
        GbufferPrograms.endEntities();
        GbufferPrograms.runPhaseChangeNotifier();
        this.bindDefault();
    }

    private void bindVintageEntityLightmap() {
        IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, MINECRAFT_SHIM.getLightTextureId());
    }

    public boolean beginVintageBlockEntityRendering() {
        if (this.vintageBlockEntityCompatProgram == null) {
            return false;
        }

        GlFramebuffer framebuffer = this.isBeforeTranslucent
                ? this.vintageBlockEntityCompatFramebufferBeforeTranslucent
                : this.vintageBlockEntityCompatFramebufferAfterTranslucent;

        if (framebuffer == null) {
            return false;
        }

        if (!this.vintageBlockEntityBridgeLogged) {
            IRIS_LOGGER.info(this.vintageBlockEntityUsesShaderPackProgram
                    ? "Using the shader pack block program for the 1.12 block entity bridge."
                    : "Using the generic 1.12 block entity compatibility shader bridge.");
            this.vintageBlockEntityBridgeLogged = true;
        }

        this.removePhaseIfNeeded();
        framebuffer.bind();
        GbufferPrograms.beginBlockEntities();
        GbufferPrograms.runPhaseChangeNotifier();
        this.celeritas$applyBlendOverrides(this.vintageBlockEntityCompatBlendOverride, this.vintageBlockEntityCompatBufferBlendOverrides);
        this.vintageBlockEntityCompatProgram.use();
        this.bindVintageEntityLightmap();
        this.vintageBlockEntityCompatRenderingActive = true;
        return true;
    }

    public void endVintageBlockEntityRendering() {
        if (!this.vintageBlockEntityCompatRenderingActive) {
            return;
        }

        Program.unbind();
        this.celeritas$restoreBlendOverrides(this.vintageBlockEntityCompatBlendOverride, this.vintageBlockEntityCompatBufferBlendOverrides);
        GbufferPrograms.endBlockEntities();
        GbufferPrograms.runPhaseChangeNotifier();
        this.vintageBlockEntityCompatRenderingActive = false;
        this.bindDefault();
    }

    public void updateVintageBlockEntityUniforms() {
        if (!this.vintageBlockEntityCompatRenderingActive || this.vintageBlockEntityCompatProgram == null) {
            return;
        }

        GlFramebuffer framebuffer = this.isBeforeTranslucent
                ? this.vintageBlockEntityCompatFramebufferBeforeTranslucent
                : this.vintageBlockEntityCompatFramebufferAfterTranslucent;

        if (framebuffer != null) {
            framebuffer.bind();
        }

        this.celeritas$applyBlendOverrides(this.vintageBlockEntityCompatBlendOverride, this.vintageBlockEntityCompatBufferBlendOverrides);
        this.vintageBlockEntityCompatProgram.use();
        this.bindVintageEntityLightmap();
    }

    public boolean beginVintageParticleRendering() {
        if (this.vintageParticleCompatProgram == null) {
            return false;
        }

        GlFramebuffer framebuffer = this.isBeforeTranslucent
                ? this.vintageParticleCompatFramebufferBeforeTranslucent
                : this.vintageParticleCompatFramebufferAfterTranslucent;

        if (framebuffer == null) {
            return false;
        }

        this.removePhaseIfNeeded();
        framebuffer.bind();
        this.setPhase(WorldRenderingPhase.PARTICLES);
        GbufferPrograms.runPhaseChangeNotifier();
        this.celeritas$applyBlendOverrides(this.vintageParticleCompatBlendOverride, this.vintageParticleCompatBufferBlendOverrides);
        this.vintageParticleCompatProgram.use();
        this.bindVintageEntityLightmap();
        this.vintageParticleCompatRenderingActive = true;
        return true;
    }

    public void endVintageParticleRendering() {
        if (!this.vintageParticleCompatRenderingActive) {
            return;
        }

        Program.unbind();
        this.celeritas$restoreBlendOverrides(this.vintageParticleCompatBlendOverride, this.vintageParticleCompatBufferBlendOverrides);
        this.setPhase(WorldRenderingPhase.NONE);
        GbufferPrograms.runPhaseChangeNotifier();
        this.vintageParticleCompatRenderingActive = false;
        this.bindDefault();
    }

    public boolean beginVintageHandRendering() {
        if (this.vintageHandCompatProgram == null) {
            return false;
        }

        GlFramebuffer framebuffer = this.isBeforeTranslucent
                ? this.vintageHandCompatFramebufferBeforeTranslucent
                : this.vintageHandCompatFramebufferAfterTranslucent;

        if (framebuffer == null) {
            return false;
        }

        if (!this.vintageHandCompatBridgeLogged) {
            IRIS_LOGGER.info("Using the 1.12 legacy first-person hand compatibility shader bridge.");
            this.vintageHandCompatBridgeLogged = true;
        }

        this.beginHand();
        this.removePhaseIfNeeded();
        framebuffer.bind();
        this.setPhase(this.isBeforeTranslucent ? WorldRenderingPhase.HAND_SOLID : WorldRenderingPhase.HAND_TRANSLUCENT);
        GbufferPrograms.runPhaseChangeNotifier();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        this.celeritas$applyBlendOverrides(this.vintageHandCompatBlendOverride, this.vintageHandCompatBufferBlendOverrides);
        this.vintageHandCompatProgram.use();
        this.bindVintageEntityLightmap();
        this.vintageHandCompatRenderingActive = true;
        return true;
    }

    public void endVintageHandRendering() {
        if (!this.vintageHandCompatRenderingActive) {
            return;
        }

        Program.unbind();
        this.celeritas$restoreBlendOverrides(this.vintageHandCompatBlendOverride, this.vintageHandCompatBufferBlendOverrides);
        this.setPhase(WorldRenderingPhase.NONE);
        GbufferPrograms.runPhaseChangeNotifier();
        this.vintageHandCompatRenderingActive = false;
        this.bindDefault();
    }

    public boolean beginVintageLineRendering() {
        if (this.vintageLineProgram == null) {
            return false;
        }

        GlFramebuffer framebuffer = this.isBeforeTranslucent
                ? this.vintageLineFramebufferBeforeTranslucent
                : this.vintageLineFramebufferAfterTranslucent;

        if (framebuffer == null) {
            return false;
        }

        if (!this.vintageLineBridgeLogged) {
            IRIS_LOGGER.info("Using the shader pack basic program for the 1.12 selection outline bridge.");
            this.vintageLineBridgeLogged = true;
        }

        this.removePhaseIfNeeded();
        framebuffer.bind();
        GbufferPrograms.beginOutline();
        GbufferPrograms.runPhaseChangeNotifier();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        this.celeritas$applyBlendOverrides(this.vintageLineBlendOverride, this.vintageLineBufferBlendOverrides);
        this.vintageLineProgram.use();
        this.bindVintageEntityLightmap();
        this.customUniforms.push(this.vintageLineProgram);
        this.vintageLineRenderingActive = true;
        return true;
    }

    public void endVintageLineRendering() {
        if (!this.vintageLineRenderingActive) {
            return;
        }

        Program.unbind();
        this.celeritas$restoreBlendOverrides(this.vintageLineBlendOverride, this.vintageLineBufferBlendOverrides);
        GbufferPrograms.endOutline();
        GbufferPrograms.runPhaseChangeNotifier();
        this.vintageLineRenderingActive = false;
        this.bindDefault();
    }

    public boolean beginVintageBeaconBeamRendering() {
        if (this.vintageBeaconBeamProgram == null) {
            return false;
        }

        GlFramebuffer framebuffer = this.isBeforeTranslucent
                ? this.vintageBeaconBeamFramebufferBeforeTranslucent
                : this.vintageBeaconBeamFramebufferAfterTranslucent;

        if (framebuffer == null) {
            return false;
        }

        if (!this.vintageBeaconBeamBridgeLogged) {
            IRIS_LOGGER.info("Using the shader pack beacon beam program for the 1.12 End crystal beam bridge.");
            this.vintageBeaconBeamBridgeLogged = true;
        }

        framebuffer.bind();
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        this.celeritas$applyBlendOverrides(this.vintageBeaconBeamBlendOverride, this.vintageBeaconBeamBufferBlendOverrides);
        this.vintageBeaconBeamProgram.use();
        this.bindVintageEntityLightmap();
        this.customUniforms.push(this.vintageBeaconBeamProgram);
        this.vintageBeaconBeamRenderingActive = true;
        return true;
    }

    public void endVintageBeaconBeamRendering() {
        if (!this.vintageBeaconBeamRenderingActive) {
            return;
        }

        Program.unbind();
        this.celeritas$restoreBlendOverrides(this.vintageBeaconBeamBlendOverride, this.vintageBeaconBeamBufferBlendOverrides);
        this.vintageBeaconBeamRenderingActive = false;
        this.celeritas$restoreActiveVintageRenderProgram();
    }

    private void celeritas$restoreActiveVintageRenderProgram() {
        if (this.vintageEntityCompatRenderingActive && this.vintageEntityCompatFramebuffer != null) {
            this.vintageEntityCompatFramebuffer.bind();
            this.vintageEntityCompatProgram.use();
            this.bindVintageEntityLightmap();
            return;
        }

        if (this.vintageEntityRenderingActive && this.vintageEntityFramebuffer != null) {
            this.vintageEntityFramebuffer.bind();
            this.vintageEntityProgram.use();
            this.bindVintageEntityLightmap();
            this.customUniforms.push(this.vintageEntityProgram);
            GbufferPrograms.runFallbackEntityListener();
            return;
        }

        if (this.vintageBlockEntityCompatRenderingActive) {
            GlFramebuffer framebuffer = this.isBeforeTranslucent
                    ? this.vintageBlockEntityCompatFramebufferBeforeTranslucent
                    : this.vintageBlockEntityCompatFramebufferAfterTranslucent;

            if (framebuffer != null) {
                framebuffer.bind();
                this.vintageBlockEntityCompatProgram.use();
                this.bindVintageEntityLightmap();
                return;
            }
        }

        if (this.vintageParticleCompatRenderingActive) {
            GlFramebuffer framebuffer = this.isBeforeTranslucent
                    ? this.vintageParticleCompatFramebufferBeforeTranslucent
                    : this.vintageParticleCompatFramebufferAfterTranslucent;

            if (framebuffer != null) {
                framebuffer.bind();
                this.vintageParticleCompatProgram.use();
                this.bindVintageEntityLightmap();
                return;
            }
        }

        if (this.vintageHandCompatRenderingActive) {
            GlFramebuffer framebuffer = this.isBeforeTranslucent
                    ? this.vintageHandCompatFramebufferBeforeTranslucent
                    : this.vintageHandCompatFramebufferAfterTranslucent;

            if (framebuffer != null) {
                framebuffer.bind();
                this.vintageHandCompatProgram.use();
                this.bindVintageEntityLightmap();
                return;
            }
        }

        if (this.vintageLineRenderingActive) {
            GlFramebuffer framebuffer = this.isBeforeTranslucent
                    ? this.vintageLineFramebufferBeforeTranslucent
                    : this.vintageLineFramebufferAfterTranslucent;

            if (framebuffer != null) {
                framebuffer.bind();
                this.vintageLineProgram.use();
                this.bindVintageEntityLightmap();
                this.customUniforms.push(this.vintageLineProgram);
                return;
            }
        }

        this.bindDefault();
    }

    public void endVintageEntityRendering() {
        if (this.vintageEntityCompatRenderingActive) {
            Program.unbind();
            this.celeritas$restoreBlendOverrides(this.vintageEntityCompatBlendOverride, this.vintageEntityCompatBufferBlendOverrides);
            GbufferPrograms.endEntities();
            GbufferPrograms.runPhaseChangeNotifier();
            this.vintageEntityCompatRenderingActive = false;
            this.bindDefault();
            return;
        }

        if (!this.vintageEntityRenderingActive) {
            return;
        }

        Program.unbind();
        this.celeritas$restoreBlendOverrides(this.vintageEntityBlendOverride, this.vintageEntityBufferBlendOverrides);

        GbufferPrograms.endEntities();
        GbufferPrograms.runPhaseChangeNotifier();
        this.vintageEntityRenderingActive = false;
        this.bindDefault();
    }

    @Override
    protected ShaderKey[] getShaderKeyValues() {
        // Terrain/entity shader replacement needs a 1.12 MCShaderInstance bridge first.
        return NO_SHADER_KEYS;
    }

    @Override
    protected CompletableFuture<MCShaderInstance> createShader(String name, Executor syncExecutor, ProgramSource source, ProgramId programId,
            AlphaTest fallbackAlpha, MCVertexFormat vertexFormat, FogMode fogMode, boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText)
            throws IOException {
        throw new UnsupportedOperationException("1.12 MCShaderInstance creation is not implemented yet");
    }

    @Override
    protected MCShaderInstance createFallbackShader(String name, ShaderKey key) throws IOException {
        throw new UnsupportedOperationException("1.12 fallback MCShaderInstance creation is not implemented yet");
    }

    @Override
    protected MCShaderInstance createFallbackShadowShader(String name, ShaderKey key) throws IOException {
        throw new UnsupportedOperationException("1.12 fallback shadow MCShaderInstance creation is not implemented yet");
    }

    @Override
    protected CompletableFuture<MCShaderInstance> createShadowShader(String name, Executor syncExecutor, ProgramSource source, ProgramId programId,
            AlphaTest fallbackAlpha, MCVertexFormat vertexFormat, boolean isIntensity, boolean isFullbright, boolean isText) throws IOException {
        throw new UnsupportedOperationException("1.12 shadow MCShaderInstance creation is not implemented yet");
    }

    @Override
    public RenderTargetStateListener getRenderTargetStateListener() {
        return this;
    }

    @Override
    public void destroy() {
        if (this.vintageEntityProgram != null) {
            this.vintageEntityProgram.delete();
            this.vintageEntityProgram = null;
        }

        if (this.vintageEntityCompatProgram != null) {
            this.vintageEntityCompatProgram.delete();
            this.vintageEntityCompatProgram = null;
        }

        if (this.vintageBlockEntityCompatProgram != null) {
            this.vintageBlockEntityCompatProgram.delete();
            this.vintageBlockEntityCompatProgram = null;
        }

        if (this.vintageParticleCompatProgram != null) {
            this.vintageParticleCompatProgram.delete();
            this.vintageParticleCompatProgram = null;
        }

        if (this.vintageBeaconBeamProgram != null) {
            this.vintageBeaconBeamProgram.delete();
            this.vintageBeaconBeamProgram = null;
        }

        if (this.vintageHandCompatProgram != null) {
            this.vintageHandCompatProgram.delete();
            this.vintageHandCompatProgram = null;
        }

        if (this.vintageLineProgram != null) {
            this.vintageLineProgram.delete();
            this.vintageLineProgram = null;
        }

        super.destroy();
    }
}
