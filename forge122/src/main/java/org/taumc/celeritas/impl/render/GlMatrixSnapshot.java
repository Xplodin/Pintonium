package org.taumc.celeritas.impl.render;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * Captures the fixed-function matrices which are active for the current render pass.
 */
public record GlMatrixSnapshot(Matrix4f projection, Matrix4f modelView) {
    private static final FloatBuffer PROJECTION_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer RESTORE_BUFFER = BufferUtils.createFloatBuffer(16);
    private static GlMatrixSnapshot mainCamera;
    private static boolean renderingShadowPass;

    public static GlMatrixSnapshot capture() {
        PROJECTION_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION_BUFFER);

        MODEL_VIEW_BUFFER.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);

        return new GlMatrixSnapshot(
                new Matrix4f(PROJECTION_BUFFER),
                new Matrix4f(MODEL_VIEW_BUFFER)
        );
    }

    public static GlMatrixSnapshot captureMainCamera() {
        GlMatrixSnapshot matrices = capture();
        mainCamera = matrices;
        return matrices;
    }

    /** Loads a projection/model-view pair into Minecraft 1.12's fixed-function stacks. */
    public static void load(Matrix4f projection, Matrix4f modelView) {
        RESTORE_BUFFER.clear();
        projection.get(RESTORE_BUFFER);
        RESTORE_BUFFER.rewind();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadMatrix(RESTORE_BUFFER);

        RESTORE_BUFFER.clear();
        modelView.get(RESTORE_BUFFER);
        RESTORE_BUFFER.rewind();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(RESTORE_BUFFER);
    }

    public static GlMatrixSnapshot getMainCamera() {
        return mainCamera;
    }

    public static void clearMainCamera() {
        mainCamera = null;
    }

    public static boolean isRenderingShadowPass() {
        return renderingShadowPass;
    }

    public static void setRenderingShadowPass(boolean rendering) {
        renderingShadowPass = rendering;
    }

    /**
     * Restores this snapshot to the legacy fixed-function matrix stacks.
     *
     * Shader shadow rendering changes these matrices even though modern terrain
     * programs use uniforms. Distant Horizons 3.2 still reads the legacy stacks
     * for its CPU-side culling and camera state, so they must describe the main
     * camera again before DH begins its opaque pass.
     */
    public void restore() {
        RESTORE_BUFFER.clear();
        this.projection.get(RESTORE_BUFFER);
        RESTORE_BUFFER.rewind();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadMatrix(RESTORE_BUFFER);

        RESTORE_BUFFER.clear();
        this.modelView.get(RESTORE_BUFFER);
        RESTORE_BUFFER.rewind();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(RESTORE_BUFFER);
    }
}
