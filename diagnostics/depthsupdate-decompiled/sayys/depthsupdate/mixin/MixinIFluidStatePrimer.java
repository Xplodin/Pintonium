package sayys.depthsupdate.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import sayys.depthsupdate.core.HeightManager;

@Mixin(remap = false, targets = "git.jbredwards.fluidlogged_api.api.world.IFluidStatePrimer")
public abstract class MixinIFluidStatePrimer {
   @ModifyConstant(constant = @Constant(intValue = 65536), method = "<init>")
   private int depthsupdate$expandFluidDataArray(int original) {
      return HeightManager.getMaxContext().primerArraySize();
   }
}
