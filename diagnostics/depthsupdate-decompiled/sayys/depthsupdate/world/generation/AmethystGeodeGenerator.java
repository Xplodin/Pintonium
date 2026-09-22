package sayys.depthsupdate.world.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockAmethystCluster;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.registry.AmethystRegistry;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class AmethystGeodeGenerator implements IWorldGenerator {
   private static final double FILLING = 1.7;
   private static final double INNER_LAYER = 2.2;
   private static final double MIDDLE_LAYER = 3.2;
   private static final double OUTER_LAYER = 4.2;
   private static final double CRACK_CHANCE = 0.95;
   private static final double BASE_CRACK_SIZE = 2.0;
   private static final int CRACK_POINT_OFFSET = 2;
   private static final double USE_POTENTIAL_PLACEMENTS_CHANCE = 0.35;
   private static final double USE_ALTERNATE_LAYER0_CHANCE = 0.083;
   private static final int OUTER_WALL_DIST_MIN = 4;
   private static final int OUTER_WALL_DIST_MAX = 6;
   private static final int DISTRIBUTION_POINTS_MIN = 3;
   private static final int DISTRIBUTION_POINTS_MAX = 4;
   private static final int POINT_OFFSET_MIN = 1;
   private static final int POINT_OFFSET_MAX = 2;
   private static final int MIN_GEN_OFFSET = -16;
   private static final int MAX_GEN_OFFSET = 16;
   private static final double NOISE_MULTIPLIER = 0.05;
   private static final int INVALID_BLOCKS_THRESHOLD = 1;
   private static final EnumFacing[] ALL_FACINGS = EnumFacing.values();

   public static void register() {
      if (DepthsUpdateConfig.amethystGeodes.enableAmethystGeodes
         && DepthsUpdateConfig.REGISTRY.enableAmethystFamily
         && DepthsUpdateConfig.REGISTRY.enableSmoothBasalt
         && DepthsUpdateConfig.REGISTRY.enableCalcite) {
         GameRegistry.registerWorldGenerator(new AmethystGeodeGenerator(), 120);
      }
   }

   public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (world.field_73011_w.getDimension() == 0) {
         if (!(chunkGenerator instanceof ChunkGeneratorFlat) && !(chunkGenerator instanceof ChunkGeneratorDebug)) {
            int rarity = DepthsUpdateConfig.amethystGeodes.geodeRarity;
            if (random.nextInt(rarity) == 0) {
               int y = CaveRegion.randomYInWindow(
                  random, HeightManager.get(world), DepthsUpdateConfig.amethystGeodes.geodeMinY, DepthsUpdateConfig.amethystGeodes.geodeMaxY
               );
               if (y != Integer.MIN_VALUE) {
                  int x = chunkX * 16 + 8 + random.nextInt(16);
                  int z = chunkZ * 16 + 8 + random.nextInt(16);
                  BlockPos origin = new BlockPos(x, y, z);
                  GenerationLog.featurePlaced("amethyst geode", origin);
                  this.generateGeode(world, random, origin);
               }
            }
         }
      }
   }

   private void generateGeode(World world, Random random, BlockPos origin) {
      int numPoints = uniformInt(random, 3, 4);
      long noiseSeed = world.func_72905_C() ^ 25214903917L;
      double crackSizeAdjustment = numPoints / 6.0;
      double thresholdAir = 1.0 / Math.sqrt(1.7);
      double thresholdInnerBlock = 1.0 / Math.sqrt(2.2 + crackSizeAdjustment);
      double thresholdMiddle = 1.0 / Math.sqrt(3.2 + crackSizeAdjustment);
      double thresholdOuter = 1.0 / Math.sqrt(4.2 + crackSizeAdjustment);
      double crackThreshold = 1.0 / Math.sqrt(2.0 + random.nextDouble() / 2.0 + (numPoints > 3 ? crackSizeAdjustment : 0.0));
      boolean shouldGenerateCrack = random.nextFloat() < 0.95;
      List<long[]> points = new ArrayList<>();
      int numInvalidPoints = 0;

      for (int i = 0; i < numPoints; i++) {
         int dx = uniformInt(random, 4, 6);
         int dy = uniformInt(random, 4, 6);
         int dz = uniformInt(random, 4, 6);
         BlockPos pointPos = origin.func_177982_a(dx, dy, dz);
         IBlockState stateAtPoint = world.func_180495_p(pointPos);
         if (stateAtPoint.func_177230_c().isAir(stateAtPoint, world, pointPos) || isInvalidBlock(stateAtPoint)) {
            if (++numInvalidPoints > 1) {
               return;
            }
         }

         int pointOffset = uniformInt(random, 1, 2);
         points.add(new long[]{pointPos.func_177958_n(), pointPos.func_177956_o(), pointPos.func_177952_p(), pointOffset});
      }

      List<BlockPos> crackPoints = new ArrayList<>();
      if (shouldGenerateCrack) {
         int crackDir = random.nextInt(4);
         int crackOffset = numPoints * 2 + 1;
         if (crackDir == 0) {
            crackPoints.add(origin.func_177982_a(crackOffset, 7, 0));
            crackPoints.add(origin.func_177982_a(crackOffset, 5, 0));
            crackPoints.add(origin.func_177982_a(crackOffset, 1, 0));
         } else if (crackDir == 1) {
            crackPoints.add(origin.func_177982_a(0, 7, crackOffset));
            crackPoints.add(origin.func_177982_a(0, 5, crackOffset));
            crackPoints.add(origin.func_177982_a(0, 1, crackOffset));
         } else if (crackDir == 2) {
            crackPoints.add(origin.func_177982_a(crackOffset, 7, crackOffset));
            crackPoints.add(origin.func_177982_a(crackOffset, 5, crackOffset));
            crackPoints.add(origin.func_177982_a(crackOffset, 1, crackOffset));
         } else {
            crackPoints.add(origin.func_177982_a(0, 7, 0));
            crackPoints.add(origin.func_177982_a(0, 5, 0));
            crackPoints.add(origin.func_177982_a(0, 1, 0));
         }
      }

      List<BlockPos> potentialCrystalPlacements = new ArrayList<>();
      BlockPos cornerMin = origin.func_177982_a(-16, -16, -16);
      BlockPos cornerMax = origin.func_177982_a(16, 16, 16);

      for (BlockPos pos : BlockPos.func_177980_a(cornerMin, cornerMax)) {
         double noiseValue = sampleNoise(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), noiseSeed) * 0.05;
         double distSumShell = 0.0;

         for (long[] point : points) {
            double dx = pos.func_177958_n() - point[0];
            double dy = pos.func_177956_o() - point[1];
            double dz = pos.func_177952_p() - point[2];
            double distSq = dx * dx + dy * dy + dz * dz + point[3];
            distSumShell += fastInvSqrt(distSq) + noiseValue;
         }

         double distSumCrack = 0.0;

         for (BlockPos crackPoint : crackPoints) {
            double dx = pos.func_177958_n() - crackPoint.func_177958_n();
            double dy = pos.func_177956_o() - crackPoint.func_177956_o();
            double dz = pos.func_177952_p() - crackPoint.func_177952_p();
            double distSq = dx * dx + dy * dy + dz * dz + 2.0;
            distSumCrack += fastInvSqrt(distSq) + noiseValue;
         }

         if (!(distSumShell < thresholdOuter)) {
            IBlockState existingState = world.func_180495_p(pos);
            if (canReplace(existingState)) {
               if (shouldGenerateCrack && distSumCrack >= crackThreshold && distSumShell < thresholdAir) {
                  world.func_180501_a(pos, Blocks.field_150350_a.func_176223_P(), 2);
               } else if (distSumShell >= thresholdAir) {
                  world.func_180501_a(pos, Blocks.field_150350_a.func_176223_P(), 2);
               } else if (distSumShell >= thresholdInnerBlock) {
                  boolean useAlternate = random.nextFloat() < 0.083;
                  if (useAlternate) {
                     world.func_180501_a(pos, AmethystRegistry.budding_amethyst.func_176223_P(), 2);
                  } else {
                     world.func_180501_a(pos, AmethystRegistry.amethyst_block.func_176223_P(), 2);
                  }

                  if (useAlternate && random.nextFloat() < 0.35) {
                     potentialCrystalPlacements.add(pos.func_185334_h());
                  }
               } else if (distSumShell >= thresholdMiddle) {
                  world.func_180501_a(pos, DeepslateRegistry.calcite.func_176223_P(), 2);
               } else if (distSumShell >= thresholdOuter) {
                  world.func_180501_a(pos, DeepslateRegistry.smooth_basalt.func_176223_P(), 2);
               }
            }
         }
      }

      IBlockState[] crystalStates = new IBlockState[]{
         AmethystRegistry.small_amethyst_bud.func_176223_P(),
         AmethystRegistry.medium_amethyst_bud.func_176223_P(),
         AmethystRegistry.large_amethyst_bud.func_176223_P(),
         AmethystRegistry.amethyst_cluster.func_176223_P()
      };

      for (BlockPos crystalPos : potentialCrystalPlacements) {
         IBlockState chosenCrystal = crystalStates[random.nextInt(crystalStates.length)];

         for (EnumFacing facing : ALL_FACINGS) {
            BlockPos placePos = crystalPos.func_177972_a(facing);
            IBlockState placeState = world.func_180495_p(placePos);
            if (placeState.func_177230_c().isAir(placeState, world, placePos)) {
               IBlockState orientedCrystal = chosenCrystal.func_177226_a(BlockAmethystCluster.FACING, facing);
               world.func_180501_a(placePos, orientedCrystal, 2);
               break;
            }
         }
      }
   }

   private static double fastInvSqrt(double value) {
      return 1.0 / Math.sqrt(value);
   }

   private static double sampleNoise(int x, int y, int z, long seed) {
      double sx = x / 16.0;
      double sy = y / 16.0;
      double sz = z / 16.0;
      int ix = (int)Math.floor(sx);
      int iy = (int)Math.floor(sy);
      int iz = (int)Math.floor(sz);
      double fx = sx - ix;
      double fy = sy - iy;
      double fz = sz - iz;
      fx = fx * fx * (3.0 - 2.0 * fx);
      fy = fy * fy * (3.0 - 2.0 * fy);
      fz = fz * fz * (3.0 - 2.0 * fz);
      double c000 = hashCorner(ix, iy, iz, seed);
      double c100 = hashCorner(ix + 1, iy, iz, seed);
      double c010 = hashCorner(ix, iy + 1, iz, seed);
      double c110 = hashCorner(ix + 1, iy + 1, iz, seed);
      double c001 = hashCorner(ix, iy, iz + 1, seed);
      double c101 = hashCorner(ix + 1, iy, iz + 1, seed);
      double c011 = hashCorner(ix, iy + 1, iz + 1, seed);
      double c111 = hashCorner(ix + 1, iy + 1, iz + 1, seed);
      double c00 = c000 + fx * (c100 - c000);
      double c10 = c010 + fx * (c110 - c010);
      double c01 = c001 + fx * (c101 - c001);
      double c11 = c011 + fx * (c111 - c011);
      double c0 = c00 + fy * (c10 - c00);
      double c1 = c01 + fy * (c11 - c01);
      return c0 + fz * (c1 - c0);
   }

   private static double hashCorner(int x, int y, int z, long seed) {
      long hash = seed ^ x * 700726916507062L;
      hash ^= y * 475226193342381L;
      hash ^= z * 1063155655502159L;
      hash ^= hash >>> 33;
      hash *= -49064778989728563L;
      hash ^= hash >>> 33;
      hash *= -4265267296055464877L;
      hash ^= hash >>> 33;
      return (hash & 16777215L) / 1.6777215E7 * 2.0 - 1.0;
   }

   private static int uniformInt(Random random, int min, int max) {
      return min + random.nextInt(max - min + 1);
   }

   private static boolean isInvalidBlock(IBlockState state) {
      Block block = state.func_177230_c();
      return block == Blocks.field_150357_h || block == Blocks.field_150432_aD || block == Blocks.field_150403_cj || state.func_185904_a().func_76224_d();
   }

   private static boolean canReplace(IBlockState state) {
      Block block = state.func_177230_c();
      return block != Blocks.field_150357_h && block != Blocks.field_150474_ac && block != Blocks.field_150486_ae && block != Blocks.field_150378_br;
   }
}
