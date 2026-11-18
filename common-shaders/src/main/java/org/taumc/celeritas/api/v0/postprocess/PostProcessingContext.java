package org.taumc.celeritas.api.v0.postprocess;

import java.nio.FloatBuffer;

/**
 * Data for one callback invocation. All methods require the render thread and an active callback.
 * Textures belong to the renderer: sample them, but do not delete, reallocate, or change their parameters.
 * See docs/post-processing-api.md for the GL state and depth contracts.
 */
public interface PostProcessingContext {
    int getWidth();

    int getHeight();

    float getPartialTicks();

    /** Scene depth after world/hand rendering (depthtex0), or -1 if unavailable. Not linear depth. */
    int getFinalDepthTexture();

    /** Pre-hand scene depth (depthtex2), or -1 if unavailable. Not a definitive hand mask. */
    int getDepthTextureNoHand();

    /** Read-only direct, column-major 16-float buffer positioned at zero; an independent world projection copy. */
    FloatBuffer getProjectionMatrix();

    /** Read-only direct, column-major 16-float buffer positioned at zero, for camera-relative geometry. */
    FloatBuffer getModelViewMatrix();

    /**
     * Copies the current output to a separate texture on each call, safely readable while drawing output.
     * Includes earlier effects and earlier draws in this effect. Call before binding your input textures.
     */
    int copyColorTexture();

    /**
     * Runs in the same neutral state used on callback entry, restoring the previous documented state in
     * finally. Output is the main color image through a depth-free framebuffer, safe for sampling scene depth.
     */
    void withNeutralState(Runnable action);

    /**
     * Loads world camera matrices and binds the main framebuffer with its existing depth, enables LEQUAL
     * depth testing, and disables depth writes. Draw camera-relative geometry; blending remains disabled.
     * Do not sample getFinalDepthTexture() here: it is attached to the draw framebuffer.
     */
    void withWorldState(Runnable action);

    /**
     * Draws a quad with clip-space positions [-1,1] and UVs [0,1], using the currently bound program/textures.
     * Legacy GLSL shaders can use gl_Vertex and gl_MultiTexCoord0. Does not change programs or blending.
     */
    void drawFullscreenQuad();
}
