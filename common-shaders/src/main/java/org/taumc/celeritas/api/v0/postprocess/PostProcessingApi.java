package org.taumc.celeritas.api.v0.postprocess;

import java.util.function.Consumer;

/** Supported integration after shader-pack composition and before the HUD, on the render thread. */
public interface PostProcessingApi {
    /**
     * Registers an effect in registration order. Registration is thread-safe; changes during dispatch
     * take effect next frame. Each effect enters a neutral state and its documented GL state is restored
     * even if it throws. Ordinary callback failures are logged and do not prevent subsequent effects.
     * Callbacks run only while a shader pipeline renders a world, including packs without a final program.
     * Retain the registration and close it to unregister. Do not register a new effect every frame.
     *
     * @param id a diagnostic name, preferably modid:effect
     * @param effect callback; the supplied context and its texture IDs must not be retained
     */
    Registration onFinalPassComplete(String id, Consumer<PostProcessingContext> effect);

    interface Registration extends AutoCloseable {
        /** Thread-safe and idempotent. Does not free resources owned by the mod. */
        @Override
        void close();
    }
}
