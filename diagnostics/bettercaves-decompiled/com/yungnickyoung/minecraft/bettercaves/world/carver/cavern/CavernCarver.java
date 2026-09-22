package com.yungnickyoung.minecraft.bettercaves.world.carver.cavern;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.enums.CavernType;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseColumn;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseGen;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverSettings;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverUtils;
import com.yungnickyoung.minecraft.bettercaves.world.carver.ICarver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;

public class CavernCarver implements ICarver {
   private CarverSettings settings;
   private NoiseGen noiseGen;
   private World world;
   private CavernType cavernType;
   private int bottomY;
   private int topY;

   public CavernCarver(CavernCarverBuilder builder) {
      this.settings = builder.getSettings();
      this.noiseGen = new NoiseGen(
         this.settings.getWorld(),
         this.settings.isFastNoise(),
         this.settings.getNoiseSettings(),
         this.settings.getNumGens(),
         this.settings.getyCompression(),
         this.settings.getXzCompression()
      );
      this.world = builder.getSettings().getWorld();
      this.cavernType = builder.getCavernType();
      this.bottomY = builder.getBottomY();
      this.topY = builder.getTopY();
      if (this.bottomY > this.topY) {
         BetterCaves.LOGGER.warn("Warning: Min altitude for caverns should not be greater than max altitude.");
         BetterCaves.LOGGER.warn("Using default values...");
         this.bottomY = 1;
         this.topY = 35;
      }
   }

   public void carveColumn(ChunkPrimer primer, BlockPos colPos, int topY, float smoothAmp, NoiseColumn noises, IBlockState liquidBlock, boolean flooded) {
      int localX = BetterCavesUtils.getLocal(colPos.func_177958_n());
      int localZ = BetterCavesUtils.getLocal(colPos.func_177952_p());
      if (localX >= 0 && localX <= 15) {
         if (localZ >= 0 && localZ <= 15) {
            if (this.bottomY >= 0 && this.bottomY <= 255) {
               if (topY <= 255) {
                  topY -= 2;
                  int topTransitionBoundary = topY - 6;
                  int bottomTransitionBoundary = this.bottomY + 3;
                  if (this.cavernType == CavernType.FLOORED) {
                     bottomTransitionBoundary = this.bottomY < this.settings.getLiquidAltitude() ? this.settings.getLiquidAltitude() + 8 : this.bottomY + 7;
                  }

                  topTransitionBoundary = Math.max(topTransitionBoundary, 1);
                  bottomTransitionBoundary = Math.min(bottomTransitionBoundary, 255);

                  for (int y = topY; y >= this.bottomY && (y > this.settings.getLiquidAltitude() || liquidBlock != null); y--) {
                     boolean digBlock = false;
                     float noise = 1.0F;

                     for (double n : noises.get(y).getNoiseValues()) {
                        noise = (float)(noise * n);
                     }

                     float noiseThreshold = this.settings.getNoiseThreshold();
                     if (y >= topTransitionBoundary) {
                        noiseThreshold *= (float)(y - topY) / (topTransitionBoundary - topY);
                     }

                     if (y < bottomTransitionBoundary) {
                        noiseThreshold *= (float)(y - this.bottomY) / (bottomTransitionBoundary - this.bottomY);
                     }

                     if (smoothAmp < 1.0F) {
                        noiseThreshold *= smoothAmp;
                     }

                     if (noise < noiseThreshold) {
                        digBlock = true;
                     }

                     BlockPos blockPos = new BlockPos(localX, y, localZ);
                     IBlockState airBlockState = flooded && y < this.world.func_181545_F()
                        ? Blocks.field_150355_j.func_176223_P()
                        : Blocks.field_150350_a.func_176223_P();
                     if (this.settings.isEnableDebugVisualizer()) {
                        CarverUtils.debugDigBlock(primer, blockPos, this.settings.getDebugBlock(), digBlock);
                     } else if (digBlock) {
                        CarverUtils.digBlock(
                           this.settings.getWorld(),
                           primer,
                           blockPos,
                           airBlockState,
                           liquidBlock,
                           this.settings.getLiquidAltitude(),
                           this.settings.isReplaceFloatingGravel()
                        );
                     }
                  }
               }
            }
         }
      }
   }

   public NoiseGen getNoiseGen() {
      return this.noiseGen;
   }

   public CarverSettings getSettings() {
      return this.settings;
   }

   @Override
   public int getPriority() {
      return this.settings.getPriority();
   }

   public int getBottomY() {
      return this.bottomY;
   }

   @Override
   public int getTopY() {
      return this.topY;
   }
}
