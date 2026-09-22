package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightManager;

@Mixin(ChunkCache.class)
public abstract class MixinChunkCache {
   @Shadow
   protected int field_72818_a;
   @Shadow
   protected int field_72816_b;
   @Shadow
   protected Chunk[][] field_72817_c;
   @Shadow
   protected World field_72815_e;

   @Shadow
   protected abstract boolean withinBounds(int var1, int var2);

   @Shadow
   public abstract IBlockState func_180495_p(BlockPos var1);

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/ChunkCache;func_180495_p(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;"
   )
   private void depthsupdate$getBlockState(@NonNull BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
      if (HeightManager.isExtended(this.field_72815_e)) {
         int y = pos.func_177956_o();
         int minY = HeightManager.getMinY(this.field_72815_e);
         int maxY = HeightManager.getMaxY(this.field_72815_e);
         if (y >= minY && y < maxY) {
            int i = (pos.func_177958_n() >> 4) - this.field_72818_a;
            int j = (pos.func_177952_p() >> 4) - this.field_72816_b;
            if (i >= 0 && i < this.field_72817_c.length && j >= 0 && j < this.field_72817_c[i].length) {
               Chunk chunk = this.field_72817_c[i][j];
               if (chunk != null) {
                  cir.setReturnValue(chunk.func_177435_g(pos));
                  return;
               }
            }

            cir.setReturnValue(Blocks.field_150350_a.func_176223_P());
         } else {
            cir.setReturnValue(Blocks.field_150350_a.func_176223_P());
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/ChunkCache;isSideSolid(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;Z)Z"
   )
   private void depthsupdate$isSideSolid(@NonNull BlockPos pos, EnumFacing side, boolean _default, CallbackInfoReturnable<Boolean> cir) {
      if (HeightManager.isExtended(this.field_72815_e)) {
         int y = pos.func_177956_o();
         int minY = HeightManager.getMinY(this.field_72815_e);
         int maxY = HeightManager.getMaxY(this.field_72815_e);
         if (y < minY || y >= maxY) {
            cir.setReturnValue(_default);
         } else if (y < 0 || y >= 256) {
            int x = (pos.func_177958_n() >> 4) - this.field_72818_a;
            int z = (pos.func_177952_p() >> 4) - this.field_72816_b;
            if (!this.withinBounds(x, z)) {
               cir.setReturnValue(_default);
            } else {
               IBlockState state = this.field_72817_c[x][z].func_177435_g(pos);
               cir.setReturnValue(state.func_177230_c().isSideSolid(state, (ChunkCache)this, pos, side));
            }
         }
      }
   }
}
