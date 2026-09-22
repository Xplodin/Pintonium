package sayys.depthsupdate.mixin;

import net.minecraft.world.WorldProvider;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(WorldProvider.class)
public abstract class MixinWorldProvider {
   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/WorldProvider;getActualHeight()I")
   private void depthsupdate$getActualHeight(@NonNull CallbackInfoReturnable<Integer> cir) {
      WorldProvider self = (WorldProvider)this;
      HeightContext ctx = HeightManager.get(self.getDimension());
      if (ctx.isExtended()) {
         cir.setReturnValue(ctx.maxY());
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/WorldProvider;getHeight()I")
   private void depthsupdate$getHeight(@NonNull CallbackInfoReturnable<Integer> cir) {
      WorldProvider self = (WorldProvider)this;
      HeightContext ctx = HeightManager.get(self.getDimension());
      if (ctx.isExtended()) {
         cir.setReturnValue(ctx.maxY());
      }
   }
}
