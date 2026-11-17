package org.taumc.celeritas.impl.compat.distanthorizons;

import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Opt-in, bounded observations of DH's default terrain program. Never changes render state. */
public final class DhRenderDiagnostics {
    private static final Logger LOGGER = LogManager.getLogger("Pintonium-DH-Diagnostic");
    private static final Path FLAG = Path.of("config", "pintonium-dh-diagnostics.flag");
    private static long nextSample;
    private static int samples;
    private static int pendingVertexSamples;
    private static int pendingDrawVbo;
    private static ByteBuffer pendingDrawVertices;
    private static final Set<Integer> sampledVbos = new HashSet<>();

    private DhRenderDiagnostics() {}

    public static void afterUniforms(DhApiRenderParam params) {
        long now = System.nanoTime();
        if (now < nextSample) return;
        nextSample = now + 3_000_000_000L;
        if (!Files.isRegularFile(FLAG)) {
            samples = 0;
            sampledVbos.clear();
            pendingVertexSamples = 0;
            pendingDrawVertices = null;
            return;
        }
        if (samples >= 12) return;
        ++samples;
        pendingVertexSamples = 4;

        FloatBuffer liveProjectionBuffer = BufferUtils.createFloatBuffer(16);
        FloatBuffer liveViewBuffer = BufferUtils.createFloatBuffer(16);
        // Query the driver directly, bypassing Minecraft's cached GL state.
        GL11C.glGetFloatv(0x0BA7, liveProjectionBuffer); // GL_PROJECTION_MATRIX
        GL11C.glGetFloatv(0x0BA6, liveViewBuffer); // GL_MODELVIEW_MATRIX
        Matrix4f liveProjection = new Matrix4f(liveProjectionBuffer);
        Matrix4f liveView = new Matrix4f(liveViewBuffer);
        Matrix4f mcProjection = toJoml(params.mcProjectionMatrix);
        Matrix4f mcView = toJoml(params.mcModelViewMatrix);
        Matrix4f dhProjection = toJoml(params.dhProjectionMatrix);
        Matrix4f dhView = toJoml(params.dhModelViewMatrix);
        Matrix4f expected = new Matrix4f(dhProjection).mul(dhView);
        int program = GL11C.glGetInteger(GL20C.GL_CURRENT_PROGRAM);
        int location = program == 0 ? -1 : GL20C.glGetUniformLocation(program, "uCombinedMatrix");
        LOGGER.info("Sample {}/12: pass={}, worldYOffset={}, clip={}..{}, program={}, combinedLocation={}, GPU={}",
                samples, params.renderPass, params.worldYOffset, params.nearClipPlane, params.farClipPlane,
                program, location, GL11C.glGetString(GL11C.GL_RENDERER));
        LOGGER.info("Forward-depth test flag={}", DhDepthMode.useForwardDepth());
        LOGGER.info("Terrain uniforms: whiteWorld={}, worldYOffset={}, clipDistance={}, earthRadius={}",
                integerUniform(program, "uIsWhiteWorld"), floatUniform(program, "uWorldYOffset"),
                floatUniform(program, "uClipDistance"), floatUniform(program, "uEarthRadius"));
        LOGGER.info("DH mc projection={}, view={}", values(mcProjection), values(mcView));
        LOGGER.info("Live GL projection={}, view={}", values(liveProjection), values(liveView));
        LOGGER.info("Camera differences: projection={}, view={}",
                maxDifference(mcProjection, liveProjection), maxDifference(mcView, liveView));
        LOGGER.info("DH projection={}, view={}, expectedCombined={}",
                values(dhProjection), values(dhView), values(expected));
        if (location >= 0) {
            FloatBuffer uniform = BufferUtils.createFloatBuffer(16);
            GL20C.glGetUniformfv(program, location, uniform);
            Matrix4f actual = new Matrix4f(uniform);
            LOGGER.info("GPU combined={}, uploadDifference={}", values(actual), maxDifference(expected, actual));
        }
        if (samples == 12) LOGGER.info("Matrix capture complete. Remove the flag or restart to capture again.");
    }

