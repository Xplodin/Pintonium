package sayys.depthsupdate.world.generation.noise;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.GenerationLog;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

public class CaveNoiseGenerator {
   private static final int DEEP_BAND_MAX_Y = 30;
   private static final int TERRAIN_MAX_Y = 255;
   private static final int DEFAULT_MIN_Y_OFFSET = 4;
   private static final int WATER_SUPPORT_DEPTH = 3;
   private final List<ICaveGenerator> generators = new ArrayList<>();
   private final double offsetX;
   private final double offsetY;
   private final double offsetZ;
   private final int caveMinY;
   private final int caveMaxY;
   private final int deepBandMaxY;
   private final HeightContext ctx;
   private final PillarGenerator pillarGenerator;
   private final AquiferSampler aquifer;
   private final UndergroundRiverGenerator riverGen;
   private final int[][] riverSpans;
   private final int[] surfaceHeights = new int[256];
   private final Block deepslateBlock = BlockUtils.getDeepslateBlockState().func_177230_c();

   public CaveNoiseGenerator(@NonNull World world) {
      long seed = world.func_72905_C();
      Random rand = new Random(seed);
      this.offsetX = rand.nextDouble() * 100000.0;
      this.offsetY = rand.nextDouble() * 100000.0;
      this.offsetZ = rand.nextDouble() * 100000.0;
      HeightContext ctx = HeightManager.get(world);
      this.ctx = ctx;
      this.caveMinY = ctx.minY() + 4;
      int deepBandMaxY = Math.min(30, ctx.maxY() - 1);
      int terrainMaxY = Math.min(255, ctx.maxY() - 1);
      this.deepBandMaxY = deepBandMaxY;
      this.caveMaxY = terrainMaxY;
      this.generators.add(new CheeseCaveGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, this.caveMinY, terrainMaxY));
      this.generators.add(new SpaghettiCaveGenerator(seed));
      this.generators.add(new NoodleCaveGenerator(seed, this.caveMinY, terrainMaxY));
      this.generators.add(new CaveEntranceGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, this.caveMinY, terrainMaxY));
      this.pillarGenerator = new PillarGenerator(seed, this.offsetX, this.offsetY, this.offsetZ, this.caveMinY, deepBandMaxY);
      this.aquifer = DepthsUpdateConfig.aquifers.enableAquifers ? new AquiferSampler(seed, ctx.lavaLevel(), ctx.seaLevel(), this.caveMinY, deepBandMaxY) : null;
      this.riverGen = DepthsUpdateConfig.generateUndergroundRivers ? new UndergroundRiverGenerator(world) : null;
      this.riverSpans = this.riverGen != null ? new int[324][] : null;
   }

   private void prepareRiverSpans(int worldX, int worldZ) {
      for (int dx = -1; dx <= 16; dx++) {
         for (int dz = -1; dz <= 16; dz++) {
            this.riverSpans[(dx + 1) * 18 + dz + 1] = this.riverGen.waterSpan(worldX + dx, worldZ + dz);
         }
      }
   }

   private boolean riverNear(int x, int y, int z) {
      int lowY = y - 1;
      int highY = y + 3;

      for (int nx = x - 1; nx <= x + 1; nx++) {
         for (int nz = z - 1; nz <= z + 1; nz++) {
            int[] span = this.riverSpans[(nx + 1) * 18 + nz + 1];
            if (span != null && highY >= span[0] && lowY <= span[1]) {
               return true;
            }
         }
      }

      return false;
   }

   public void generate(int chunkX, int chunkZ, ChunkPrimer primer, Biome[] biomes) {
      int worldX = chunkX * 16;
      int worldZ = chunkZ * 16;
      IBlockState air = Blocks.field_150350_a.func_176223_P();
      IBlockState stone = Blocks.field_150348_b.func_176223_P();
      IBlockState water = Blocks.field_150355_j.func_176223_P();
      IBlockState lava = Blocks.field_150353_l.func_176223_P();
      CaveSampleContext context = new CaveSampleContext();
      boolean generatePillars = DepthsUpdateConfig.generateCavePillars;
      boolean logging = GenerationLog.enabled();
      int highestSurface = this.readSurfaceHeights(primer);

      for (ICaveGenerator gen : this.generators) {
         if (gen.canGenerate()) {
            gen.prepare(chunkX, chunkZ, highestSurface);
         }
      }

      if (generatePillars) {
         this.pillarGenerator.prepare(chunkX, chunkZ);
      }

      if (this.aquifer != null) {
         this.aquifer.prepare(chunkX, chunkZ);
      }

      boolean guardWater = this.chunkHasWater(primer, highestSurface);
      if (this.riverGen != null) {
         this.prepareRiverSpans(worldX, worldZ);
      }

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            double realX = worldX + x + this.offsetX;
            double realZ = worldZ + z + this.offsetZ;
            Biome biome = biomes[x + z * 16];
            IBlockState topBlock = biome.field_76752_A;
            IBlockState fillerBlock = biome.field_76753_B;
            int surface = this.surfaceHeights[x << 4 | z];
            int columnMaxY = Math.min(this.caveMaxY, surface);

            for (int y = this.caveMinY; y <= columnMaxY; y++) {
               IBlockState currentState = primer.func_177856_a(x, y, z);
               if (this.isCarvable(currentState.func_177230_c())
                  || isLooseSurface(currentState.func_177230_c()) && primer.func_177856_a(x, y + 1, z).func_185904_a() != Material.field_151586_h) {
                  double realY = y + this.offsetY;
                  context.reset(realX, realY, realZ, x, y, z, surface - y);

                  for (ICaveGenerator genx : this.generators) {
                     if (genx.canGenerate()) {
                        genx.sample(context);
                     }
                  }

                  if (context.shouldCarve() && generatePillars && this.pillarGenerator.isPillar(x, y, z)) {
                     if (logging) {
                        GenerationLog.pillarBlocked();
                     }
                  } else if (!context.shouldCarve()) {
                     if (context.shouldDebug) {
                        primer.func_177855_a(x, y, z, context.debugBlock);
                     }
                  } else {
                     boolean nearRiver = this.riverGen != null && this.riverNear(x, y, z);
                     if (!nearRiver) {
                        if (this.aquifer != null && y <= this.deepBandMaxY) {
                           switch (this.aquifer.substanceAt(worldX + x, y, worldZ + z, context.density)) {
                              case SOLID:
                              default:
                                 break;
                              case AIR:
                                 primer.func_177855_a(x, y, z, air);
                                 if (logging) {
                                    GenerationLog.carved(context.openMask);
                                 }
                                 break;
                              case WATER:
                                 primer.func_177855_a(x, y, z, water);
                                 if (logging) {
                                    GenerationLog.aquiferWater();
                                 }
                                 break;
                              case LAVA:
                                 primer.func_177855_a(x, y, z, lava);
                                 if (logging) {
                                    GenerationLog.aquiferLava();
                                 }
                           }
                        } else if (!guardWater || this.isSafeToCarve(primer, x, y, z)) {
                           if (y < this.ctx.lavaLevel()) {
                              primer.func_177855_a(x, y, z, lava);
                           } else {
                              primer.func_177855_a(x, y, z, air);
                              if (currentState == topBlock && primer.func_177856_a(x, y - 1, z) == fillerBlock) {
                                 primer.func_177855_a(x, y - 1, z, topBlock);
                              }
                           }

                           if (logging) {
                              GenerationLog.carved(context.openMask);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      this.ensureOpaqueGround(primer);
      if (logging) {
         GenerationLog.chunkGenerated();
      }
   }

   private boolean isCarvable(Block block) {
      return block == Blocks.field_150348_b
         || block == this.deepslateBlock
         || block == Blocks.field_150346_d
         || block == Blocks.field_150349_c
         || block == Blocks.field_150405_ch
         || block == Blocks.field_150406_ce
         || block == Blocks.field_150322_A
         || block == Blocks.field_180395_cM
         || block == Blocks.field_150391_bh;
   }

   private static boolean isLooseSurface(Block block) {
      return block == Blocks.field_150354_m || block == Blocks.field_150351_n;
   }

   private int readSurfaceHeights(ChunkPrimer primer) {
      int highest = this.caveMinY;

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int surface = this.ctx.minY();

            for (int y = this.caveMaxY; y >= this.caveMinY; y--) {
               Block block = primer.func_177856_a(x, y, z).func_177230_c();
               if (this.isCarvable(block) || isLooseSurface(block)) {
                  surface = y;
                  break;
               }
            }

            this.surfaceHeights[x << 4 | z] = surface;
            if (surface > highest) {
               highest = surface;
            }
         }
      }

      return highest;
   }

   private void ensureOpaqueGround(ChunkPrimer primer) {
      IBlockState stone = Blocks.field_150348_b.func_176223_P();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            int surface = this.surfaceHeights[x << 4 | z];
            if (surface >= 1) {
               boolean opaque = false;

               for (int y = surface; y >= 1; y--) {
                  Material material = primer.func_177856_a(x, y, z).func_185904_a();
                  if (material != Material.field_151579_a && material != Material.field_151586_h && material != Material.field_151587_i) {
                     opaque = true;
                     break;
                  }
               }

               if (!opaque) {
                  primer.func_177855_a(x, 1, z, stone);
                  primer.func_177855_a(x, 2, z, stone);
                  if (GenerationLog.enabled()) {
                     GenerationLog.groundPlug();
                  }
               }
            }
         }
      }
   }

   private boolean isSafeToCarve(ChunkPrimer primer, int x, int y, int z) {
      int lowY = Math.max(y - 1, this.ctx.minY());
      int highY = Math.min(y + 3, this.ctx.maxY() - 1);

      for (int nx = Math.max(x - 1, 0); nx <= Math.min(x + 1, 15); nx++) {
         for (int nz = Math.max(z - 1, 0); nz <= Math.min(z + 1, 15); nz++) {
            for (int ny = lowY; ny <= highY; ny++) {
               if (isWater(primer.func_177856_a(nx, ny, nz))) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   private static boolean isWater(IBlockState state) {
      return state.func_177230_c() == Blocks.field_150355_j || state.func_177230_c() == Blocks.field_150358_i;
   }

   private boolean chunkHasWater(ChunkPrimer primer, int highestSurface) {
      int lowY = Math.max(this.caveMinY - 1, this.ctx.minY());
      int highY = Math.min(highestSurface + 3, this.ctx.maxY() - 1);

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = lowY; y <= highY; y++) {
               if (isWater(primer.func_177856_a(x, y, z))) {
                  return true;
               }
            }
         }
      }

      return false;
   }
}
