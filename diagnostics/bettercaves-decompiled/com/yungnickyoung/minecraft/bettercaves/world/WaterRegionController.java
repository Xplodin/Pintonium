package com.yungnickyoung.minecraft.bettercaves.world;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseUtils;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WaterRegionController {
   private FastNoise waterRegionController;
   private long worldSeed;
   private int dimensionID;
   private String dimensionName;
   private Random rand;
   private IBlockState lavaBlock;
   private IBlockState waterBlock;
   private float waterRegionThreshold;
   private static final float SMOOTH_RANGE = 0.04F;
   private static final float SMOOTH_DELTA = 0.01F;

   public WaterRegionController(World world, ConfigHolder config) {
      this.worldSeed = world.func_72905_C();
      this.dimensionID = world.field_73011_w.getDimension();
      this.dimensionName = world.field_73011_w.func_186058_p().toString();
      this.rand = new Random();
      this.lavaBlock = this.getLavaBlockFromString(config.lavaBlock.get());
      this.waterBlock = this.getWaterBlockFromString(config.waterBlock.get());
      this.waterRegionThreshold = NoiseUtils.simplexNoiseOffsetByPercent(-1.0F, config.waterRegionSpawnChance.get() / 100.0F);
      float waterRegionSize = this.calcWaterRegionSize(config.waterRegionSize.get(), config.waterRegionCustomSize.get());
      this.waterRegionController = new FastNoise();
      this.waterRegionController.SetSeed((int)world.func_72905_C() + 444);
      this.waterRegionController.SetFrequency(waterRegionSize);
   }

   public IBlockState[][] getLiquidBlocksForChunk(int chunkX, int chunkZ) {
      this.rand.setSeed(this.worldSeed ^ chunkX ^ chunkZ);
      IBlockState[][] blocks = new IBlockState[16][16];

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int realX = chunkX * 16 + x;
            int realZ = chunkZ * 16 + z;
            BlockPos pos = new BlockPos(realX, 1, realZ);
            blocks[x][z] = this.getLiquidBlockAtPos(this.rand, pos);
         }
      }

      return blocks;
   }

   private IBlockState getLiquidBlockAtPos(Random rand, BlockPos blockPos) {
      IBlockState liquidBlock = this.lavaBlock;
      if (this.waterRegionThreshold > -1.0F) {
         float waterRegionNoise = this.waterRegionController.GetNoise(blockPos.func_177958_n(), blockPos.func_177952_p());
         float randOffset = rand.nextFloat() * 0.01F + 0.04F;
         if (waterRegionNoise < this.waterRegionThreshold - randOffset) {
            liquidBlock = this.waterBlock;
         } else if (waterRegionNoise < this.waterRegionThreshold + randOffset) {
            liquidBlock = null;
         }
      }

      return liquidBlock;
   }

   private IBlockState getLavaBlockFromString(String lavaString) {
      IBlockState lavaBlock;
      try {
         lavaBlock = Block.func_149684_b(lavaString).func_176223_P();
         BetterCaves.LOGGER
            .info(
               "Using block '"
                  + lavaString
                  + "' as lava in cave generation for dimension "
                  + BetterCavesUtils.dimensionAsString(this.dimensionID, this.dimensionName)
                  + " ..."
            );
      } catch (Exception var4) {
         BetterCaves.LOGGER.warn("Unable to use block '" + lavaString + "': " + var4);
         BetterCaves.LOGGER.warn("Using vanilla lava instead...");
         lavaBlock = Blocks.field_150353_l.func_176223_P();
      }

      if (lavaBlock == null) {
         BetterCaves.LOGGER.warn("Unable to use block '" + lavaString + "': null block returned.\n Using vanilla lava instead...");
         lavaBlock = Blocks.field_150353_l.func_176223_P();
      }

      return lavaBlock;
   }

   private IBlockState getWaterBlockFromString(String waterString) {
      IBlockState waterBlock;
      try {
         waterBlock = Block.func_149684_b(waterString).func_176223_P();
         BetterCaves.LOGGER
            .info(
               "Using block '"
                  + waterString
                  + "' as water in cave generation for dimension "
                  + BetterCavesUtils.dimensionAsString(this.dimensionID, this.dimensionName)
                  + " ..."
            );
      } catch (Exception var4) {
         BetterCaves.LOGGER.warn("Unable to use block '" + waterString + "': " + var4);
         BetterCaves.LOGGER.warn("Using vanilla water instead...");
         waterBlock = Blocks.field_150355_j.func_176223_P();
      }

      if (waterBlock == null) {
         BetterCaves.LOGGER.warn("Unable to use block '" + waterString + "': null block returned.\n Using vanilla water instead...");
         waterBlock = Blocks.field_150355_j.func_176223_P();
      }

      return waterBlock;
   }

   private float calcWaterRegionSize(RegionSize waterRegionSize, float waterRegionCustomSize) {
      switch (waterRegionSize) {
         case Small:
            return 0.008F;
         case Large:
            return 0.0028F;
         case ExtraLarge:
            return 0.001F;
         case Custom:
            return waterRegionCustomSize;
         default:
            return 0.004F;
      }
   }
}
