package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobalChunkOffset {
   @Shadow
   private int field_72739_F;
   @Shadow
   private ViewFrustum field_175008_n;

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/client/renderer/RenderGlobal;func_181562_a(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/chunk/RenderChunk;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/client/renderer/chunk/RenderChunk;"
   )
   private void depthsupdate$getRenderChunkOffset(BlockPos playerPos, RenderChunk renderChunkBase, EnumFacing facing, CallbackInfoReturnable<RenderChunk> cir) {
      World world = Minecraft.func_71410_x().field_71441_e;
      if (HeightManager.isExtended(world)) {
         HeightContext ctx = HeightManager.get(world);
         BlockPos blockpos = renderChunkBase.func_181701_a(facing);
         if (MathHelper.func_76130_a(playerPos.func_177958_n() - blockpos.func_177958_n()) > this.field_72739_F * 16) {
            cir.setReturnValue(null);
         } else if (blockpos.func_177956_o() >= ctx.minY() && blockpos.func_177956_o() < ctx.maxY()) {
            cir.setReturnValue(
               MathHelper.func_76130_a(playerPos.func_177952_p() - blockpos.func_177952_p()) > this.field_72739_F * 16
                  ? null
                  : ((IMixinViewFrustum)this.field_175008_n).invokeGetRenderChunk(blockpos)
            );
         } else {
            cir.setReturnValue(null);
         }
      }
   }
}
