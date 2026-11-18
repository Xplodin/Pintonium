package net.irisshaders.iris.apiimpl;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingApi;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingContext;
import org.taumc.celeritas.impl.render.GlMatrixSnapshot;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

import static net.irisshaders.iris.IrisLogging.IRIS_LOGGER;

/** Legacy Forge implementation. All GL resources are lazy and owned independently of shader-pack targets. */
public final class VintagePostProcessing {
    public static final VintagePostProcessing INSTANCE = new VintagePostProcessing();

    private final PostProcessingCallbacks callbacks = new PostProcessingCallbacks((id, error) ->
            IRIS_LOGGER.error("Post-processing effect '" + id + "' failed (further failures suppressed)", error));
    private int colorFramebuffer;
    private int scratchVao;
    private int colorCopy;
    private int copyWidth;
    private int copyHeight;
    private boolean dispatching;

    private VintagePostProcessing() {
    }

    public PostProcessingApi getApi() {
        return this.callbacks;
    }

    public void render(int depthTexture, int noHandDepthTexture) {
        if (this.callbacks.isEmpty() || this.dispatching) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        Framebuffer target = minecraft.getFramebuffer();
        if (!minecraft.isCallingFromMinecraftThread()) return;
        render(target, depthTexture, noHandDepthTexture);
    }

    // Also used by the opt-in native smoke test without starting a Minecraft world.
    void render(Framebuffer target, int depthTexture, int noHandDepthTexture) {
        if (this.callbacks.isEmpty() || this.dispatching || target == null
                || target.framebufferWidth <= 0 || target.framebufferHeight <= 0) return;

        this.dispatching = true;
        try {
            this.callbacks.dispatch(effect -> {
                Context context = new Context(target, depthTexture, noHandDepthTexture);
                try (PostProcessingGlState ignored = new PostProcessingGlState()) {
                    ensureResources(target);
                    context.neutralState();
                    effect.accept(context);
                } finally {
                    context.active = false;
                }
            });
        } finally {
            this.dispatching = false;
        }
    }

    /** Called when a shader pipeline is destroyed; registrations survive reloads and dimension changes. */
    public void destroyResources() {
        if (this.colorFramebuffer != 0) GL30.glDeleteFramebuffers(this.colorFramebuffer);
        if (this.scratchVao != 0) GL30.glDeleteVertexArrays(this.scratchVao);
        if (this.colorCopy != 0) GL11.glDeleteTextures(this.colorCopy);
        this.colorFramebuffer = this.scratchVao = this.colorCopy = 0;
        this.copyWidth = this.copyHeight = 0;
    }

