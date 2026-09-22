package net.irisshaders.iris.gl;

/** Test-only upload recorder: the production uniform classes run without a GL context. */
public final class IrisRenderSystem {
    public static int calls;
    public static float[] last;
    public static void uniform2f(int location, float x, float y) {
        calls++; last = new float[] {x, y};
    }
    public static void uniform4f(int location, float x, float y, float z, float w) {
        calls++; last = new float[] {x, y, z, w};
    }
}
