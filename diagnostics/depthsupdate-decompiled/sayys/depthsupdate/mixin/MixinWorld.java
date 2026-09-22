package sayys.depthsupdate.mixin;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(World.class)
public abstract class MixinWorld {
   @Shadow
   private int field_73008_k;

   @Shadow
   public abstract Chunk func_175726_f(BlockPos var1);

   @Shadow
   public abstract IBlockState func_180495_p(BlockPos var1);

   @Shadow
   public abstract int func_175721_c(BlockPos var1, boolean var2);

   @Shadow
   public abstract boolean func_175701_a(BlockPos var1);

   @Shadow
   public abstract boolean func_175667_e(BlockPos var1);

   @Shadow
   protected abstract boolean func_175680_a(int var1, int var2, boolean var3);

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/World;func_175663_a(IIIIIIZ)Z")
   private void depthsupdate$isAreaLoaded(
      int startX, int startY, int startZ, int endX, int endY, int endZ, boolean allowEmpty, CallbackInfoReturnable<Boolean> cir
   ) {
      World self = (World)this;
      if (HeightManager.isExtended(self)) {
         HeightContext ctx = HeightManager.get(self);
         if (endY >= ctx.minY() && startY < ctx.maxY()) {
            int chunkStartX = startX >> 4;
            int chunkStartZ = startZ >> 4;
            int chunkEndX = endX >> 4;
            int chunkEndZ = endZ >> 4;

            for (int i = chunkStartX; i <= chunkEndX; i++) {
               for (int j = chunkStartZ; j <= chunkEndZ; j++) {
                  if (!this.func_175680_a(i, j, allowEmpty)) {
                     cir.setReturnValue(false);
                     return;
                  }
               }
            }

            cir.setReturnValue(true);
         } else {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/World;func_189509_E(Lnet/minecraft/util/math/BlockPos;)Z")
   private void depthsupdate$isOutsideBuildHeight(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      World self = (World)this;
      if (HeightManager.isExtended(self)) {
         HeightContext ctx = HeightManager.get(self);
         cir.setReturnValue(pos.func_177956_o() < ctx.minY() || pos.func_177956_o() >= ctx.maxY());
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/World;func_175699_k(Lnet/minecraft/util/math/BlockPos;)I")
   private void depthsupdate$getLightSimple(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      World self = (World)this;
      if (HeightManager.isExtended(self)) {
         int y = pos.func_177956_o();
         if (y >= HeightManager.getMinY(self) && y < 0 || y >= 256 && y < HeightManager.getMaxY(self)) {
            cir.setReturnValue(this.func_175726_f(pos).func_177443_a(pos, 0));
         }
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/World;func_175721_c(Lnet/minecraft/util/math/BlockPos;Z)I")
   private void depthsupdate$getLight(BlockPos pos, boolean checkNeighbors, CallbackInfoReturnable<Integer> cir) {
      World self = (World)this;
      if (HeightManager.isExtended(self)) {
         int y = pos.func_177956_o();
         if (y >= HeightManager.getMinY(self) && y < 0 || y >= 256 && y < HeightManager.getMaxY(self)) {
            if (pos.func_177958_n() < -30000000 || pos.func_177952_p() < -30000000 || pos.func_177958_n() >= 30000000 || pos.func_177952_p() >= 30000000) {
               cir.setReturnValue(15);
            } else if (checkNeighbors && this.func_180495_p(pos).func_185916_f()) {
               int i1 = this.func_175721_c(pos.func_177984_a(), false);
               int i = this.func_175721_c(pos.func_177974_f(), false);
               int j = this.func_175721_c(pos.func_177976_e(), false);
               int k = this.func_175721_c(pos.func_177968_d(), false);
               int l = this.func_175721_c(pos.func_177978_c(), false);
               if (i > i1) {
                  i1 = i;
               }

               if (j > i1) {
                  i1 = j;
               }

               if (k > i1) {
                  i1 = k;
               }

               if (l > i1) {
                  i1 = l;
               }

               cir.setReturnValue(i1);
            } else {
               Chunk chunk = this.func_175726_f(pos);
               cir.setReturnValue(chunk.func_177443_a(pos, this.field_73008_k));
            }
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/World;func_175642_b(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"
   )
   private void depthsupdate$getLightFor(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      World self = (World)this;
      if (HeightManager.isExtended(self)) {
         int y = pos.func_177956_o();
         if (y >= HeightManager.getMinY(self) && y < 0 || y >= 256 && y < HeightManager.getMaxY(self)) {
            if (!this.func_175701_a(pos)) {
               cir.setReturnValue(type.field_77198_c);
            } else if (!this.func_175667_e(pos)) {
               cir.setReturnValue(type.field_77198_c);
            } else {
               Chunk chunk = this.func_175726_f(pos);
               cir.setReturnValue(chunk.func_177413_a(type, pos));
            }
         }
      }
   }

   @Inject(at = @At("RETURN"), cancellable = true, method = "Lnet/minecraft/world/World;func_189649_b(II)I")
   private void depthsupdate$getHeight(int x, int z, CallbackInfoReturnable<Integer> cir) {
      if (cir.getReturnValueI() < 0) {
         cir.setReturnValue(0);
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/World;func_175672_r(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/BlockPos;"
   )
   private void depthsupdate$getTopSolidOrLiquidBlock(BlockPos pos, CallbackInfoReturnable<BlockPos> cir) {
      World self = (World)this;
      if (HeightManager.isExtended(self)) {
         HeightContext ctx = HeightManager.get(self);
         Chunk chunk = this.func_175726_f(pos);
         BlockPos blockpos = new BlockPos(pos.func_177958_n(), chunk.func_76625_h() + 16, pos.func_177952_p());

         while (blockpos.func_177956_o() >= ctx.minY()) {
            BlockPos blockpos1 = blockpos.func_177977_b();
            Material material = chunk.func_177435_g(blockpos1).func_185904_a();
            if (material.func_76230_c() && material != Material.field_151584_j) {
               break;
            }

            blockpos = blockpos1;
         }

         cir.setReturnValue(blockpos);
      }
   }
}
