package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = ViewFrustum.class, priority = 1100)
public abstract class MixinViewFrustum {
   @Shadow
   protected int field_178165_d;
   @Shadow
   protected int field_178168_c;
   @Shadow
   protected int field_178166_e;
   @Shadow
   public RenderChunk[] field_178164_f;

   @Shadow
   protected abstract int func_178157_a(int var1, int var2, int var3);

   @Unique
   private HeightContext depthsupdate$ctx() {
      World world = Minecraft.func_71410_x().field_71441_e;
      return HeightManager.get(world);
   }

   @ModifyConstant(constant = @Constant(intValue = 16), method = "Lnet/minecraft/client/renderer/ViewFrustum;func_178159_a(I)V")
   private int depthsupdate$modifyCountChunksY(int original) {
      HeightContext ctx = this.depthsupdate$ctx();
      return ctx.isExtended() ? ctx.totalStorageSections() : original;
   }

   @ModifyArg(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunk;func_189562_a(III)V"),
      index = 1,
      method = "Lnet/minecraft/client/renderer/ViewFrustum;func_178163_a(DD)V"
   )
   private int depthsupdate$modifyChunkYPosition(int y) {
      HeightContext ctx = this.depthsupdate$ctx();
      return ctx.isExtended() ? y + ctx.minY() : y;
   }

   @ModifyVariable(at = @At("HEAD"), argsOnly = true, ordinal = 1, method = "Lnet/minecraft/client/renderer/ViewFrustum;func_187474_a(IIIIIIZ)V")
   private int depthsupdate$modifyMinY(int minY) {
      HeightContext ctx = this.depthsupdate$ctx();
      return ctx.isExtended() ? minY - ctx.minY() : minY;
   }

   @ModifyVariable(at = @At("HEAD"), argsOnly = true, ordinal = 4, method = "Lnet/minecraft/client/renderer/ViewFrustum;func_187474_a(IIIIIIZ)V")
   private int depthsupdate$modifyMaxY(int maxY) {
      HeightContext ctx = this.depthsupdate$ctx();
      return ctx.isExtended() ? maxY - ctx.minY() : maxY;
   }

   @ModifyVariable(
      at = @At("HEAD"),
      argsOnly = true,
      method = "Lnet/minecraft/client/renderer/ViewFrustum;func_178161_a(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/client/renderer/chunk/RenderChunk;"
   )
   private BlockPos depthsupdate$modifyPos(@NonNull BlockPos pos) {
      HeightContext ctx = this.depthsupdate$ctx();
      return ctx.isExtended() ? pos.func_177981_b(-ctx.minY()) : pos;
   }
}
