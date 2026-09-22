package com.kc.bcducompat.mixin;

import com.kc.bcducompat.BetterCavesConfigPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.yungnickyoung.minecraft.bettercaves.config.io.ConfigLoader", remap = false)
public abstract class MixinConfigLoader {
   @Inject(
      method = "loadConfigFromFileForDimension(I)Lcom/yungnickyoung/minecraft/bettercaves/config/util/ConfigHolder;",
      at = @At("RETURN"),
      require = 1,
      remap = false
   )
   private static void bcducompat$applyNegativeYSettings(int var0, CallbackInfoReturnable<Object> var1) {
      BetterCavesConfigPatcher.apply(var0, var1.getReturnValue());
   }
}
