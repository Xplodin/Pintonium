package net.irisshaders.iris.apiimpl;

import org.taumc.celeritas.api.v0.postprocess.PostProcessingApi;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingContext;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Registration and failure isolation, independent of Minecraft and OpenGL. */
public final class PostProcessingCallbacks implements PostProcessingApi {
    private final CopyOnWriteArrayList<Entry> entries = new CopyOnWriteArrayList<>();
    private final BiConsumer<String, Throwable> errorHandler;

    public PostProcessingCallbacks(BiConsumer<String, Throwable> errorHandler) {
        this.errorHandler = Objects.requireNonNull(errorHandler, "errorHandler");
    }

    @Override
    public Registration onFinalPassComplete(String id, Consumer<PostProcessingContext> effect) {
        Objects.requireNonNull(id, "id");
        if (id.trim().isEmpty()) {
            throw new IllegalArgumentException("Effect id must not be empty");
        }
        Entry entry = new Entry(id, Objects.requireNonNull(effect, "effect"));
        this.entries.add(entry);
        return () -> this.entries.remove(entry);
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    /** The invoker owns per-callback context lifetime and GL restoration. */
    public void dispatch(Consumer<Consumer<PostProcessingContext>> invoker) {
        for (Entry entry : this.entries) {
            try {
                invoker.accept(entry.effect);
            } catch (VirtualMachineError | ThreadDeath fatal) {
                throw fatal;
            } catch (Throwable failure) {
                // Report once per registration, avoiding a log flood from an effect failing every frame.
                if (!entry.reportedFailure) {
                    entry.reportedFailure = true;
                    this.errorHandler.accept(entry.id, failure);
                }
            }
        }
    }

    private static final class Entry {
        private final String id;
        private final Consumer<PostProcessingContext> effect;
        private boolean reportedFailure;

        private Entry(String id, Consumer<PostProcessingContext> effect) {
            this.id = id;
            this.effect = effect;
        }
    }
}
