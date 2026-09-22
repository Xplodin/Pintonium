package com.bean.beanutils.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "cofh.cofhworld.world.distribution.DistributionUniform", remap = false)
public class MixinDistributionUniform {
   @Redirect(
      method = "generateFeature(Ljava/util/Random;IILnet/minecraft/world/World;)Z",
      at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", remap = false),
      remap = false
   )
   private int cofhNegYPatch$allowNegativeMinY(int a, int b) {
      return a;
   }
}
