package net.irisshaders.iris.gl.uniform;

import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import org.joml.Vector2f;

public final class MutableUniformRegression {
    private static final class Notifier implements ValueUpdateNotifier {
        Runnable listener;
        public void setListener(Runnable listener) { this.listener = listener; }
    }
    public static void main(String[] args) {
        Vector2f vector = new Vector2f();
        Notifier notifier = new Notifier();
        Vector2Uniform vec = new Vector2Uniform(7, () -> vector, notifier);
        vec.update();
        check(IrisRenderSystem.calls == 1, "Initial zero vector must upload");
        vec.update();
        check(IrisRenderSystem.calls == 1, "Unchanged vector must not upload");
        vector.set(3, 4);
        vec.update();
        check(IrisRenderSystem.calls == 2 && IrisRenderSystem.last[0] == 3, "Reused vector mutation must upload");
        vector.set(5, 6);
        notifier.listener.run();
        check(IrisRenderSystem.calls == 3 && IrisRenderSystem.last[1] == 6, "Notifier must observe vector mutation");
        notifier.listener.run();
        check(IrisRenderSystem.calls == 3, "Notifier must suppress unchanged upload");
        float[] array = {1, 2, 3, 4};
        Notifier arrayNotifier = new Notifier();
        Vector4ArrayUniform arr = new Vector4ArrayUniform(8, () -> array, arrayNotifier);
        arr.update();
        int previous = IrisRenderSystem.calls;
        arr.update();
        check(IrisRenderSystem.calls == previous, "Unchanged array must not upload");
        array[2] = 9;
        arr.update();
        check(IrisRenderSystem.calls == previous + 1 && IrisRenderSystem.last[2] == 9, "Reused array mutation must upload");
        array[3] = 10;
        arrayNotifier.listener.run();
        check(IrisRenderSystem.calls == previous + 2 && IrisRenderSystem.last[3] == 10, "Notifier must observe array mutation");
        arrayNotifier.listener.run();
        check(IrisRenderSystem.calls == previous + 2, "Unchanged array notifier must not upload");
        System.out.println("PASS: mutable vector/array snapshots, initial vector upload, unchanged suppression, notifier updates");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
