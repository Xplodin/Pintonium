package com.yungnickyoung.minecraft.bettercaves.world;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.CaveType;
import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseColumn;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverNoiseRange;
import com.yungnickyoung.minecraft.bettercaves.world.carver.ICarver;
import com.yungnickyoung.minecraft.bettercaves.world.carver.cave.CaveCarver;
import com.yungnickyoung.minecraft.bettercaves.world.carver.cave.CaveCarverBuilder;
import com.yungnickyoung.minecraft.bettercaves.world.carver.vanilla.VanillaCaveCarver;
import com.yungnickyoung.minecraft.bettercaves.world.carver.vanilla.VanillaCaveCarverBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class CaveCarverController {
   private World world;
   private VanillaCaveCarver surfaceCaveCarver;
   private FastNoise caveRegionController;
   private List<CarverNoiseRange> noiseRanges = new ArrayList<>();
   private boolean isDebugViewEnabled;
   private boolean isOverrideSurfaceDetectionEnabled;
   private boolean isSurfaceCavesEnabled;
   private boolean isFloodedUndergroundEnabled;

   public CaveCarverController(World worldIn, ConfigHolder config) {
      this.world = worldIn;
      this.isDebugViewEnabled = config.debugVisualizer.get();
      this.isOverrideSurfaceDetectionEnabled = config.overrideSurfaceDetection.get();
      this.isSurfaceCavesEnabled = config.isSurfaceCavesEnabled.get();
      this.isFloodedUndergroundEnabled = config.enableFloodedUnderground.get();
      this.surfaceCaveCarver = new VanillaCaveCarverBuilder()
         .bottomY(config.surfaceCaveBottom.get())
         .topY(config.surfaceCaveTop.get())
         .density(config.surfaceCaveDensity.get())
         .liquidAltitude(config.liquidAltitude.get())
         .replaceGravel(config.replaceFloatingGravel.get())
         .floodedUnderground(config.enableFloodedUnderground.get())
         .debugVisualizerEnabled(config.debugVisualizer.get())
         .debugVisualizerBlock(Blocks.field_150475_bE.func_176223_P())
         .build();
      float caveRegionSize = this.calcCaveRegionSize(config.caveRegionSize.get(), config.caveRegionCustomSize.get());
      this.caveRegionController = new FastNoise();
      this.caveRegionController.SetSeed((int)worldIn.func_72905_C() + 222);
      this.caveRegionController.SetFrequency(caveRegionSize);
      this.caveRegionController.SetNoiseType(FastNoise.NoiseType.Cellular);
      this.caveRegionController.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);
      List<ICarver> carvers = new ArrayList<>();
      carvers.add(new CaveCarverBuilder(worldIn).ofTypeFromConfig(CaveType.CUBIC, config).debugVisualizerBlock(Blocks.field_150344_f.func_176223_P()).build());
      carvers.add(new CaveCarverBuilder(worldIn).ofTypeFromConfig(CaveType.SIMPLEX, config).debugVisualizerBlock(Blocks.field_150347_e.func_176223_P()).build());
      carvers.add(
         new VanillaCaveCarverBuilder()
            .bottomY(config.vanillaCaveBottom.get())
            .topY(config.vanillaCaveTop.get())
            .density(config.vanillaCaveDensity.get())
            .priority(config.vanillaCavePriority.get())
            .liquidAltitude(config.liquidAltitude.get())
            .replaceGravel(config.replaceFloatingGravel.get())
            .floodedUnderground(config.enableFloodedUnderground.get())
            .debugVisualizerEnabled(config.debugVisualizer.get())
            .debugVisualizerBlock(Blocks.field_150336_V.func_176223_P())
            .build()
      );
      carvers.removeIf(carverx -> carverx.getPriority() == 0);
      float maxPossibleNoiseThreshold = config.caveSpawnChance.get() * 0.01F * 2.0F - 1.0F;
      int totalPriority = carvers.stream().map(ICarver::getPriority).reduce(0, Integer::sum);
      float totalRangeLength = maxPossibleNoiseThreshold - -1.0F;
      float currNoise = -1.0F;
      BetterCaves.LOGGER.debug("CAVE INFORMATION");
      BetterCaves.LOGGER.debug("--> MAX POSSIBLE THRESHOLD: " + maxPossibleNoiseThreshold);
      BetterCaves.LOGGER.debug("--> TOTAL PRIORITY: " + totalPriority);
      BetterCaves.LOGGER.debug("--> TOTAL RANGE LENGTH: " + totalRangeLength);

      for (ICarver carver : carvers) {
         BetterCaves.LOGGER.debug("--> CARVER");
         float noiseRangeLength = (float)carver.getPriority() / totalPriority * totalRangeLength;
         float rangeTop = currNoise + noiseRangeLength;
         CarverNoiseRange range = new CarverNoiseRange(currNoise, rangeTop, carver);
         currNoise = rangeTop;
         this.noiseRanges.add(range);
         BetterCaves.LOGGER.debug("    --> RANGE FOUND: " + range);
      }
   }

   public void carveChunk(ChunkPrimer primer, int chunkX, int chunkZ, int[][] surfaceAltitudes, IBlockState[][] liquidBlocks) {
      if (this.noiseRanges.size() != 0 || this.isSurfaceCavesEnabled) {
         boolean shouldCarveVanillaCaves = false;
         boolean[][] vanillaCarvingMask = new boolean[16][16];

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
                     maxHeight = Math.max(maxHeight, range.getCarver().getTopY());
                  }
               }

               for (int offsetX = 0; offsetX < 4; offsetX++) {
                  for (int offsetZ = 0; offsetZ < 4; offsetZ++) {
                     int localX = startX + offsetX;
                     int localZ = startZ + offsetZ;
                     BlockPos colPos = new BlockPos(chunkX * 16 + localX, 1, chunkZ * 16 + localZ);
                     boolean flooded = this.isFloodedUndergroundEnabled
                        && !this.isDebugViewEnabled
                        && BiomeDictionary.hasType(this.world.func_180494_b(colPos), Type.OCEAN);
                     if (!flooded
                        || BiomeDictionary.hasType(this.world.func_180494_b(colPos.func_177974_f()), Type.OCEAN)
                           && BiomeDictionary.hasType(this.world.func_180494_b(colPos.func_177978_c()), Type.OCEAN)
                           && BiomeDictionary.hasType(this.world.func_180494_b(colPos.func_177976_e()), Type.OCEAN)
                           && BiomeDictionary.hasType(this.world.func_180494_b(colPos.func_177968_d()), Type.OCEAN)) {
                        int surfaceAltitude = surfaceAltitudes[localX][localZ];
                        IBlockState liquidBlock = liquidBlocks[localX][localZ];
                        float caveRegionNoise = this.caveRegionController.GetNoise(colPos.func_177958_n(), colPos.func_177952_p());

                        for (CarverNoiseRange range : this.noiseRanges) {
                           if (range.contains(caveRegionNoise)) {
                              if (range.getCarver() instanceof CaveCarver) {
                                 CaveCarver carver = (CaveCarver)range.getCarver();
                                 int bottomY = carver.getBottomY();
                                 int topY = Math.min(surfaceAltitude, carver.getTopY());
                                 if (this.isOverrideSurfaceDetectionEnabled) {
                                    topY = carver.getTopY();
                                    maxHeight = carver.getTopY();
                                 }

                                 if (this.isDebugViewEnabled) {
                                    topY = 128;
                                    maxHeight = 128;
                                 }

                                 if (range.getNoiseCube() == null) {
                                    range.setNoiseCube(carver.getNoiseGen().interpolateNoiseCube(startPos, endPos, bottomY, maxHeight));
                                 }

                                 NoiseColumn noiseColumn = range.getNoiseCube().get(offsetX).get(offsetZ);
                                 carver.carveColumn(primer, colPos, topY, noiseColumn, liquidBlock, flooded);
                                 break;
                              }

                              if (range.getCarver() instanceof VanillaCaveCarver) {
                                 vanillaCarvingMask[localX][localZ] = true;
                                 shouldCarveVanillaCaves = true;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         if (shouldCarveVanillaCaves) {
            VanillaCaveCarver carverx = null;

            for (CarverNoiseRange rangex : this.noiseRanges) {
               if (rangex.getCarver() instanceof VanillaCaveCarver) {
                  carverx = (VanillaCaveCarver)rangex.getCarver();
                  break;
               }
            }

            if (carverx != null) {
               carverx.generate(this.world, chunkX, chunkZ, primer, true, liquidBlocks, vanillaCarvingMask);
            }
         }

         if (this.isSurfaceCavesEnabled) {
            this.surfaceCaveCarver.generate(this.world, chunkX, chunkZ, primer, false, liquidBlocks);
         }
      }
   }

   private float calcCaveRegionSize(RegionSize caveRegionSize, float caveRegionCustomSize) {
      switch (caveRegionSize) {
         case Small:
            return 0.008F;
         case Large:
            return 0.0032F;
         case ExtraLarge:
            return 0.001F;
         case Custom:
            return caveRegionCustomSize;
         default:
            return 0.005F;
      }
   }
}
