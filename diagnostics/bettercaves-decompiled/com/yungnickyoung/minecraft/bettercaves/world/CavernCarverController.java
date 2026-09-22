package com.yungnickyoung.minecraft.bettercaves.world;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.CavernType;
import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseColumn;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseUtils;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverNoiseRange;
import com.yungnickyoung.minecraft.bettercaves.world.carver.cavern.CavernCarver;
import com.yungnickyoung.minecraft.bettercaves.world.carver.cavern.CavernCarverBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class CavernCarverController {
   private World world;
   private FastNoise cavernRegionController;
   private List<CarverNoiseRange> noiseRanges = new ArrayList<>();
   private boolean isDebugViewEnabled;
   private boolean isOverrideSurfaceDetectionEnabled;
   private boolean isFloodedUndergroundEnabled;
   private static Predicate<Biome> isOcean = b -> BiomeDictionary.hasType(b, Type.OCEAN);
   private static Predicate<Biome> isNotOcean = b -> !isOcean.test(b);

   public CavernCarverController(World worldIn, ConfigHolder config) {
      this.world = worldIn;
      this.isDebugViewEnabled = config.debugVisualizer.get();
      this.isOverrideSurfaceDetectionEnabled = config.overrideSurfaceDetection.get();
      this.isFloodedUndergroundEnabled = config.enableFloodedUnderground.get();
      float cavernRegionSize = this.calcCavernRegionSize(config.cavernRegionSize.get(), config.cavernRegionCustomSize.get());
      this.cavernRegionController = new FastNoise();
      this.cavernRegionController.SetSeed((int)worldIn.func_72905_C() + 333);
      this.cavernRegionController.SetFrequency(cavernRegionSize);
      List<CavernCarver> carvers = new ArrayList<>();
      carvers.add(
         new CavernCarverBuilder(worldIn).ofTypeFromConfig(CavernType.LIQUID, config).debugVisualizerBlock(Blocks.field_150451_bX.func_176223_P()).build()
      );
      carvers.add(
         new CavernCarverBuilder(worldIn).ofTypeFromConfig(CavernType.FLOORED, config).debugVisualizerBlock(Blocks.field_150340_R.func_176223_P()).build()
      );
      float spawnChance = config.cavernSpawnChance.get() / 100.0F;
      int totalPriority = carvers.stream().map(CavernCarver::getPriority).reduce(0, Integer::sum);
      BetterCaves.LOGGER.debug("CAVERN INFORMATION");
      BetterCaves.LOGGER.debug("--> SPAWN CHANCE SET TO: " + spawnChance);
      BetterCaves.LOGGER.debug("--> TOTAL PRIORITY: " + totalPriority);
      carvers.removeIf(carverx -> carverx.getPriority() == 0);
      float totalDeadzonePercent = 1.0F - spawnChance;
      float deadzonePercent = carvers.size() > 1 ? totalDeadzonePercent / (carvers.size() - 1) : totalDeadzonePercent;
      BetterCaves.LOGGER.debug("--> DEADZONE PERCENT: " + deadzonePercent + "(" + totalDeadzonePercent + " TOTAL)");
      float currNoise = -1.0F;

      for (CavernCarver carver : carvers) {
         BetterCaves.LOGGER.debug("--> CARVER");
         float rangeCDFPercent = (float)carver.getPriority() / totalPriority * spawnChance;
         float topNoise = NoiseUtils.simplexNoiseOffsetByPercent(currNoise, rangeCDFPercent);
         CarverNoiseRange range = new CarverNoiseRange(currNoise, topNoise, carver);
         this.noiseRanges.add(range);
         currNoise = NoiseUtils.simplexNoiseOffsetByPercent(topNoise, deadzonePercent);
         BetterCaves.LOGGER.debug("    --> RANGE PERCENT LENGTH WANTED: " + rangeCDFPercent);
         BetterCaves.LOGGER.debug("    --> RANGE FOUND: " + range);
      }
   }

   public void carveChunk(ChunkPrimer primer, int chunkX, int chunkZ, int[][] surfaceAltitudes, IBlockState[][] liquidBlocks) {
      if (this.noiseRanges.size() != 0) {
         boolean flooded = false;
         float smoothAmpFactor = 1.0F;

         for (int subX = 0; subX < 4; subX++) {
            for (int subZ = 0; subZ < 4; subZ++) {
               int startX = subX * 4;
               int startZ = subZ * 4;
               int endX = startX + 4 - 1;
               int endZ = startZ + 4 - 1;
               BlockPos startPos = new BlockPos(chunkX * 16 + startX, 1, chunkZ * 16 + startZ);
               BlockPos endPos = new BlockPos(chunkX * 16 + endX, 1, chunkZ * 16 + endZ);
               this.noiseRanges.forEach(rangex -> rangex.setNoiseCube(null));
               int maxHeight = 0;
               if (!this.isOverrideSurfaceDetectionEnabled) {
                  for (int x = startX; x < endX; x++) {
                     for (int z = startZ; z < endZ; z++) {
                        maxHeight = Math.max(maxHeight, surfaceAltitudes[x][z]);
                     }
                  }

                  for (CarverNoiseRange range : this.noiseRanges) {
                     CavernCarver carver = (CavernCarver)range.getCarver();
                     maxHeight = Math.max(maxHeight, carver.getTopY());
                  }
               }

               for (int offsetX = 0; offsetX < 4; offsetX++) {
                  for (int offsetZ = 0; offsetZ < 4; offsetZ++) {
                     int localX = startX + offsetX;
                     int localZ = startZ + offsetZ;
                     BlockPos colPos = new BlockPos(chunkX * 16 + localX, 1, chunkZ * 16 + localZ);
                     if (this.isFloodedUndergroundEnabled && !this.isDebugViewEnabled) {
                        flooded = BiomeDictionary.hasType(this.world.func_180494_b(colPos), Type.OCEAN);
                        smoothAmpFactor = BetterCavesUtils.biomeDistanceFactor(this.world, colPos, 2, flooded ? isNotOcean : isOcean);
                        if (smoothAmpFactor <= 0.0F) {
                           continue;
                        }
                     }

                     int surfaceAltitude = surfaceAltitudes[localX][localZ];
                     IBlockState liquidBlock = liquidBlocks[localX][localZ];
                     float cavernRegionNoise = this.cavernRegionController.GetNoise(colPos.func_177958_n(), colPos.func_177952_p());

                     for (CarverNoiseRange range : this.noiseRanges) {
                        if (range.contains(cavernRegionNoise)) {
                           CavernCarver carver = (CavernCarver)range.getCarver();
                           int bottomY = carver.getBottomY();
                           int topY = this.isDebugViewEnabled ? carver.getTopY() : Math.min(surfaceAltitude, carver.getTopY());
                           if (this.isOverrideSurfaceDetectionEnabled) {
                              topY = carver.getTopY();
                              maxHeight = carver.getTopY();
                           }

                           float smoothAmp = range.getSmoothAmp(cavernRegionNoise) * smoothAmpFactor;
                           if (range.getNoiseCube() == null) {
                              range.setNoiseCube(carver.getNoiseGen().interpolateNoiseCube(startPos, endPos, bottomY, maxHeight));
                           }

                           NoiseColumn noiseColumn = range.getNoiseCube().get(offsetX).get(offsetZ);
                           carver.carveColumn(primer, colPos, topY, smoothAmp, noiseColumn, liquidBlock, flooded);
                           break;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private float calcCavernRegionSize(RegionSize cavernRegionSize, float cavernRegionCustomSize) {
      switch (cavernRegionSize) {
         case Small:
            return 0.01F;
         case Large:
            return 0.005F;
         case ExtraLarge:
            return 0.001F;
         case Custom:
            return cavernRegionCustomSize;
         default:
            return 0.007F;
      }
   }
}
