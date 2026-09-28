package net.irisshaders.iris.shadows;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.pipeline.CommonIrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.frustum.CommonFrustumHolder;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.irisshaders.iris.uniforms.CameraUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockRenderLayer;
import org.embeddedt.embeddium.compat.mc.MCCamera;
import org.embeddedt.embeddium.compat.mc.MCLevelRenderer;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;
import org.taumc.celeritas.impl.render.GlMatrixSnapshot;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;

import java.util.List;

/**
 * Terrain-only shadow-map renderer for the Minecraft 1.12 Pintonium bridge.
 *
 * <p>Entities and block entities are deliberately left for a later compatibility
 * pass. Opaque/cutout terrain is the essential part: without it the cleared
 * shadow map means "sun visible everywhere" and produces severe cave light leaks.</p>
 */
public class VintageShadowRenderer extends CommonShadowRenderer {
    private final CommonIrisRenderingPipeline pipeline;
    private final ShadowCompositeRenderer compositeRenderer;
    private int shadowFrame;

    public VintageShadowRenderer(CommonIrisRenderingPipeline pipeline,
                                 ProgramSource shadow,
                                 PackDirectives directives,
                                 ShadowRenderTargets shadowRenderTargets,
                                 ShadowCompositeRenderer compositeRenderer,
                                 boolean separateHardwareSamplers) {
        super(shadow, directives, shadowRenderTargets, separateHardwareSamplers);
        this.pipeline = pipeline;
        this.compositeRenderer = compositeRenderer;
        configureSamplingSettings(directives.getShadowDirectives());
    }

    @Override
    protected void initFrustumHolders() {
        this.terrainFrustumHolder = new CommonFrustumHolder();
        this.entityFrustumHolder = new CommonFrustumHolder();
    }

    @Override
    public void renderShadows(MCLevelRenderer ignoredLevelRenderer, MCCamera ignoredPlayerCamera) {
        if (IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance) == 0) {
            return;
        }

        Minecraft client = Minecraft.getMinecraft();
        CeleritasWorldRenderer renderer = CeleritasWorldRenderer.instanceNullable();
        if (client.world == null || renderer == null || renderer.getLastViewport() == null) {
            return;
        }

        GlMatrixSnapshot playerMatrices = GlMatrixSnapshot.capture();
        Viewport playerViewport = renderer.getLastViewport();
        Vector3d camera = CameraUniforms.getUnshiftedCameraPosition();

        Matrix4f modelView = VintageShadowMatrices.createModelViewMatrix(
                getShadowAngle(), this.intervalSize, this.sunPathRotation,
                camera.x, camera.y, camera.z);
        Matrix4f projection = this.fov == null
                ? VintageShadowMatrices.createOrthoMatrix(this.halfPlaneLength, this.nearPlane, this.farPlane)
                : VintageShadowMatrices.createPerspectiveMatrix(this.fov);

        MODELVIEW = new Matrix4f(modelView);
        PROJECTION = new Matrix4f(projection);
        renderDistance = this.renderDistanceMultiplier < 0.0F
                ? IrisVideoSettings.shadowDistance
                : (int) ((this.halfPlaneLength * this.renderDistanceMultiplier) / 16.0F);

        NonCullingFrustum shadowFrustum = new NonCullingFrustum();
        Viewport shadowViewport = shadowFrustum.sodium$createViewport();
        this.terrainFrustumHolder.setInfo(shadowFrustum,
                "render distance = " + (renderDistance * 16) + " blocks",
                "occlusion disabled for 1.12 shadow terrain");

        client.profiler.startSection("pintonium_shadow_terrain");
        RenderDevice.enterManagedCode();
        ACTIVE = true;
        try {
            GL11.glViewport(0, 0, this.resolution, this.resolution);
            GlMatrixSnapshot.load(projection, modelView);
            IrisRenderSystem.setShadowProjection(projection);

            renderer.getRenderSectionManager().markShadowGraphDirty();
            renderer.setupTerrain(shadowViewport,
                    net.irisshaders.iris.uniforms.CapturedRenderingState.INSTANCE.getTickDelta(),
                    ++this.shadowFrame, false, false);

            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.disableBlend();
            GlStateManager.disableCull();

            if (this.shouldRenderTerrain) {
                renderer.drawChunkLayer(BlockRenderLayer.SOLID, camera.x, camera.y, camera.z);
                renderer.drawChunkLayer(BlockRenderLayer.CUTOUT_MIPPED, camera.x, camera.y, camera.z);
                renderer.drawChunkLayer(BlockRenderLayer.CUTOUT, camera.x, camera.y, camera.z);
            }

            this.targets.copyPreTranslucentDepth();

            if (this.shouldRenderTranslucent) {
                renderer.drawChunkLayer(BlockRenderLayer.TRANSLUCENT, camera.x, camera.y, camera.z);
            }

            this.debugStringTerrain = renderer.getVisibleChunkCount() + " sections";
            generateMipmaps();
            this.pipeline.removePhaseIfNeeded();
            this.compositeRenderer.renderAll();
        } finally {
            ACTIVE = false;
            try {
                RenderDevice.exitManagedCode();
            } finally {
                renderer.restoreViewport(playerViewport);
                GlStateManager.enableCull();
                IrisRenderSystem.restorePlayerProjection();
                playerMatrices.restore();
                this.pipeline.bindDefault();
                GL11.glViewport(0, 0,
                        client.getFramebuffer().framebufferWidth,
                        client.getFramebuffer().framebufferHeight);
                client.profiler.endSection();
            }
        }
    }

    @Override
    protected String getEntitiesDebugString() {
        return "not yet rendered by the 1.12 shadow bridge";
    }

    @Override
    protected String getBlockEntitiesDebugString() {
        return "not yet rendered by the 1.12 shadow bridge";
    }

    @Override
    protected void addBuffersDebugText(List<String> messages) {
    }

    @Override
    public void destroy() {
        this.targets.destroy();
    }
}
