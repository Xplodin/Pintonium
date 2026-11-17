package org.taumc.celeritas.impl.compat.distanthorizons;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11C;
import org.taumc.celeritas.api.v0.CeleritasShadersApi;

public final class DhFadeAlphaFix {
    private static final int ALPHA_TEST = 0x0BC0;
    private static final DhFadeAlphaState.AlphaState ALPHA = new DhFadeAlphaState.AlphaState() {
        public boolean isEnabled() { return GL11C.glIsEnabled(ALPHA_TEST); }
        public void setEnabled(boolean enabled) {
            // Keep the vanilla cache and driver in agreement even after external GL users.
            if (enabled) {
                GlStateManager.enableAlpha();
                GL11C.glEnable(ALPHA_TEST);
            } else {
                GlStateManager.disableAlpha();
                GL11C.glDisable(ALPHA_TEST);
            }
        }
    };

    private DhFadeAlphaFix() {}

    public static void run(Runnable render) {
        DhFadeAlphaState.run(CeleritasShadersApi.getInstance().isShaderPackInUse(), ALPHA, render);
    }
}
