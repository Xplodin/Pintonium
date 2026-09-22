package sayys.depthsupdate.world.generation.noise;

import java.util.Arrays;
import java.util.Random;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class AquiferSampler {
   private static final int X_SPACING = 16;
   private static final int Y_SPACING = 12;
   private static final int Z_SPACING = 16;
   private static final int X_JITTER = 10;
   private static final int Y_JITTER = 9;
   private static final int Z_JITTER = 10;
   private static final double SIMILARITY_RANGE = 25.0;
   private static final int NOT_FLOODED = -32768;
   private static final double FULL_FLOOD_THRESHOLD = 0.8;
   private static final double PARTIAL_FLOOD_THRESHOLD = 0.4;
   private static final int LAVA_MAX_LEVEL = -10;
   private static final double LAVA_CHANCE_THRESHOLD = 0.3;
   private static final double NOISE_SCALE = 5.0;
   private static final double BARRIER_WAVELENGTH = 8.0;
   private static final double BARRIER_Y_SCALE = 0.5;
   private static final double FLOODEDNESS_WAVELENGTH = 128.0;
   private static final double FLOODEDNESS_Y_SCALE = 0.67;
   private static final double SPREAD_WAVELENGTH = 32.0;
   private static final double SPREAD_XZ_Y_SCALE = 0.7142857142857143;
   private static final double LAVA_WAVELENGTH = 2.0;
   private static final int FLUID_CELL_XZ = 16;
   private static final int FLUID_CELL_Y = 40;
   private static final int LAVA_CELL_XZ = 64;
   private static final int LAVA_CELL_Y = 40;
   private final Perlin barrierNoise;
   private final Perlin floodednessNoise;
   private final Perlin spreadNoise;
   private final Perlin lavaNoise;
   private final long worldSeed;
   private final int lavaLevel;
   private final int seaLevel;
   private final int bandMinY;
   private final int bandMaxY;
   private long[] locationCache;
   private int[] levelCache;
   private boolean[] lavaCache;
   private boolean[] cacheValid;
   private int minGridX;
   private int minGridY;
   private int minGridZ;
   private int gridSizeX;
   private int gridSizeY;
   private int gridSizeZ;

   public AquiferSampler(long seed, int lavaLevel, int seaLevel, int bandMinY, int bandMaxY) {
      this.worldSeed = seed;
      this.lavaLevel = lavaLevel;
      this.seaLevel = seaLevel;
      this.bandMinY = bandMinY;
      this.bandMaxY = bandMaxY;
      this.barrierNoise = createNoise((int)seed + 10000, 0.125);
      this.floodednessNoise = createNoise((int)seed + 10001, 0.0078125);
      this.spreadNoise = createNoise((int)seed + 10002, 0.03125);
      this.lavaNoise = createNoise((int)seed + 10003, 0.5);
   }

   private static Perlin createNoise(int seed, double frequency) {
      Perlin perlin = new Perlin();
      perlin.setSeed(seed);
      perlin.setOctaveCount(1);
      perlin.setFrequency(frequency);
      return perlin;
   }

   public void prepare(int chunkX, int chunkZ) {
      int worldX = chunkX * 16;
      int worldZ = chunkZ * 16;
      this.minGridX = gridXZ(worldX - 5);
      int maxGridX = gridXZ(worldX + 15 - 5) + 1;
      this.gridSizeX = maxGridX - this.minGridX + 1;
      this.minGridY = gridY(this.bandMinY + 1) - 1;
      int maxGridY = gridY(this.bandMaxY + 1) + 1;
      this.gridSizeY = maxGridY - this.minGridY + 1;
      this.minGridZ = gridXZ(worldZ - 5);
      int maxGridZ = gridXZ(worldZ + 15 - 5) + 1;
      this.gridSizeZ = maxGridZ - this.minGridZ + 1;
      int cacheSize = this.gridSizeX * this.gridSizeY * this.gridSizeZ;
      if (this.locationCache != null && this.locationCache.length >= cacheSize) {
         Arrays.fill(this.cacheValid, 0, cacheSize, false);
      } else {
         this.locationCache = new long[cacheSize];
         this.levelCache = new int[cacheSize];
         this.lavaCache = new boolean[cacheSize];
         this.cacheValid = new boolean[cacheSize];
      }
   }

   public AquiferSampler.Substance substanceAt(int x, int y, int z, double density) {
      if (density > 0.0) {
         return AquiferSampler.Substance.SOLID;
      } else if (y < Math.min(this.lavaLevel, this.seaLevel)) {
         return AquiferSampler.Substance.LAVA;
      } else {
         int anchorX = gridXZ(x - 5);
         int anchorY = gridY(y + 1);
         int anchorZ = gridXZ(z - 5);
         int dist1 = Integer.MAX_VALUE;
         int dist2 = Integer.MAX_VALUE;
         int dist3 = Integer.MAX_VALUE;
         int idx1 = -1;
         int idx2 = -1;
         int idx3 = -1;

         for (int gx = 0; gx <= 1; gx++) {
            for (int gy = -1; gy <= 1; gy++) {
               for (int gz = 0; gz <= 1; gz++) {
                  int index = this.cellIndex(anchorX + gx, anchorY + gy, anchorZ + gz);
                  if (index >= 0) {
                     long location = this.cellLocation(anchorX + gx, anchorY + gy, anchorZ + gz, index);
                     int dx = unpackX(location) - x;
                     int dy = unpackY(location) - y;
                     int dz = unpackZ(location) - z;
                     int distSq = dx * dx + dy * dy + dz * dz;
                     if (distSq <= dist1) {
                        dist3 = dist2;
                        idx3 = idx2;
                        dist2 = dist1;
                        idx2 = idx1;
                        dist1 = distSq;
                        idx1 = index;
                     } else if (distSq <= dist2) {
                        dist3 = dist2;
                        idx3 = idx2;
                        dist2 = distSq;
                        idx2 = index;
                     } else if (distSq <= dist3) {
                        dist3 = distSq;
                        idx3 = index;
                     }
                  }
               }
            }
         }

         if (idx1 < 0) {
            return AquiferSampler.Substance.AIR;
         } else {
            AquiferSampler.Substance fluid1 = this.fluidAt(idx1, y);
            double similarity12 = similarity(dist1, dist2);
            if (similarity12 <= 0.0) {
               return fluid1;
            } else if (fluid1 == AquiferSampler.Substance.WATER && y - 1 < Math.min(this.lavaLevel, this.seaLevel)) {
               return AquiferSampler.Substance.WATER;
            } else {
               double[] barrierHolder = new double[]{Double.NaN};
               double pressure12 = similarity12 * this.pressure(x, y, z, idx1, idx2, barrierHolder);
               if (density + pressure12 > 0.0) {
                  return AquiferSampler.Substance.SOLID;
               } else {
                  if (idx3 >= 0) {
                     double similarity13 = similarity(dist1, dist3);
                     if (similarity13 > 0.0 && density + similarity12 * similarity13 * this.pressure(x, y, z, idx1, idx3, barrierHolder) > 0.0) {
                        return AquiferSampler.Substance.SOLID;
                     }

                     double similarity23 = similarity(dist2, dist3);
                     if (similarity23 > 0.0 && density + similarity12 * similarity23 * this.pressure(x, y, z, idx2, idx3, barrierHolder) > 0.0) {
                        return AquiferSampler.Substance.SOLID;
                     }
                  }

                  return fluid1;
               }
            }
         }
      }
   }

   private AquiferSampler.Substance fluidAt(int index, int y) {
      int level = this.levelCache[index];
      if (level != -32768 && y < level) {
         return this.lavaCache[index] ? AquiferSampler.Substance.LAVA : AquiferSampler.Substance.WATER;
      } else {
         return AquiferSampler.Substance.AIR;
      }
   }

   private static double similarity(int distSq1, int distSq2) {
      return 1.0 - (distSq2 - distSq1) / 25.0;
   }

   private double pressure(int x, int y, int z, int indexA, int indexB, double[] barrierHolder) {
      AquiferSampler.Substance typeA = this.fluidAt(indexA, y);
      AquiferSampler.Substance typeB = this.fluidAt(indexB, y);
      if ((typeA != AquiferSampler.Substance.LAVA || typeB != AquiferSampler.Substance.WATER)
         && (typeA != AquiferSampler.Substance.WATER || typeB != AquiferSampler.Substance.LAVA)) {
         if (typeA == AquiferSampler.Substance.AIR != (typeB == AquiferSampler.Substance.AIR)) {
            return 2.0;
         } else {
            int levelA = this.levelCache[indexA];
            int levelB = this.levelCache[indexB];
            int levelDiff = Math.abs(levelA - levelB);
            if (levelDiff == 0) {
               return 0.0;
            } else {
               double averageLevel = 0.5 * (levelA + levelB);
               double aboveAverage = y + 0.5 - averageLevel;
               double edgeDistance = levelDiff / 2.0 - Math.abs(aboveAverage);
               double gradient;
               if (aboveAverage > 0.0) {
                  gradient = edgeDistance > 0.0 ? edgeDistance / 1.5 : edgeDistance / 2.5;
               } else {
                  double fromBottom = 3.0 + edgeDistance;
                  gradient = fromBottom > 0.0 ? fromBottom / 3.0 : fromBottom / 10.0;
               }

               double noise = 0.0;
               if (gradient >= -2.0 && gradient <= 2.0) {
                  if (Double.isNaN(barrierHolder[0])) {
                     barrierHolder[0] = this.barrierNoise.getValue(x, y * 0.5, z) * 5.0;
                  }

                  noise = barrierHolder[0];
               }

               return 2.0 * (noise + gradient);
            }
         }
      } else {
         return 2.0;
      }
   }

   private long cellLocation(int gridX, int gridY, int gridZ, int index) {
      if (this.cacheValid[index]) {
         return this.locationCache[index];
      } else {
         Random cellRandom = new Random(hashCell(gridX, gridY, gridZ, this.worldSeed));
         int centerX = gridX * 16 + cellRandom.nextInt(10);
         int centerY = gridY * 12 + cellRandom.nextInt(9);
         int centerZ = gridZ * 16 + cellRandom.nextInt(10);
         long location = packPos(centerX, centerY, centerZ);
         AquiferSampler.CellStatus status = this.computeCellStatus(centerX, centerY, centerZ);
         this.locationCache[index] = location;
         this.levelCache[index] = status.level();
         this.lavaCache[index] = status.lava();
         this.cacheValid[index] = true;
         return location;
      }
   }

   public AquiferSampler.CellStatus computeCellStatus(int centerX, int centerY, int centerZ) {
      int globalLevel = centerY < Math.min(this.lavaLevel, this.seaLevel) ? this.lavaLevel : this.seaLevel;
      boolean globalLava = centerY < Math.min(this.lavaLevel, this.seaLevel);
      double floodedness = clamp(this.floodednessNoise.getValue(centerX, centerY * 0.67, centerZ) * 5.0, -1.0, 1.0);
      int level;
      if (floodedness > 0.8) {
         level = globalLevel;
      } else if (floodedness > 0.4) {
         level = this.randomizedLevel(centerX, centerY, centerZ);
      } else {
         level = -32768;
      }

      boolean lava = globalLava;
      if (!globalLava && level != -32768 && level <= -10) {
         double lavaValue = this.lavaNoise.getValue(Math.floorDiv(centerX, 64), Math.floorDiv(centerY, 40), Math.floorDiv(centerZ, 64)) * 5.0;
         lava = Math.abs(lavaValue) > 0.3;
      }

      return new AquiferSampler.CellStatus(level, lava);
   }

   private int randomizedLevel(int x, int y, int z) {
      int cellX = Math.floorDiv(x, 16);
      int cellY = Math.floorDiv(y, 40);
      int cellZ = Math.floorDiv(z, 16);
      double spread = this.spreadNoise.getValue(cellX * 0.7142857142857143, cellY * 0.7142857142857143, cellZ * 0.7142857142857143) * 5.0 * 10.0;
      int target = cellY * 40 + 20 + quantize(spread, 3);
      return Math.min(this.seaLevel, target);
   }

   private static int gridXZ(int blockCoord) {
      return blockCoord >> 4;
   }

   private static int gridY(int blockCoord) {
      return Math.floorDiv(blockCoord, 12);
   }

   private int cellIndex(int gridX, int gridY, int gridZ) {
      int x = gridX - this.minGridX;
      int y = gridY - this.minGridY;
      int z = gridZ - this.minGridZ;
      return x >= 0 && y >= 0 && z >= 0 && x < this.gridSizeX && y < this.gridSizeY && z < this.gridSizeZ ? (y * this.gridSizeZ + z) * this.gridSizeX + x : -1;
   }

   private static long packPos(int x, int y, int z) {
      return (x & 67108863L) << 38 | (y & 4095L) << 26 | z & 67108863L;
   }

   private static int unpackX(long packed) {
      return (int)(packed >> 38);
   }

   private static int unpackY(long packed) {
      return (int)(packed << 26 >> 52);
   }

   private static int unpackZ(long packed) {
      return (int)(packed << 38 >> 38);
   }

   private static long hashCell(int gx, int gy, int gz, long seed) {
      long hash = seed * 6364136223846793005L + 1442695040888963407L;
      hash += gx;
      hash = hash * 6364136223846793005L + 1442695040888963407L;
      hash += gy;
      hash = hash * 6364136223846793005L + 1442695040888963407L;
      hash += gz;
      return hash * 6364136223846793005L + 1442695040888963407L;
   }

   private static int quantize(double value, int step) {
      return (int)Math.floor(value / step) * step;
   }

   private static double clamp(double value, double min, double max) {
      return value < min ? min : Math.min(value, max);
   }

   public record CellStatus(int level, boolean lava) {
   }

   public static enum Substance {
      SOLID,
      AIR,
      WATER,
      LAVA;
   }
}
