package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
   @Redirect(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;func_178568_j()Lnet/minecraft/util/math/BlockPos;"),
      method = "Lnet/minecraft/client/renderer/RenderGlobal;func_180446_a(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V"
   )
   private BlockPos depthsupdate$redirectRenderChunkPosForEntityArray(RenderChunk renderChunk) {
      BlockPos pos = renderChunk.func_178568_j();
      World world = Minecraft.func_71410_x().field_71441_e;
      if (HeightManager.isExtended(world)) {
         HeightContext ctx = HeightManager.get(world);
         int storageIndex = ctx.toStorageIndex(pos.func_177956_o());
         return new BlockPos(pos.func_177958_n(), storageIndex * 16, pos.func_177952_p());
      } else {
         return pos;
      }
   }
}
