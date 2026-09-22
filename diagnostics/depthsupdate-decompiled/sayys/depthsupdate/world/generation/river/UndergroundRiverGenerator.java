package sayys.depthsupdate.world.generation.river;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.RidgedMulti;

public class UndergroundRiverGenerator {
   private static final double BAND_MIN = 0.6;
   private static final double BAND_MAX = 2.0;
   private final Perlin perlinNoise;
   private final RidgedMulti ridgedMultiNoise = new RidgedMulti();
   private final World world;
   private final int baseY;
   private final int waterLevel;
   protected static final IBlockState AIR = Blocks.field_150350_a.func_176223_P();
   protected static final IBlockState WATER = Blocks.field_150355_j.func_176223_P();
   protected static final IBlockState STONE = Blocks.field_150348_b.func_176223_P();

   public UndergroundRiverGenerator(World worldIn) {
      this.world = worldIn;
      long seed = worldIn.func_72905_C();
      this.ridgedMultiNoise.setSeed((int)seed);
      this.ridgedMultiNoise.setFrequency(0.005);
      this.ridgedMultiNoise.setOctaveCount(1);
      this.ridgedMultiNoise.setLacunarity(0.0);
      this.perlinNoise = new Perlin();
      this.perlinNoise.setSeed((int)seed);
      this.perlinNoise.setFrequency(0.1);
      this.perlinNoise.setOctaveCount(2);
      this.perlinNoise.setLacunarity(0.0);
      this.perlinNoise.setPersistence(-0.4);
      HeightContext heightCtx = HeightManager.get(worldIn);
      this.baseY = heightCtx.minY() + (0 - heightCtx.minY()) / 3;
      this.waterLevel = this.baseY - 3;
   }

   private int channelHeight(double vnoise, int realX, int realZ) {
      int height = this.calculateHeightByCenter(vnoise, 0.6, 0.625, 1, 10, 17, 0.63);
      if (height == 10 || height == 9) {
         double pnoise = this.perlinNoise.getValue(realX, 1.0, realZ);
         if (pnoise >= 0.0) {
            height++;
            if (pnoise >= 0.08) {
               height++;
            }
         } else if (pnoise < -0.14) {
            height--;
         }
      }

      return height;
   }

   public int[] waterSpan(int realX, int realZ) {
      double vnoise = this.ridgedMultiNoise.getValue(realX, 1.0, realZ);
      if (!(vnoise < 0.6) && !(vnoise > 2.0)) {
         int height = this.channelHeight(vnoise, realX, realZ);
         int top = this.baseY + height / 2;
         int bottom = this.baseY - height + 1;
         int waterTop = Math.min(this.waterLevel, top);
         return waterTop >= bottom ? new int[]{bottom, waterTop} : null;
      } else {
         return null;
      }
   }

   public boolean waterWithin(int realX, int realZ, int yMin, int yMax) {
      if (yMin > this.waterLevel) {
         return false;
      } else {
         int[] span = this.waterSpan(realX, realZ);
         return span != null && yMax >= span[0] && yMin <= span[1];
      }
   }

   private double calculatePercent(double noise, double minNoise, double maxNoise) {
      double maxNoiseCalc = maxNoise - minNoise;
      double noiseCalc = noise - minNoise;
      return noiseCalc / maxNoiseCalc;
   }

   public int calculateHeightByCenter(
      double noise, double minNoise, double maxNoise, int minHeight, int maxHeight, int maxHeightIfCavern, double cavernWhenNoise
   ) {
      double noiseCenter = (maxNoise - minNoise) / 2.0 + minNoise;
      double noisePercent = 0.0;
      if (noiseCenter > noise) {
         noisePercent = this.calculatePercent(noise, minNoise, noiseCenter);
      } else if (noiseCenter < noise) {
         if (noise > cavernWhenNoise) {
            noisePercent = this.calculatePercent(noise, maxHeightIfCavern, noiseCenter);
         } else {
            noisePercent = this.calculatePercent(noise, maxHeight, noiseCenter);
         }
      } else {
         noisePercent = 1.0;
      }

      int maxHeightCalc = maxHeight - minHeight;
      double height = maxHeightCalc * noisePercent;
      return (int)Math.round(height) + minHeight;
   }

   public void generate(int chunkX, int chunkZ, ChunkPrimer primer) {
      HeightContext heightCtx = HeightManager.get(this.world);
      IBlockState deepslate = BlockUtils.getDeepslateBlockState();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int realX = chunkX * 16 + x;
            int realZ = chunkZ * 16 + z;
            double vnoise = this.ridgedMultiNoise.getValue(realX, 1.0, realZ);
            if (vnoise >= 0.6 && vnoise <= 2.0) {
               int height = this.channelHeight(vnoise, realX, realZ);
               int top = this.baseY + height / 2;
               int bottom = this.baseY - height + 1;

               for (int a = top; a >= bottom; a--) {
                  if (a >= heightCtx.minY() && a < heightCtx.maxY()) {
                     IBlockState current = primer.func_177856_a(x, a, z);
                     if (current.func_177230_c() == Blocks.field_150348_b || current == deepslate || current.func_177230_c() == deepslate.func_177230_c()) {
                        if (a > this.waterLevel) {
                           primer.func_177855_a(x, a, z, AIR);
                        } else {
                           primer.func_177855_a(x, a, z, WATER);
                        }
                     }
                  }
               }

               int waterTop = Math.min(this.waterLevel, top);

               for (int ax = Math.min(waterTop, heightCtx.maxY() - 1); ax >= Math.max(bottom, heightCtx.minY() + 1); ax--) {
                  if (primer.func_177856_a(x, ax, z).func_177230_c() == Blocks.field_150355_j
                     && primer.func_177856_a(x, ax - 1, z).func_177230_c() == Blocks.field_150350_a) {
                     primer.func_177855_a(x, ax - 1, z, ax - 1 < 0 ? deepslate : STONE);
                  }
               }

               if (DepthsUpdateConfig.DEBUG.enableDebugVisualizers) {
                  IBlockState riverDebug = BlockUtils.getRiverDebugBlockState();
                  int topShell = top + 1;
                  int bottomShell = bottom - 1;
                  if (topShell < heightCtx.maxY() && topShell >= heightCtx.minY()) {
                     IBlockState shellState = primer.func_177856_a(x, topShell, z);
                     if (shellState != AIR && shellState != WATER) {
                        primer.func_177855_a(x, topShell, z, riverDebug);
                     }
                  }

                  if (bottomShell < heightCtx.maxY() && bottomShell >= heightCtx.minY()) {
                     IBlockState shellState = primer.func_177856_a(x, bottomShell, z);
                     if (shellState != AIR && shellState != WATER) {
                        primer.func_177855_a(x, bottomShell, z, riverDebug);
                     }
                  }
               }
            }
         }
      }
   }
}
