package org.taumc.celeritas.mixin.compat.distanthorizons;

import org.taumc.celeritas.api.v0.CeleritasShadersApi;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.compat.distanthorizons.DhShaderAccessor;
import org.taumc.celeritas.interfaces.IRenderTargetExt;

/** DH 3.3's Forge 1.12 loader otherwise recognizes only Actinium. */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.forge112.ForgeMain", remap = false)
public abstract class MixinDhForgeMainShaderCompat {
    @Inject(method = "initializeModCompat", at = @At("HEAD"), remap = false)
    private void pintonium$connectShaderLoader(CallbackInfo ci) {
        try {
            if (DhShaderAccessor.register(getClass().getClassLoader(),
                    () -> CeleritasShadersApi.getInstance().isShaderPackInUse(),
                    () -> CeleritasShadersApi.getInstance().isRenderingShadowPass(),
                    framebuffer -> framebuffer instanceof IRenderTargetExt target
                            ? target.getIris$depthTextureId() : -1)) {
                LogManager.getLogger("Pintonium-DH").info("Connected Distant Horizons to Pintonium's shader pipeline.");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to connect Distant Horizons to Pintonium's shader pipeline.", e);
        }
    }
}
