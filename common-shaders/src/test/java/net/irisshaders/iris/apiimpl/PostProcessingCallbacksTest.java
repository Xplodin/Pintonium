package net.irisshaders.iris.apiimpl;

import org.junit.jupiter.api.Test;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PostProcessingCallbacksTest {
    @Test
    void isolatesFailuresAndLetsInvokerRestoreStateBeforeNextEffect() {
        List<String> events = new ArrayList<>();
        PostProcessingCallbacks callbacks = new PostProcessingCallbacks((id, error) -> events.add("error:" + id));
        callbacks.onFinalPassComplete("broken", context -> {
            events.add("broken");
            throw new IllegalStateException("broken mod");
        });
        callbacks.onFinalPassComplete("working", context -> events.add("working"));
        callbacks.dispatch(effect -> {
            events.add("enter");
            try {
                effect.accept(null);
            } finally {
                events.add("restore");
            }
        });
        assertEquals(List.of("enter", "broken", "restore", "error:broken", "enter", "working", "restore"), events);
    }

    @Test
    void registrationChangesTakeEffectNextFrameAndCloseIsIdempotent() {
        List<String> events = new ArrayList<>();
        PostProcessingCallbacks callbacks = new PostProcessingCallbacks((id, error) -> fail(error));
        PostProcessingApi.Registration[] second = new PostProcessingApi.Registration[1];
        AtomicInteger frames = new AtomicInteger();
        callbacks.onFinalPassComplete("first", context -> {
            events.add("first");
            if (frames.getAndIncrement() == 0) {
                second[0].close();
                second[0].close();
                callbacks.onFinalPassComplete("third", ignored -> events.add("third"));
            }
        });
        second[0] = callbacks.onFinalPassComplete("second", context -> events.add("second"));
        callbacks.dispatch(effect -> effect.accept(null));
        assertEquals(List.of("first", "second"), events);
        events.clear();
        callbacks.dispatch(effect -> effect.accept(null));
        assertEquals(List.of("first", "third"), events);
    }

    @Test
    void logsOnlyOnceButRetriesEffectsOnLaterFrames() {
        AtomicInteger failures = new AtomicInteger();
        AtomicInteger invocations = new AtomicInteger();
        PostProcessingCallbacks callbacks = new PostProcessingCallbacks((id, error) -> failures.incrementAndGet());
        callbacks.onFinalPassComplete("optional-dependency", context -> {
            invocations.incrementAndGet();
            throw new NoClassDefFoundError("missing optional mod");
        });
        callbacks.dispatch(effect -> effect.accept(null));
        callbacks.dispatch(effect -> effect.accept(null));
        assertEquals(2, invocations.get());
        assertEquals(1, failures.get());
    }

    @Test
    void doesNotSwallowFatalVmFailures() {
        PostProcessingCallbacks callbacks = new PostProcessingCallbacks((id, error) -> fail(error));
        callbacks.onFinalPassComplete("fatal", context -> { throw new OutOfMemoryError("test"); });
        assertThrows(OutOfMemoryError.class, () -> callbacks.dispatch(effect -> effect.accept(null)));
    }

    @Test
    void rejectsInvalidRegistrationsAndRemovesTheSpecificRegistration() {
        PostProcessingCallbacks callbacks = new PostProcessingCallbacks((id, error) -> fail(error));
        assertThrows(NullPointerException.class, () -> callbacks.onFinalPassComplete(null, ignored -> {}));
        assertThrows(IllegalArgumentException.class, () -> callbacks.onFinalPassComplete(" ", ignored -> {}));
        assertThrows(NullPointerException.class, () -> callbacks.onFinalPassComplete("id", null));
        AtomicInteger calls = new AtomicInteger();
        PostProcessingApi.Registration registration = callbacks.onFinalPassComplete("same", c -> calls.incrementAndGet());
        callbacks.onFinalPassComplete("same", c -> calls.incrementAndGet());
        registration.close();
        callbacks.dispatch(effect -> effect.accept(null));
        assertEquals(1, calls.get());
        assertFalse(callbacks.isEmpty());
    }
}
