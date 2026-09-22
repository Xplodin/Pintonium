package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.EnumSkyBlock;
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
public abstract class MixinChunkCacheLighting {
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

   @Shadow
   public abstract int func_175628_b(EnumSkyBlock var1, BlockPos var2);

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/ChunkCache;func_175628_b(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"
   )
   private void depthsupdate$getLightFor(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      if (HeightManager.isExtended(this.field_72815_e)) {
         int y = pos.func_177956_o();
         int minY = HeightManager.getMinY(this.field_72815_e);
         int maxY = HeightManager.getMaxY(this.field_72815_e);
         if (y < minY || y >= maxY) {
            cir.setReturnValue(type.field_77198_c);
         } else if (y < 0 || y >= 256) {
            int i = (pos.func_177958_n() >> 4) - this.field_72818_a;
            int j = (pos.func_177952_p() >> 4) - this.field_72816_b;
            if (!this.withinBounds(i, j)) {
               cir.setReturnValue(type.field_77198_c);
            } else {
               cir.setReturnValue(this.field_72817_c[i][j].func_177413_a(type, pos));
            }
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/ChunkCache;func_175629_a(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"
   )
   private void depthsupdate$getLightForExt(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      if (HeightManager.isExtended(this.field_72815_e)) {
         int y = pos.func_177956_o();
         int minY = HeightManager.getMinY(this.field_72815_e);
         int maxY = HeightManager.getMaxY(this.field_72815_e);
         if (y < minY || y >= maxY) {
            cir.setReturnValue(type.field_77198_c);
         } else if (y < 0 || y >= 256) {
            if (!this.func_180495_p(pos).func_185916_f()) {
               int i = (pos.func_177958_n() >> 4) - this.field_72818_a;
               int j = (pos.func_177952_p() >> 4) - this.field_72816_b;
               if (!this.withinBounds(i, j)) {
                  cir.setReturnValue(type.field_77198_c);
               } else {
                  cir.setReturnValue(this.field_72817_c[i][j].func_177413_a(type, pos));
               }
            } else {
               int brightest = 0;

               for (EnumFacing facing : EnumFacing.values()) {
                  int light = this.func_175628_b(type, pos.func_177972_a(facing));
                  if (light > brightest) {
                     brightest = light;
                  }

                  if (brightest >= 15) {
                     break;
                  }
               }

               cir.setReturnValue(brightest);
            }
         }
      }
   }
}
