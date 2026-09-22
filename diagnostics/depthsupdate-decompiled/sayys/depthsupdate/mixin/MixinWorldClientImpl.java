package sayys.depthsupdate.mixin;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import sayys.depthsupdate.core.HeightManager;

@Mixin(WorldClient.class)
public class MixinWorldClientImpl {
   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_147458_c(IIIIII)V"),
      index = 1,
      method = "Lnet/minecraft/client/multiplayer/WorldClient;func_73025_a(IIZ)V"
   )
   private int depthsupdate$modifyPreChunkMinY(int y1) {
      World self = (World)this;
      return HeightManager.isExtended(self) ? HeightManager.getMinY(self) : y1;
   }

   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_147458_c(IIIIII)V"),
      index = 4,
      method = "Lnet/minecraft/client/multiplayer/WorldClient;func_73025_a(IIZ)V"
   )
   private int depthsupdate$modifyPreChunkMaxY(int y2) {
      World self = (World)this;
      return HeightManager.isExtended(self) ? HeightManager.getMaxY(self) : y2;
   }
}
