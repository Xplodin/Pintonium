package com.bean.beanutils.mixin.bettercaves;

import com.yungnickyoung.minecraft.bettercaves.world.carver.cave.CaveCarver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.yungnickyoung.minecraft.bettercaves.world.carver.cave.CaveCarver", remap = false)
public abstract class MixinCaveCarver {
   @Redirect(
      method = "carveColumn",
      at = @At(value = "FIELD", target = "Lcom/yungnickyoung/minecraft/bettercaves/world/carver/cave/CaveCarver;bottomY:I", ordinal = 0),
      require = 1,
      remap = false
   )
   private int beanutils$allowNegativeBottom(CaveCarver ignored) {
      return 0;
   }
}
