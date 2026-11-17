package org.taumc.celeritas.mixin.compat.distanthorizons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.compat.distanthorizons.DhRenderDiagnostics;

/** Bounded observer for DH's LWJGL3 draw path; inactive without a sampled terrain VBO. */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.lwjgl.lwjgl3.LWJGL3Service", remap = false)
public abstract class MixinDhIndexedDrawDiagnostics {
    @Inject(method = "glDrawElements(IIII)V", at = @At("HEAD"), remap = false)
    private void pintonium$observeIndexedDraw(int mode, int count, int type, int offset, CallbackInfo ci) {
        DhRenderDiagnostics.beforeIndexedDraw(mode, count, type, offset);
    }
}
