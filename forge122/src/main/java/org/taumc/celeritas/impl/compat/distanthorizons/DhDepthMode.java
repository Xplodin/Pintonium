package org.taumc.celeritas.impl.compat.distanthorizons;

import java.nio.file.Files;
import java.nio.file.Path;

/** Reversible diagnostic switch; the absent flag preserves DH's own depth decision. */
public final class DhDepthMode {
    private static final Path FLAG = Path.of("config", "pintonium-dh-forward-depth.flag");
    private static long nextCheck;
    private static boolean forward;

    private DhDepthMode() {}

    public static boolean useForwardDepth() {
        long now = System.nanoTime();
        if (now >= nextCheck) {
            nextCheck = now + 500_000_000L;
            forward = Files.isRegularFile(FLAG);
        }
        return forward;
    }
}
