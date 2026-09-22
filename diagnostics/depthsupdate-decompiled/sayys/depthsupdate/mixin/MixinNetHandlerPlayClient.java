package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import sayys.depthsupdate.core.HeightManager;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_73031_a(IIIIII)V"),
      index = 1,
      method = "Lnet/minecraft/client/network/NetHandlerPlayClient;func_147263_a(Lnet/minecraft/network/play/server/SPacketChunkData;)V"
   )
   private int depthsupdate$modifyInvalidateRegionMinY(int y1) {
      World world = Minecraft.func_71410_x().field_71441_e;
      return HeightManager.isExtended(world) ? HeightManager.getMinY(world) : y1;
   }

   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_73031_a(IIIIII)V"),
      index = 4,
      method = "Lnet/minecraft/client/network/NetHandlerPlayClient;func_147263_a(Lnet/minecraft/network/play/server/SPacketChunkData;)V"
   )
   private int depthsupdate$modifyInvalidateRegionMaxY(int y2) {
      World world = Minecraft.func_71410_x().field_71441_e;
      return HeightManager.isExtended(world) ? HeightManager.getMaxY(world) : y2;
   }

   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_147458_c(IIIIII)V"),
      index = 1,
      method = "Lnet/minecraft/client/network/NetHandlerPlayClient;func_147263_a(Lnet/minecraft/network/play/server/SPacketChunkData;)V"
   )
   private int depthsupdate$modifyMarkRenderMinY(int y1) {
      World world = Minecraft.func_71410_x().field_71441_e;
      return HeightManager.isExtended(world) ? HeightManager.getMinY(world) : y1;
   }

   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;func_147458_c(IIIIII)V"),
      index = 4,
      method = "Lnet/minecraft/client/network/NetHandlerPlayClient;func_147263_a(Lnet/minecraft/network/play/server/SPacketChunkData;)V"
   )
   private int depthsupdate$modifyMarkRenderMaxY(int y2) {
      World world = Minecraft.func_71410_x().field_71441_e;
      return HeightManager.isExtended(world) ? HeightManager.getMaxY(world) : y2;
   }
}