    public static void afterVertexBuffer(int vbo) {
        if (pendingVertexSamples == 0 || !sampledVbos.add(vbo)) return;
        --pendingVertexSamples;
        LOGGER.info("Vertex setup: requestedVbo={}, vao={}, arrayBuffer={}", vbo,
                GL11C.glGetInteger(GL30C.GL_VERTEX_ARRAY_BINDING), GL11C.glGetInteger(GL15C.GL_ARRAY_BUFFER_BINDING));
        for (int index = 0; index < 3; ++index) {
            LOGGER.info("Attribute {}: enabled={}, size={}, type={}, stride={}, integer={}, normalized={}, buffer={}",
                    index, GL20C.glGetVertexAttribi(index, GL20C.GL_VERTEX_ATTRIB_ARRAY_ENABLED),
                    GL20C.glGetVertexAttribi(index, GL20C.GL_VERTEX_ATTRIB_ARRAY_SIZE),
                    GL20C.glGetVertexAttribi(index, GL20C.GL_VERTEX_ATTRIB_ARRAY_TYPE),
                    GL20C.glGetVertexAttribi(index, GL20C.GL_VERTEX_ATTRIB_ARRAY_STRIDE),
                    GL20C.glGetVertexAttribi(index, GL30C.GL_VERTEX_ATTRIB_ARRAY_INTEGER),
                    GL20C.glGetVertexAttribi(index, GL20C.GL_VERTEX_ATTRIB_ARRAY_NORMALIZED),
                    GL20C.glGetVertexAttribi(index, GL20C.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING));
            if (GL.getCapabilities().OpenGL43) {
                int binding = GL20C.glGetVertexAttribi(index, GL43C.GL_VERTEX_ATTRIB_BINDING);
                LOGGER.info("Attribute {} separate binding={}, relativeOffset={}, bindingStride={}, bindingBuffer={}", index,
                        binding, GL20C.glGetVertexAttribi(index, GL43C.GL_VERTEX_ATTRIB_RELATIVE_OFFSET),
                        GL30C.glGetIntegeri(GL43C.GL_VERTEX_BINDING_STRIDE, binding),
                        GL30C.glGetIntegeri(GL43C.GL_VERTEX_BINDING_BUFFER, binding));
            }
        }
        int program = GL11C.glGetInteger(GL20C.GL_CURRENT_PROGRAM);
        int modelLocation = program == 0 ? -1 : GL20C.glGetUniformLocation(program, "uModelOffset");
        if (modelLocation < 0 && program != 0) modelLocation = GL20C.glGetUniformLocation(program, "modelOffset");
        FloatBuffer model = BufferUtils.createFloatBuffer(16);
        if (modelLocation >= 0) GL20C.glGetUniformfv(program, modelLocation, model);
        LOGGER.info("Draw state: depthFunc={}, depthTest={}, depthMask={}, framebuffer={}, modelOffset=({},{},{})",
                GL11C.glGetInteger(GL11C.GL_DEPTH_FUNC), GL11C.glIsEnabled(GL11C.GL_DEPTH_TEST),
                GL11C.glGetBoolean(GL11C.GL_DEPTH_WRITEMASK), GL11C.glGetInteger(GL30C.GL_DRAW_FRAMEBUFFER_BINDING),
                model.get(0), model.get(1), model.get(2));
        // Inspect only the already-bound buffer. No binds or writes are needed.
        if (GL11C.glGetInteger(GL15C.GL_ARRAY_BUFFER_BINDING) == vbo && vbo != 0
                && GL15C.glGetBufferParameteri(GL15C.GL_ARRAY_BUFFER, GL15C.GL_BUFFER_SIZE) >= 48) {
            int bufferBytes = GL15C.glGetBufferParameteri(GL15C.GL_ARRAY_BUFFER, GL15C.GL_BUFFER_SIZE);
            int readBytes = Math.min(bufferBytes, 262144) & ~15;
            ByteBuffer vertices = BufferUtils.createByteBuffer(readBytes);
            GL15C.glGetBufferSubData(GL15C.GL_ARRAY_BUFFER, 0L, vertices);
            int minY = 65535, maxY = 0, extremeY = 0, greenMin = 65535, greenMax = -1;
            for (int offset = 0; offset < readBytes; offset += 16) {
                int y = Short.toUnsignedInt(vertices.getShort(offset + 2));
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                if (y > 4096) ++extremeY;
                int r = Byte.toUnsignedInt(vertices.get(offset + 8));
                int g = Byte.toUnsignedInt(vertices.get(offset + 9));
                int b = Byte.toUnsignedInt(vertices.get(offset + 10));
                if (g > r * 1.3f && g > b * 1.3f) {
                    greenMin = Math.min(greenMin, y);
                    greenMax = Math.max(greenMax, y);
                }
            }
            LOGGER.info("Mesh bounds: vbo={}, bytesRead={}/{}, vertices={}, y={}..{}, yAbove4096={}, greenY={}..{}",
                    vbo, readBytes, bufferBytes, readBytes / 16, minY, maxY, extremeY, greenMin, greenMax);
            pendingDrawVbo = vbo;
            pendingDrawVertices = vertices;
            for (int vertex = 0; vertex < 3; ++vertex) {
                int offset = vertex * 16;
                LOGGER.info("Vertex {}: position=({},{},{}), meta={}, rgba=({},{},{},{})", vertex,
                        Short.toUnsignedInt(vertices.getShort(offset)), Short.toUnsignedInt(vertices.getShort(offset + 2)),
                        Short.toUnsignedInt(vertices.getShort(offset + 4)), Short.toUnsignedInt(vertices.getShort(offset + 6)),
                        Byte.toUnsignedInt(vertices.get(offset + 8)), Byte.toUnsignedInt(vertices.get(offset + 9)),
                        Byte.toUnsignedInt(vertices.get(offset + 10)), Byte.toUnsignedInt(vertices.get(offset + 11)));
            }
        }
    }

