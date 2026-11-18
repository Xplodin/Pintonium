package net.irisshaders.iris.apiimpl;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;

/** Compatibility-profile state scope. Does not undo edits to GL objects or unbalanced matrix stacks. */
final class PostProcessingGlState implements AutoCloseable {
    private final int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
    private final int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
    private final int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
    private final int vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
    private final int arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
    private final int elementBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
    private final int packBuffer = GL11.glGetInteger(GL21.GL_PIXEL_PACK_BUFFER_BINDING);
    private final int unpackBuffer = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    private final FloatBuffer projection = matrix(GL11.GL_PROJECTION_MATRIX);
    private final FloatBuffer modelView = matrix(GL11.GL_MODELVIEW_MATRIX);
    private final int[] textures = new int[GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS)];
    private final int[] samplers = GL.getCapabilities().OpenGL33 ? new int[this.textures.length] : null;

    PostProcessingGlState() {
        // Attribute stacks cover legacy raster/blend/depth/stencil/color/texture and client-array state.
        // Modern bindings are saved separately; Minecraft's caches are synchronized after popping.
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_CLIENT_ALL_ATTRIB_BITS);
        for (int i = 0; i < this.textures.length; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            this.textures[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            if (this.samplers != null) {
                this.samplers[i] = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);
            }
        }
        GL13.glActiveTexture(this.activeTexture);
    }

    static void setEnabled(int capability, boolean enabled) {
        // Set both the vanilla cache and actual driver state: Iris legacy paths also use raw GL.
        switch (capability) {
            case GL11.GL_BLEND:
                if (enabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
                break;
            case GL11.GL_DEPTH_TEST:
                if (enabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
                break;
            case GL11.GL_ALPHA_TEST:
                if (enabled) GlStateManager.enableAlpha(); else GlStateManager.disableAlpha();
                break;
            case GL11.GL_CULL_FACE:
                if (enabled) GlStateManager.enableCull(); else GlStateManager.disableCull();
                break;
            case GL11.GL_LIGHTING:
                if (enabled) GlStateManager.enableLighting(); else GlStateManager.disableLighting();
                break;
            case GL11.GL_FOG:
                if (enabled) GlStateManager.enableFog(); else GlStateManager.disableFog();
                break;
            case GL11.GL_TEXTURE_2D:
                if (enabled) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
                break;
            default:
                break;
        }
        if (enabled) GL11.glEnable(capability); else GL11.glDisable(capability);
    }

    static void activeTexture(int texture) {
        GlStateManager.setActiveTexture(texture);
        GL13.glActiveTexture(texture);
    }

    static void bindTexture(int texture) {
        GlStateManager.bindTexture(texture);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
    }

    static void depthMask(boolean enabled) {
        GlStateManager.depthMask(enabled);
        GL11.glDepthMask(enabled);
    }

    @Override
    public void close() {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFramebuffer);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.drawFramebuffer);
        GL30.glBindVertexArray(this.vao);
        GL11.glPopClientAttrib();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.arrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.elementBuffer);
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, this.packBuffer);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, this.unpackBuffer);
        GL11.glPopAttrib();
        GL20.glUseProgram(this.program);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadMatrix(this.projection);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(this.modelView);
        GL11.glMatrixMode(this.matrixMode);

        int fixedUnits = GL11.glGetInteger(GL13.GL_MAX_TEXTURE_UNITS);
        for (int i = 0; i < this.textures.length; i++) {
            // Vanilla 1.12 tracks eight texture units, even if the driver exposes many more.
            if (i < 8) {
                activeTexture(GL13.GL_TEXTURE0 + i);
                bindTexture(this.textures[i]);
                if (i < fixedUnits) {
                    setEnabled(GL11.GL_TEXTURE_2D, GL11.glIsEnabled(GL11.GL_TEXTURE_2D));
                }
            } else {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures[i]);
            }
            if (this.samplers != null) GL33.glBindSampler(i, this.samplers[i]);
        }
        activeTexture(this.activeTexture);
        syncVanillaCaches();
    }

    private static void syncVanillaCaches() {
        int[] capabilities = {GL11.GL_BLEND, GL11.GL_DEPTH_TEST, GL11.GL_ALPHA_TEST,
                GL11.GL_CULL_FACE, GL11.GL_LIGHTING, GL11.GL_FOG};
        for (int capability : capabilities) setEnabled(capability, GL11.glIsEnabled(capability));
        depthMask(GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK));
        int depthFunc = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        GlStateManager.depthFunc(depthFunc);
        GL11.glDepthFunc(depthFunc);
        int srcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int dstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int dstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        GlStateManager.tryBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        GL14.glBlendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha);
        int alphaFunc = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        float alphaRef = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
        GlStateManager.alphaFunc(alphaFunc, alphaRef);
        GL11.glAlphaFunc(alphaFunc, alphaRef);
        // glGetBooleanv needs space for four values, not the scalar glGetBoolean overload.
        java.nio.ByteBuffer mask = BufferUtils.createByteBuffer(4);
        GL11.glGetBoolean(GL11.GL_COLOR_WRITEMASK, mask);
        GlStateManager.colorMask(mask.get(0) != 0, mask.get(1) != 0, mask.get(2) != 0, mask.get(3) != 0);
        GL11.glColorMask(mask.get(0) != 0, mask.get(1) != 0, mask.get(2) != 0, mask.get(3) != 0);
        FloatBuffer color = BufferUtils.createFloatBuffer(4);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        GL11.glColor4f(color.get(0), color.get(1), color.get(2), color.get(3));
    }

    private static FloatBuffer matrix(int name) {
        FloatBuffer result = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(name, result);
        return result;
    }
}
