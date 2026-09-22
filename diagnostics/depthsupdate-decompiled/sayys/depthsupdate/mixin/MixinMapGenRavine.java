package sayys.depthsupdate.mixin;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenRavine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

@Mixin(MapGenRavine.class)
public abstract class MixinMapGenRavine extends MapGenBase {
   @Shadow
   private float[] field_75046_d;
   @Unique
   private UndergroundRiverGenerator depthsupdate$river;

   @Unique
   private boolean depthsupdate$riverTouches(int chunkX, int chunkZ, int xMin, int xMax, int zMin, int zMax, int yLow, int yHigh) {
      if (!DepthsUpdateConfig.generateUndergroundRivers) {
         return false;
      } else {
         if (this.depthsupdate$river == null) {
            this.depthsupdate$river = new UndergroundRiverGenerator(this.field_75039_c);
         }

         for (int bx = xMin - 1; bx <= xMax; bx++) {
            for (int bz = zMin - 1; bz <= zMax; bz++) {
               if (this.depthsupdate$river.waterWithin(chunkX * 16 + bx, chunkZ * 16 + bz, yLow, yHigh)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   @Shadow
   protected abstract boolean isOceanBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6);

   @Shadow
   protected abstract boolean isExceptionBiome(Biome var1);

   @Shadow
   protected abstract boolean isTopBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6);

   @Shadow
   protected abstract void digBlock(ChunkPrimer var1, int var2, int var3, int var4, int var5, int var6, boolean var7);

   @Shadow
   protected abstract void func_180707_a(
      long var1,
      int var3,
      int var4,
      ChunkPrimer var5,
      double var6,
      double var8,
      double var10,
      float var12,
      float var13,
      float var14,
      int var15,
      int var16,
      double var17
   );

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/gen/MapGenRavine;digBlock(Lnet/minecraft/world/chunk/ChunkPrimer;IIIIIZ)V")
   protected void depthsupdate$digBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop, CallbackInfo ci) {
      if (HeightManager.isExtended(this.field_75039_c)) {
         ci.cancel();
         Biome biome = this.field_75039_c.func_180494_b(new BlockPos(x + chunkX * 16, 0, z + chunkZ * 16));
         IBlockState state = data.func_177856_a(x, y, z);
         IBlockState top = this.isExceptionBiome(biome) ? Blocks.field_150349_c.func_176223_P() : biome.field_76752_A;
         IBlockState filler = this.isExceptionBiome(biome) ? Blocks.field_150346_d.func_176223_P() : biome.field_76753_B;
         IBlockState deepslate = BlockUtils.getDeepslateBlockState();
         if (state.func_177230_c() == Blocks.field_150348_b
            || state.func_177230_c() == top.func_177230_c()
            || state.func_177230_c() == filler.func_177230_c()
            || state == deepslate
            || state.func_177230_c() == deepslate.func_177230_c()) {
            if (y < HeightManager.getLavaLevel(this.field_75039_c)) {
               data.func_177855_a(x, y, z, Blocks.field_150353_l.func_176223_P());
            } else {
               data.func_177855_a(x, y, z, Blocks.field_150350_a.func_176223_P());
               if (foundTop && data.func_177856_a(x, y - 1, z).func_177230_c() == filler.func_177230_c()) {
                  data.func_177855_a(x, y - 1, z, top.func_177230_c().func_176223_P());
               }
            }
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/gen/MapGenRavine;func_180707_a(JIILnet/minecraft/world/chunk/ChunkPrimer;DDDFFFIID)V"
   )
   protected void depthsupdate$addTunnel(
      long p_180707_1_,
      int p_180707_3_,
      int p_180707_4_,
      ChunkPrimer p_180707_5_,
      double p_180707_6_,
      double p_180707_8_,
      double p_180707_10_,
      float p_180707_12_,
      float p_180707_13_,
      float p_180707_14_,
      int p_180707_15_,
      int p_180707_16_,
      double p_180707_17_,
      CallbackInfo ci
   ) {
      if (HeightManager.isExtended(this.field_75039_c)) {
         ci.cancel();
         Random random = new Random(p_180707_1_);
         double d0 = p_180707_3_ * 16 + 8;
         double d1 = p_180707_4_ * 16 + 8;
         float f = 0.0F;
         float f1 = 0.0F;
         if (p_180707_16_ <= 0) {
            int i = this.field_75040_a * 16 - 16;
            p_180707_16_ = i - random.nextInt(i / 4);
         }

         boolean flag1 = false;
         if (p_180707_15_ == -1) {
            p_180707_15_ = p_180707_16_ / 2;
            flag1 = true;
         }

         float f2 = 1.0F;
         HeightContext heightCtx = HeightManager.get(this.field_75039_c);
         int rsSize = Math.min(heightCtx.maxY() - heightCtx.minY(), this.field_75046_d.length);

         for (int j = 0; j < rsSize; j++) {
            if (j == 0 || random.nextInt(3) == 0) {
               f2 = 1.0F + random.nextFloat() * random.nextFloat();
            }

            this.field_75046_d[j] = f2 * f2;
         }

         for (; p_180707_15_ < p_180707_16_; p_180707_15_++) {
            double d9 = 1.5 + MathHelper.func_76126_a(p_180707_15_ * (float) Math.PI / p_180707_16_) * p_180707_12_;
            double d2 = d9 * p_180707_17_;
            d9 *= random.nextFloat() * 0.25 + 0.75;
            d2 *= random.nextFloat() * 0.25 + 0.75;
            float f3 = MathHelper.func_76134_b(p_180707_14_);
            float f4 = MathHelper.func_76126_a(p_180707_14_);
            p_180707_6_ += MathHelper.func_76134_b(p_180707_13_) * f3;
            p_180707_8_ += f4;
            p_180707_10_ += MathHelper.func_76126_a(p_180707_13_) * f3;
            p_180707_14_ *= 0.7F;
            p_180707_14_ += f1 * 0.05F;
            p_180707_13_ += f * 0.05F;
            f1 *= 0.8F;
            f *= 0.5F;
            f1 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            f += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
            if (flag1 || random.nextInt(4) != 0) {
               double d3 = p_180707_6_ - d0;
               double d4 = p_180707_10_ - d1;
               double d5 = p_180707_16_ - p_180707_15_;
               double d6 = p_180707_12_ + 2.0F + 16.0F;
               if (d3 * d3 + d4 * d4 - d5 * d5 > d6 * d6) {
                  return;
               }

               if (p_180707_6_ >= d0 - 16.0 - d9 * 2.0
                  && p_180707_10_ >= d1 - 16.0 - d9 * 2.0
                  && p_180707_6_ <= d0 + 16.0 + d9 * 2.0
                  && p_180707_10_ <= d1 + 16.0 + d9 * 2.0) {
                  int worldMinY = heightCtx.minY();
                  int worldMaxY = heightCtx.maxY();
                  int k2 = MathHelper.func_76128_c(p_180707_6_ - d9) - p_180707_3_ * 16 - 1;
                  int k = MathHelper.func_76128_c(p_180707_6_ + d9) - p_180707_3_ * 16 + 1;
                  int l2 = MathHelper.func_76128_c(p_180707_8_ - d2) - 1;
                  int l = MathHelper.func_76128_c(p_180707_8_ + d2) + 1;
                  int i3 = MathHelper.func_76128_c(p_180707_10_ - d9) - p_180707_4_ * 16 - 1;
                  int i1 = MathHelper.func_76128_c(p_180707_10_ + d9) - p_180707_4_ * 16 + 1;
                  if (k2 < 0) {
                     k2 = 0;
                  }

                  if (k > 16) {
                     k = 16;
                  }

                  if (l2 < worldMinY + 1) {
                     l2 = worldMinY + 1;
                  }

                  if (l > worldMaxY - 5) {
                     l = worldMaxY - 5;
                  }

                  if (i3 < 0) {
                     i3 = 0;
                  }

                  if (i1 > 16) {
                     i1 = 16;
                  }

                  boolean flag2 = false;

                  for (int j1 = k2; !flag2 && j1 < k; j1++) {
                     for (int k1 = i3; !flag2 && k1 < i1; k1++) {
                        for (int l1 = l + 1; !flag2 && l1 >= l2 - 1; l1--) {
                           if (l1 >= worldMinY && l1 < worldMaxY && this.isOceanBlock(p_180707_5_, j1, l1, k1, p_180707_3_, p_180707_4_)) {
                              flag2 = true;
                           }
                        }
                     }
                  }

                  if (!flag2) {
                     flag2 = this.depthsupdate$riverTouches(
                        p_180707_3_, p_180707_4_, k2, k, i3, i1, Math.max(l2 - 1, worldMinY), Math.min(l + 1, worldMaxY - 1)
                     );
                  }

                  if (!flag2) {
                     for (int j3 = k2; j3 < k; j3++) {
                        double d10 = (j3 + p_180707_3_ * 16 + 0.5 - p_180707_6_) / d9;

                        for (int i2 = i3; i2 < i1; i2++) {
                           double d7 = (i2 + p_180707_4_ * 16 + 0.5 - p_180707_10_) / d9;
                           boolean flag = false;
                           if (d10 * d10 + d7 * d7 < 1.0) {
                              for (int j2 = l; j2 > l2; j2--) {
                                 double d8 = (j2 - 1 + 0.5 - p_180707_8_) / d2;
                                 int rsIndex = j2 - worldMinY;
                                 if (rsIndex >= 0
                                    && rsIndex < this.field_75046_d.length
                                    && (d10 * d10 + d7 * d7) * this.field_75046_d[rsIndex] + d8 * d8 / 6.0 < 1.0) {
                                    if (this.isTopBlock(p_180707_5_, j3, j2, i2, p_180707_3_, p_180707_4_)) {
                                       flag = true;
                                    }

                                    this.digBlock(p_180707_5_, j3, j2, i2, p_180707_3_, p_180707_4_, flag);
                                 }
                              }
                           }
                        }
                     }

                     if (flag1) {
                        break;
                     }
                  }
               }
            }
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/gen/MapGenRavine;func_180701_a(Lnet/minecraft/world/World;IIIILnet/minecraft/world/chunk/ChunkPrimer;)V"
   )
   protected void depthsupdate$recursiveGenerate(
      World p_180701_1_, int p_180701_2_, int p_180701_3_, int p_180701_4_, int p_180701_5_, ChunkPrimer p_180701_6_, CallbackInfo ci
   ) {
      if (HeightManager.isExtended(p_180701_1_)) {
         ci.cancel();
         if (this.field_75038_b.nextInt(50) == 0) {
            double d0 = p_180701_2_ * 16 + this.field_75038_b.nextInt(16);
            double d1 = this.field_75038_b.nextInt(this.field_75038_b.nextInt(40) + 8) + 20;
            double d2 = p_180701_3_ * 16 + this.field_75038_b.nextInt(16);

            for (int j = 0; j < 1; j++) {
               float f = this.field_75038_b.nextFloat() * (float) (Math.PI * 2);
               float f1 = (this.field_75038_b.nextFloat() - 0.5F) * 2.0F / 8.0F;
               float f2 = (this.field_75038_b.nextFloat() * 2.0F + this.field_75038_b.nextFloat()) * 2.0F;
               this.func_180707_a(this.field_75038_b.nextLong(), p_180701_4_, p_180701_5_, p_180701_6_, d0, d1, d2, f2, f, f1, 0, 0, 3.0);
            }
         }
      }
   }
}