    private void ensureResources(Framebuffer target) {
        if (this.colorFramebuffer == 0) this.colorFramebuffer = GL30.glGenFramebuffers();
        if (this.scratchVao == 0) this.scratchVao = GL30.glGenVertexArrays();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.colorFramebuffer);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                GL11.GL_TEXTURE_2D, target.framebufferTexture, 0);
        GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
        GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Post-processing color framebuffer is incomplete");
        }
    }

    private final class Context implements PostProcessingContext {
        private final Framebuffer target;
        private final int depthTexture;
        private final int noHandDepthTexture;
        private final Thread renderThread = Thread.currentThread();
        private final Matrix4f projection = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferProjection());
        private final Matrix4f modelView = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());
        private final float partialTicks = CapturedRenderingState.INSTANCE.getTickDelta();
        private boolean active = true;

        private Context(Framebuffer target, int depthTexture, int noHandDepthTexture) {
            this.target = target;
            this.depthTexture = depthTexture;
            this.noHandDepthTexture = noHandDepthTexture;
        }

        private void checkActive() {
            if (!this.active || Thread.currentThread() != this.renderThread) {
                throw new IllegalStateException("Post-processing context is only valid inside its render callback");
            }
        }

        @Override
        public int getWidth() { checkActive(); return this.target.framebufferWidth; }

        @Override
        public int getHeight() { checkActive(); return this.target.framebufferHeight; }

        @Override
        public float getPartialTicks() { checkActive(); return this.partialTicks; }

        @Override
        public int getFinalDepthTexture() { checkActive(); return this.depthTexture; }

        @Override
        public int getDepthTextureNoHand() { checkActive(); return this.noHandDepthTexture; }

        @Override
        public FloatBuffer getProjectionMatrix() { checkActive(); return matrixBuffer(this.projection); }

        @Override
        public FloatBuffer getModelViewMatrix() { checkActive(); return matrixBuffer(this.modelView); }

        @Override
        public int copyColorTexture() {
            checkActive();
            try (PostProcessingGlState ignored = new PostProcessingGlState()) {
                PostProcessingGlState.activeTexture(GL13.GL_TEXTURE0);
                if (colorCopy == 0) colorCopy = GL11.glGenTextures();
                PostProcessingGlState.bindTexture(colorCopy);
                if (copyWidth != getWidth() || copyHeight != getHeight()) {
                    // Minecraft's final image is an RGBA8 texture; preserve that format in the snapshot.
                    GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                            getWidth(), getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                    copyWidth = getWidth();
                    copyHeight = getHeight();
                }
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, colorFramebuffer);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, getWidth(), getHeight());
                return colorCopy;
            }
        }

        @Override
        public void withNeutralState(Runnable action) {
            checkActive();
            Objects.requireNonNull(action, "action");
            try (PostProcessingGlState ignored = new PostProcessingGlState()) {
                neutralState();
                action.run();
            }
        }

        @Override
        public void withWorldState(Runnable action) {
            checkActive();
            Objects.requireNonNull(action, "action");
            try (PostProcessingGlState ignored = new PostProcessingGlState()) {
                neutralState();
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.target.framebufferObject);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GlMatrixSnapshot.load(this.projection, this.modelView);
                PostProcessingGlState.setEnabled(GL11.GL_DEPTH_TEST, true);
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GL11.glDepthFunc(GL11.GL_LEQUAL);
                action.run();
            }
        }

        private void neutralState() {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, colorFramebuffer);
            GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            GL11.glViewport(0, 0, getWidth(), getHeight());
            GL20.glUseProgram(0);
            GL30.glBindVertexArray(scratchVao);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            int[] disabled = {GL11.GL_DEPTH_TEST, GL11.GL_BLEND, GL11.GL_CULL_FACE, GL11.GL_ALPHA_TEST,
                    GL11.GL_LIGHTING, GL11.GL_FOG, GL11.GL_SCISSOR_TEST, GL11.GL_STENCIL_TEST,
                    GL11.GL_COLOR_LOGIC_OP, GL11.GL_POLYGON_OFFSET_FILL,
                    GL30.GL_RASTERIZER_DISCARD, GL30.GL_FRAMEBUFFER_SRGB};
            for (int capability : disabled) PostProcessingGlState.setEnabled(capability, false);
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
            GL11.glDepthRange(0, 1);
            PostProcessingGlState.depthMask(false);
            GlStateManager.colorMask(true, true, true, true);
            GL11.glColorMask(true, true, true, true);
            GlStateManager.color(1, 1, 1, 1);
            GL11.glColor4f(1, 1, 1, 1);
            GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ZERO);
            GL14.glBlendFuncSeparate(GL11.GL_ONE, GL11.GL_ZERO, GL11.GL_ONE, GL11.GL_ZERO);
            int units = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
            int fixedUnits = Math.min(8, GL11.glGetInteger(GL13.GL_MAX_TEXTURE_UNITS));
            for (int i = 0; i < units; i++) {
                if (i < 8) {
                    PostProcessingGlState.activeTexture(GL13.GL_TEXTURE0 + i);
                    PostProcessingGlState.bindTexture(0);
                    if (i < fixedUnits) PostProcessingGlState.setEnabled(GL11.GL_TEXTURE_2D, false);
                } else {
                    GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                }
                if (GL.getCapabilities().OpenGL33) GL33.glBindSampler(i, 0);
            }
            PostProcessingGlState.activeTexture(GL13.GL_TEXTURE0);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
        }

        @Override
        public void drawFullscreenQuad() {
            checkActive();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(-1, -1);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(1, -1);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(1, 1);
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(-1, 1);
            GL11.glEnd();
        }
    }

    private static FloatBuffer matrixBuffer(Matrix4f matrix) {
        FloatBuffer result = BufferUtils.createFloatBuffer(16);
        matrix.get(result);
        result.position(0);
        return result.asReadOnlyBuffer();
    }

}
