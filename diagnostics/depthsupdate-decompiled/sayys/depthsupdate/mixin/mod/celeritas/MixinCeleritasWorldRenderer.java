package sayys.depthsupdate.mixin.mod.celeritas;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = CeleritasWorldRenderer.class, remap = false)
public class MixinCeleritasWorldRenderer {
   @Inject(at = @At("HEAD"), cancellable = true, method = "getMinimumBuildHeight")
   private void depthsupdate$fixMinBuildHeight(CallbackInfoReturnable<Integer> cir) {
      if (HeightManager.isExtended(Minecraft.func_71410_x().field_71441_e)) {
         cir.setReturnValue(HeightManager.getMinY(Minecraft.func_71410_x().field_71441_e));
      }
   }
}
