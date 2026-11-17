package org.taumc.celeritas.impl.compat.nothirium;

/**
 * Stage one: Nothirium owns terrain, its GL state and video options. Shader hooks
 * cannot run until a Nothirium vertex/program bridge has been implemented.
 * Detect resources rather than defining optional classes during Mixin discovery.
 */
public final class NothiriumCompatibility {
    public static final String MARKER = "meldexun/nothirium/mc/asm/NothiriumPlugin.class";
    private static final String MIXIN_PACKAGE = "org.taumc.celeritas.mixin.";

    private NothiriumCompatibility() {}

    private static final class Detection {
        private static final boolean PRESENT = detect(NothiriumCompatibility.class.getClassLoader());
    }

    public static boolean isPresent() {
        return Detection.PRESENT;
    }

    public static boolean detect(ClassLoader loader) {
        return loader != null && loader.getResource(MARKER) != null;
    }

    public static boolean shouldApplyMixin(String name, boolean nothiriumPresent) {
        if (!nothiriumPresent) return true;
        String relative = name.startsWith(MIXIN_PACKAGE) ? name.substring(MIXIN_PACKAGE.length()) : name;
        // Only these independent optimizations have been reviewed for this stage.
        // In particular, visible-only animation needs our terrain visibility feed.
        return relative.equals("core.MixinDirection")
                || relative.startsWith("core.collections.")
                || relative.equals("features.mipmaps.TextureUtilMixin")
                || relative.startsWith("compat.ironchest.");
    }
}
