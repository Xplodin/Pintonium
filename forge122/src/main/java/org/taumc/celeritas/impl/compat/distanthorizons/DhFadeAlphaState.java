package org.taumc.celeritas.impl.compat.distanthorizons;

/** Legacy alpha rejection must not discard the fade shader's intentional zero-alpha writes. */
public final class DhFadeAlphaState {
    public interface AlphaState {
        boolean isEnabled();
        void setEnabled(boolean enabled);
    }

    private DhFadeAlphaState() {}

    public static void run(boolean shaderPackActive, AlphaState alpha, Runnable render) {
        if (shaderPackActive) {
            render.run();
            return;
        }
        boolean previous = alpha.isEnabled();
        alpha.setEnabled(false);
        try {
            render.run();
        } finally {
            alpha.setEnabled(previous);
        }
    }
}
