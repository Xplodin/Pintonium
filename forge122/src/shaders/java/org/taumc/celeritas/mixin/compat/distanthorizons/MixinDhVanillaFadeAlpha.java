package org.taumc.celeritas.mixin.compat.distanthorizons;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.taumc.celeritas.impl.compat.distanthorizons.DhFadeAlphaFix;

/** DH's Iris-presence guard omits alpha handling even when Pintonium shaderpacks are off. */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.core.api.internal.ClientApi", remap = false)
public abstract class MixinDhVanillaFadeAlpha {
    @WrapMethod(method = {"renderFadeOpaque", "renderFadeTransparent"}, remap = false)
    private void pintonium$allowZeroAlphaFadeWrites(Operation<Void> original) {
        DhFadeAlphaFix.run(() -> original.call());
    }
}