    /** Observes the actual DH draw, including its selected index type. No binds or writes. */
    public static void beforeIndexedDraw(int mode, int count, int type, int offset) {
        ByteBuffer vertices = pendingDrawVertices;
        pendingDrawVertices = null;
        if (vertices == null) return;
        int vbo = GL11C.glGetInteger(GL15C.GL_ARRAY_BUFFER_BINDING);
        int ibo = GL11C.glGetInteger(GL15C.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        int width = indexWidth(type);
        if (vbo != pendingDrawVbo || ibo == 0 || width == 0 || count <= 0 || offset < 0) {
            LOGGER.info("Draw audit unavailable: expectedVbo={}, vbo={}, ibo={}, mode={}, count={}, type={}, offset={}",
                    pendingDrawVbo, vbo, ibo, mode, count, type, offset);
            return;
        }
        int vboBytes = GL15C.glGetBufferParameteri(GL15C.GL_ARRAY_BUFFER, GL15C.GL_BUFFER_SIZE);
        int iboBytes = GL15C.glGetBufferParameteri(GL15C.GL_ELEMENT_ARRAY_BUFFER, GL15C.GL_BUFFER_SIZE);
        long needed = (long) count * width;
        long available = Math.max(0L, (long) iboBytes - offset);
        int readBytes = (int) Math.min(Math.min(needed, available), 262144L);
        readBytes -= readBytes % width;
        if (readBytes == 0) {
            LOGGER.info("Draw audit: empty index buffer, ibo={}, count={}, type={}, offset={}, bufferBytes={}",
                    ibo, count, type, offset, iboBytes);
            return;
        }
        ByteBuffer indices = BufferUtils.createByteBuffer(readBytes);
        GL15C.glGetBufferSubData(GL15C.GL_ELEMENT_ARRAY_BUFFER, (long) offset, indices);
        // DH's quad draw uses six indices per four live vertices. GPU allocations may
        // be larger than that live range; do not mistake unused capacity for geometry.
        int liveVertices = mode == 4 && count % 6 == 0 ? (int) Math.min((long) count / 6 * 4, vboBytes / 16) : vboBytes / 16;
        long[] audit = auditIndices(indices, type, liveVertices);
        ByteBuffer liveSample = vertices.duplicate().order(vertices.order());
        liveSample.limit((int) Math.min(vertices.limit(), (long) liveVertices * 16));
        int[] quads = auditQuads(liveSample);
        LOGGER.info("Quad audit: checked={}, nonAxisAligned={}, degenerate={}, maxVerticalSpan={}",
                quads[0], quads[1], quads[2], quads[3]);
        LOGGER.info("Draw audit: vbo={}, ibo={}, mode={}, count={}, type={}, offset={}, liveVertices={}, neededBytes={}, availableBytes={}, checked={}, minIndex={}, maxIndex={}, outOfRange={}, program={}",
                vbo, ibo, mode, count, type, offset, liveVertices, needed, available, audit[0], audit[1], audit[2], audit[3],
                GL11C.glGetInteger(GL20C.GL_CURRENT_PROGRAM));
    }

    private static int integerUniform(int program, String name) {
        int location = program == 0 ? -1 : GL20C.glGetUniformLocation(program, name);
        return location < 0 ? -1 : GL20C.glGetUniformi(program, location);
    }

    private static float floatUniform(int program, String name) {
        int location = program == 0 ? -1 : GL20C.glGetUniformLocation(program, name);
        if (location < 0) return Float.NaN;
        FloatBuffer value = BufferUtils.createFloatBuffer(1);
        GL20C.glGetUniformfv(program, location, value);
        return value.get(0);
    }

    private static int indexWidth(int type) {
        return switch (type) { case 5121 -> 1; case 5123 -> 2; case 5125 -> 4; default -> 0; };
    }

    /** Returns checked/min/max/out-of-range counts, respecting unsigned GL index types. */
    public static long[] auditIndices(ByteBuffer source, int type, int vertexCount) {
        int width = indexWidth(type);
        if (width == 0) throw new IllegalArgumentException("Unsupported index type " + type);
        long count = 0, min = Long.MAX_VALUE, max = -1, invalid = 0;
        for (int pos = source.position(); pos + width <= source.limit(); pos += width) {
            long index = switch (width) {
                case 1 -> Byte.toUnsignedInt(source.get(pos));
                case 2 -> Short.toUnsignedInt(source.getShort(pos));
                default -> Integer.toUnsignedLong(source.getInt(pos));
            };
            ++count; min = Math.min(min, index); max = Math.max(max, index);
            if (index >= vertexCount) ++invalid;
        }
        return new long[]{count, count == 0 ? -1 : min, max, invalid};
    }

    /** DH emits four 16-byte vertices per axis-aligned quad, before shader curvature. */
    public static int[] auditQuads(ByteBuffer source) {
        int count = 0, skew = 0, degenerate = 0, maxHeight = 0;
        for (int base = source.position(); base + 64 <= source.limit(); base += 64) {
            int dimensions = 0;
            for (int axis = 0; axis < 3; ++axis) {
                int min = 65535, max = 0;
                for (int vertex = 0; vertex < 4; ++vertex) {
                    int coordinate = Short.toUnsignedInt(source.getShort(base + vertex * 16 + axis * 2));
                    min = Math.min(min, coordinate); max = Math.max(max, coordinate);
                }
                if (max != min) ++dimensions;
                if (axis == 1) maxHeight = Math.max(maxHeight, max - min);
            }
            ++count;
            if (dimensions == 3) ++skew;
            if (dimensions < 2) ++degenerate;
        }
        return new int[]{count, skew, degenerate, maxHeight};
    }

    // DH uses row-first field names; JOML's constructor takes columns.
    public static Matrix4f toJoml(DhApiMat4f m) {
        return new Matrix4f(m.m00, m.m10, m.m20, m.m30, m.m01, m.m11, m.m21, m.m31,
                m.m02, m.m12, m.m22, m.m32, m.m03, m.m13, m.m23, m.m33);
    }

    public static float maxDifference(Matrix4f first, Matrix4f second) {
        float[] a = first.get(new float[16]);
        float[] b = second.get(new float[16]);
        float result = 0;
        for (int i = 0; i < 16; ++i) result = Math.max(result, Math.abs(a[i] - b[i]));
        return result;
    }

    private static String values(Matrix4f matrix) {
        return Arrays.toString(matrix.get(new float[16]));
    }
}
