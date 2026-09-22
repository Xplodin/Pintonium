package com.bean.beanutils.mixin.bettercaves;

import com.yungnickyoung.minecraft.bettercaves.world.carver.cavern.CavernCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.yungnickyoung.minecraft.bettercaves.world.carver.cavern.CavernCarver", remap = false)
public abstract class MixinCavernCarver {
   @Redirect(
      method = "carveColumn",
      at = @At(value = "FIELD", target = "Lcom/yungnickyoung/minecraft/bettercaves/world/carver/cavern/CavernCarver;bottomY:I", ordinal = 0),
      require = 1,
      remap = false
   )
   private int beanutils$allowNegativeBottom(CavernCarver var1) {
      return 0;
   }

   @Redirect(method = "carveColumn", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I", ordinal = 0), require = 1, remap = false)
   private int beanutils$removeVanillaTransitionFloor(int var1, int var2) {
      return var1;
   }
}
