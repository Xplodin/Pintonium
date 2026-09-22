package com.bean.beanutils.mixin.bettercaves;

import com.bean.beanutils.compat.bettercaves.BetterCavesDepthPatcher;
import com.bean.beanutils.compat.bettercaves.FlooredCavernPatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.yungnickyoung.minecraft.bettercaves.config.io.ConfigLoader", remap = false)
public abstract class MixinBetterCavesConfigLoader {
   @Inject(
      method = "loadConfigFromFileForDimension(I)Lcom/yungnickyoung/minecraft/bettercaves/config/util/ConfigHolder;",
      at = @At("RETURN"),
      require = 1,
      remap = false
   )
   private static void beanutils$extendCaveDepth(int dimension, CallbackInfoReturnable<Object> cir) {
      BetterCavesDepthPatcher.apply(dimension, cir.getReturnValue());
      FlooredCavernPatcher.apply(dimension, cir.getReturnValue());
   }
}
