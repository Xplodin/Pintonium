package org.taumc.celeritas.mixin.compat.distanthorizons;

import com.seibel.distanthorizons.api.enums.config.EDhApiDepthDirection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.compat.distanthorizons.DhDepthMode;

/** Opt-in comparison of DH's complete forward-depth path, including its post-processing. */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.common.render.openGl.GlDhRenderApiDefinition", remap = false)
public abstract class MixinDhDepthMode {
    @Inject(method = "getDepthDirection", at = @At("HEAD"), cancellable = true, remap = false)
    private void pintonium$selectDiagnosticDepth(CallbackInfoReturnable<EDhApiDepthDirection> cir) {
        if (DhDepthMode.useForwardDepth()) cir.setReturnValue(EDhApiDepthDirection.FORWARD_Z);
    }
}
