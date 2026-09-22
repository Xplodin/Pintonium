package sayys.depthsupdate.world.generation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jspecify.annotations.NonNull;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockPointedDripstone;
import sayys.depthsupdate.compat.FluidloggedCompat;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.registry.DeepslateRegistry;
import sayys.depthsupdate.util.BlockUtils;

public class DripstoneCavesGenerator implements IWorldGenerator {
   private static final int NONE = Integer.MIN_VALUE;
   private static final int COLUMN_SCAN_RANGE = 12;
   private static final int LARGE_SCAN_RANGE = 30;
   private static final int CLUSTER_LAYER_MIN = 2;
   private static final int CLUSTER_LAYER_RANGE = 3;
   private static final int CLUSTER_HEIGHT_DEVIATION = 3;
   private static final int CLUSTER_MAX_HEIGHT_DIFF = 1;
   private static final int CLUSTER_CENTER_BIAS_RANGE = 8;
   private static final int CLUSTER_EDGE_FADE_RANGE = 3;
   private static final float CLUSTER_EDGE_CHANCE = 0.1F;
   private static final float LARGE_RADIUS_TO_HEIGHT_RATIO = 0.33F;
   private static final int LARGE_MIN_RADIUS = 3;
   private static final int LARGE_MAX_RADIUS = 16;
   private static final float LARGE_WIND_MAX_SPEED = 0.3F;
   private static final int LARGE_MIN_RADIUS_FOR_WIND = 4;
   private static final float LARGE_MIN_BLUNTNESS_FOR_WIND = 0.6F;
   private static final int MIN_OPEN_HEIGHT = 3;
   private static final float POINTED_TALLER_CHANCE = 0.35F;
   private static final float POINTED_SPREAD_CHANCE = 0.7F;
   private static final float POINTED_SPREAD2_CHANCE = 0.5F;
   private static final float POINTED_SPREAD3_CHANCE = 0.5F;

   public static void register() {
      GameRegistry.registerWorldGenerator(new DripstoneCavesGenerator(), 110);
   }

   public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (DepthsUpdateConfig.dripstoneCaves.enableDripstoneCaves) {
         if (world.field_73011_w.getDimension() == 0) {
            if (!(chunkGenerator instanceof ChunkGeneratorFlat) && !(chunkGenerator instanceof ChunkGeneratorDebug)) {
               if (random.nextInt(DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRarity) == 0) {
                  int y = CaveRegion.randomYInWindow(
                     random,
                     HeightManager.get(world),
                     DepthsUpdateConfig.dripstoneCaves.dripstoneCavesMinY,
                     DepthsUpdateConfig.dripstoneCaves.dripstoneCavesMaxY
                  );
                  if (y == Integer.MIN_VALUE) {
                     return;
                  }

                  int x = chunkX * 16 + 8 + random.nextInt(16);
                  int z = chunkZ * 16 + 8 + random.nextInt(16);
                  BlockPos center = new BlockPos(x, y, z);
                  GenerationLog.featurePlaced("dripstone cave", center);
                  this.generateDripstoneCave(world, random, center);
               }
            }
         }
      }
   }

   private void generateDripstoneCave(World world, @NonNull Random random, BlockPos center) {
      int radiusX = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusBase
         + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusVariation));
      int radiusY = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesHeightBase
         + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesHeightVariation));
      int radiusZ = DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusBase
         + random.nextInt(Math.max(1, DepthsUpdateConfig.dripstoneCaves.dripstoneCavesRadiusVariation));
      double volume = CaveRegion.volume(radiusX, radiusY, radiusZ);
      int clusters = Math.max(6, (int)(volume / 700.0));
      int spires = Math.max(3, (int)(volume / 2600.0));
      int pointedPatches = Math.max(4, (int)(volume / 2600.0));
      List<BlockPos> placedSpikes = new ArrayList<>();

      for (int i = 0; i < clusters; i++) {
         this.placeCluster(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ), placedSpikes);
      }

      for (int i = 0; i < spires; i++) {
         this.placeLargeDripstone(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }

      for (int i = 0; i < pointedPatches; i++) {
         BlockPos patchCenter = CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ);
         int attempts = 1 + random.nextInt(5);

         for (int j = 0; j < attempts; j++) {
            BlockPos pos = patchCenter.func_177982_a(
               (int)clampedNormal(random, 0.0F, 3.0F, -10.0F, 10.0F),
               (int)clampedNormal(random, 0.0F, 0.6F, -2.0F, 2.0F),
               (int)clampedNormal(random, 0.0F, 3.0F, -10.0F, 10.0F)
            );
            this.placePointedSpike(world, random, pos, placedSpikes);
         }
      }

      this.relinkSpikes(world, placedSpikes);
   }

   private void relinkSpikes(World world, List<BlockPos> placed) {
      Set<BlockPos> targets = new HashSet<>(placed);

      for (BlockPos pos : placed) {
         addIfSpike(world, targets, pos.func_177984_a());
         addIfSpike(world, targets, pos.func_177977_b());
      }

      List<BlockPos> ordered = new ArrayList<>(targets);
      ordered.sort(Comparator.comparingInt(Vec3i::func_177956_o));

      for (BlockPos pos : ordered) {
         if (hasTipDirection(world, pos, EnumFacing.DOWN)) {
            BlockPointedDripstone.refreshThickness(world, pos);
         }
      }

      for (int i = ordered.size() - 1; i >= 0; i--) {
         BlockPos posx = ordered.get(i);
         if (hasTipDirection(world, posx, EnumFacing.UP)) {
            BlockPointedDripstone.refreshThickness(world, posx);
         }
      }
   }

   private static void addIfSpike(World world, Set<BlockPos> targets, BlockPos pos) {
      if (world.func_180495_p(pos).func_177230_c() == DeepslateRegistry.pointed_dripstone) {
         targets.add(pos);
      }
   }

   private static boolean hasTipDirection(World world, BlockPos pos, EnumFacing tipDirection) {
      return BlockPointedDripstone.isPointedDripstoneWithDirection(world.func_180495_p(pos), tipDirection);
   }

   private void placeCluster(World world, Random random, BlockPos origin, List<BlockPos> placed) {
      if (isEmptyOrWater(world.func_180495_p(origin))) {
         int clusterHeight = 3 + random.nextInt(4);
         float wetness = clampedNormal(random, 0.1F, 0.3F, 0.1F, 0.9F);
         float density = 0.3F + random.nextFloat() * 0.4F;
         int radiusX = 2 + random.nextInt(7);
         int radiusZ = 2 + random.nextInt(7);

         for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
               double chance = spikeChance(radiusX, radiusZ, dx, dz);
               this.placeClusterColumn(world, random, origin.func_177982_a(dx, 0, dz), dx, dz, wetness, chance, clusterHeight, density, placed);
            }
         }
      }
   }

   private void placeClusterColumn(
      World world, Random random, BlockPos pos, int dx, int dz, float wetness, double chance, int clusterHeight, float density, List<BlockPos> placed
   ) {
      int ceilingY = this.scanForSolid(world, pos, EnumFacing.UP, 12, false);
      int floorY = this.scanForSolid(world, pos, EnumFacing.DOWN, 12, false);
      if (ceilingY != Integer.MIN_VALUE || floorY != Integer.MIN_VALUE) {
         if (ceilingY == Integer.MIN_VALUE || floorY == Integer.MIN_VALUE || ceilingY - floorY - 1 >= 3) {
            if (floorY != Integer.MIN_VALUE && random.nextFloat() < wetness) {
               BlockPos floorPos = new BlockPos(pos.func_177958_n(), floorY, pos.func_177952_p());
               if (this.canPlacePool(world, floorPos)) {
                  world.func_180501_a(floorPos, Blocks.field_150355_j.func_176223_P(), 2);
                  floorY--;
               }
            }

            int stalactiteHeight = 0;
            if (ceilingY != Integer.MIN_VALUE && random.nextDouble() < chance && !isLava(world, pos.func_177958_n(), ceilingY, pos.func_177952_p())) {
               int layerThickness = 2 + random.nextInt(3);
               this.replaceWithDripstone(world, new BlockPos(pos.func_177958_n(), ceilingY, pos.func_177952_p()), layerThickness, EnumFacing.UP);
               int maxHeight = floorY != Integer.MIN_VALUE ? Math.min(clusterHeight, ceilingY - floorY) : clusterHeight;
               stalactiteHeight = this.clusterSpikeHeight(random, dx, dz, density, maxHeight);
            }

            int stalagmiteHeight = 0;
            if (floorY != Integer.MIN_VALUE && random.nextDouble() < chance && !isLava(world, pos.func_177958_n(), floorY, pos.func_177952_p())) {
               int layerThickness = 2 + random.nextInt(3);
               this.replaceWithDripstone(world, new BlockPos(pos.func_177958_n(), floorY, pos.func_177952_p()), layerThickness, EnumFacing.DOWN);
               if (ceilingY != Integer.MIN_VALUE) {
                  stalagmiteHeight = Math.max(0, stalactiteHeight + random.nextInt(3) - 1);
               } else {
                  stalagmiteHeight = this.clusterSpikeHeight(random, dx, dz, density, clusterHeight);
               }
            }

            if (ceilingY != Integer.MIN_VALUE && floorY != Integer.MIN_VALUE && ceilingY - stalactiteHeight <= floorY + stalagmiteHeight) {
               int lowestStalactiteBottom = Math.max(ceilingY - stalactiteHeight, floorY + 1);
               int highestStalagmiteTop = Math.min(floorY + stalagmiteHeight, ceilingY - 1);
               int bound = Math.max(1, highestStalagmiteTop + 2 - lowestStalactiteBottom);
               int stalactiteBottom = lowestStalactiteBottom + random.nextInt(bound);
               stalactiteHeight = ceilingY - stalactiteBottom;
               stalagmiteHeight = stalactiteBottom - 1 - floorY;
            }

            int gap = ceilingY != Integer.MIN_VALUE && floorY != Integer.MIN_VALUE ? ceilingY - floorY - 1 : -1;
            boolean mergedTip = random.nextBoolean() && stalactiteHeight > 0 && stalagmiteHeight > 0 && gap > 0 && stalactiteHeight + stalagmiteHeight == gap;
            if (ceilingY != Integer.MIN_VALUE) {
               this.growSpeleothem(
                  world, new BlockPos(pos.func_177958_n(), ceilingY - 1, pos.func_177952_p()), EnumFacing.DOWN, stalactiteHeight, mergedTip, placed
               );
            }

            if (floorY != Integer.MIN_VALUE) {
               this.growSpeleothem(
                  world, new BlockPos(pos.func_177958_n(), floorY + 1, pos.func_177952_p()), EnumFacing.UP, stalagmiteHeight, mergedTip, placed
               );
            }
         }
      }
   }

   private int clusterSpikeHeight(Random random, int dx, int dz, float density, int maxHeight) {
      if (random.nextFloat() > density) {
         return 0;
      } else {
         int distanceFromCenter = Math.abs(dx) + Math.abs(dz);
         float mean = clampedMap(distanceFromCenter, 0.0F, 8.0F, maxHeight / 2.0F, 0.0F);
         return (int)clampedNormal(random, mean, 3.0F, 0.0F, maxHeight);
      }
   }

   private static double spikeChance(int radiusX, int radiusZ, int dx, int dz) {
      int distanceFromEdge = Math.min(radiusX - Math.abs(dx), radiusZ - Math.abs(dz));
      return clampedMap(distanceFromEdge, 0.0F, 3.0F, 0.1F, 1.0F);
   }

   private boolean canPlacePool(World world, BlockPos pos) {
      IBlockState state = world.func_180495_p(pos);
      Block block = state.func_177230_c();
      if (state.func_185904_a() != Material.field_151586_h && block != DeepslateRegistry.dripstone_block && block != DeepslateRegistry.pointed_dripstone) {
         if (world.func_180495_p(pos.func_177984_a()).func_185904_a() == Material.field_151586_h) {
            return false;
         } else {
            for (EnumFacing facing : EnumFacing.field_176754_o) {
               if (!this.isStoneOrWater(world, pos.func_177972_a(facing))) {
                  return false;
               }
            }

            return this.isStoneOrWater(world, pos.func_177977_b());
         }
      } else {
         return false;
      }
   }

   private boolean isStoneOrWater(World world, BlockPos pos) {
      IBlockState state = world.func_180495_p(pos);
      return isReplaceableStone(state) || state.func_185904_a() == Material.field_151586_h;
   }

   private void placeLargeDripstone(World world, Random random, BlockPos origin) {
      if (isEmptyOrWater(world.func_180495_p(origin))) {
         int ceilingY = this.scanForSolid(world, origin, EnumFacing.UP, 30, true);
         int floorY = this.scanForSolid(world, origin, EnumFacing.DOWN, 30, true);
         if (ceilingY != Integer.MIN_VALUE && floorY != Integer.MIN_VALUE) {
            int caveHeight = ceilingY - floorY - 1;
            if (caveHeight >= 4) {
               int maxRadius = MathHelper.func_76125_a((int)(caveHeight * 0.33F), 3, 16);
               int radius = 3 + random.nextInt(maxRadius - 3 + 1);
               DripstoneCavesGenerator.Spire stalactite = new DripstoneCavesGenerator.Spire(
                  new BlockPos(origin.func_177958_n(), ceilingY - 1, origin.func_177952_p()),
                  false,
                  radius,
                  (double)uniform(random, 0.3F, 0.9F),
                  (double)uniform(random, 0.4F, 2.0F)
               );
               DripstoneCavesGenerator.Spire stalagmite = new DripstoneCavesGenerator.Spire(
                  new BlockPos(origin.func_177958_n(), floorY + 1, origin.func_177952_p()),
                  true,
                  radius,
                  (double)uniform(random, 0.4F, 1.0F),
                  (double)uniform(random, 0.4F, 2.0F)
               );
               DripstoneCavesGenerator.Wind wind = stalactite.isSuitableForWind() && stalagmite.isSuitableForWind()
                  ? new DripstoneCavesGenerator.Wind(origin.func_177956_o(), random, 0.3F, 16 - radius)
                  : DripstoneCavesGenerator.Wind.NONE;
               if (stalactite.embedRoot(world, wind)) {
                  stalactite.place(world, random, wind);
               }

               if (stalagmite.embedRoot(world, wind)) {
                  stalagmite.place(world, random, wind);
               }
            }
         }
      }
   }

   private void placePointedSpike(World world, Random random, BlockPos origin, List<BlockPos> placed) {
      if (isEmptyOrWater(world.func_180495_p(origin))) {
         int ceilingY = this.scanForSolid(world, origin, EnumFacing.UP, 12, false);
         int floorY = this.scanForSolid(world, origin, EnumFacing.DOWN, 12, false);
         if (ceilingY != Integer.MIN_VALUE || floorY != Integer.MIN_VALUE) {
            if (ceilingY == Integer.MIN_VALUE || floorY == Integer.MIN_VALUE || ceilingY - floorY - 1 >= 3) {
               boolean fromCeiling = floorY == Integer.MIN_VALUE || ceilingY != Integer.MIN_VALUE && random.nextBoolean();
               EnumFacing tipDirection = fromCeiling ? EnumFacing.DOWN : EnumFacing.UP;
               BlockPos pos = new BlockPos(origin.func_177958_n(), fromCeiling ? ceilingY - 1 : floorY + 1, origin.func_177952_p());
               if (isEmptyOrWater(world.func_180495_p(pos)) && hasOpenSide(world, pos)) {
                  if (isBase(world.func_180495_p(pos.func_177972_a(tipDirection.func_176734_d())))) {
                     this.placeBasePatch(world, random, pos.func_177972_a(tipDirection.func_176734_d()));
                     int height = random.nextFloat() < 0.35F && isEmptyOrWater(world.func_180495_p(pos.func_177972_a(tipDirection))) ? 2 : 1;
                     this.growSpeleothem(world, pos, tipDirection, height, false, placed);
                  }
               }
            }
         }
      }
   }

   private static boolean hasOpenSide(World world, BlockPos pos) {
      for (EnumFacing facing : EnumFacing.field_176754_o) {
         IBlockState state = world.func_180495_p(pos.func_177972_a(facing));
         if (isEmptyOrWater(state) || state.func_177230_c() == DeepslateRegistry.pointed_dripstone) {
            return true;
         }
      }

      return false;
   }

   private void placeBasePatch(World world, Random random, BlockPos pos) {
      this.placeDripstoneBlockIfPossible(world, pos);

      for (EnumFacing facing : EnumFacing.field_176754_o) {
         if (!(random.nextFloat() > 0.7F)) {
            BlockPos first = pos.func_177972_a(facing);
            this.placeDripstoneBlockIfPossible(world, first);
            if (!(random.nextFloat() > 0.5F)) {
               BlockPos second = first.func_177972_a(EnumFacing.func_176741_a(random));
               this.placeDripstoneBlockIfPossible(world, second);
               if (!(random.nextFloat() > 0.5F)) {
                  this.placeDripstoneBlockIfPossible(world, second.func_177972_a(EnumFacing.func_176741_a(random)));
               }
            }
         }
      }
   }

   private void growSpeleothem(World world, BlockPos start, EnumFacing tipDirection, int totalLength, boolean mergedTip, List<BlockPos> placed) {
      if (totalLength > 0) {
         if (isBase(world.func_180495_p(start.func_177972_a(tipDirection.func_176734_d())))) {
            BlockPos pos = start;
            if (totalLength >= 3) {
               pos = this.placePointed(world, start, tipDirection, BlockPointedDripstone.DripstoneThickness.BASE, placed);

               for (int i = 0; i < totalLength - 3; i++) {
                  pos = this.placePointed(world, pos, tipDirection, BlockPointedDripstone.DripstoneThickness.MIDDLE, placed);
               }
            }

            if (totalLength >= 2) {
               pos = this.placePointed(world, pos, tipDirection, BlockPointedDripstone.DripstoneThickness.FRUSTUM, placed);
            }

            this.placePointed(
               world, pos, tipDirection, mergedTip ? BlockPointedDripstone.DripstoneThickness.TIP_MERGE : BlockPointedDripstone.DripstoneThickness.TIP, placed
            );
         }
      }
   }

   private BlockPos placePointed(World world, BlockPos pos, EnumFacing tipDirection, BlockPointedDripstone.DripstoneThickness thickness, List<BlockPos> placed) {
      boolean inWater = world.func_180495_p(pos).func_185904_a() == Material.field_151586_h;
      IBlockState state = DeepslateRegistry.pointed_dripstone
         .func_176223_P()
         .func_177226_a(BlockPointedDripstone.TIP_DIRECTION, tipDirection)
         .func_177226_a(BlockPointedDripstone.THICKNESS, thickness);
      world.func_180501_a(pos, state, 2);
      placed.add(pos);
      if (inWater) {
         FluidloggedCompat.logWater(world, pos, state);
      }

      return pos.func_177972_a(tipDirection);
   }

   private void replaceWithDripstone(World world, BlockPos start, int maxCount, EnumFacing direction) {
      BlockPos pos = start;

      for (int i = 0; i < maxCount; i++) {
         if (!this.placeDripstoneBlockIfPossible(world, pos)) {
            return;
         }

         pos = pos.func_177972_a(direction);
      }
   }

   private boolean placeDripstoneBlockIfPossible(World world, BlockPos pos) {
      IBlockState state = world.func_180495_p(pos);
      if (isReplaceableStone(state)) {
         world.func_180501_a(pos, DeepslateRegistry.dripstone_block.func_176223_P(), 2);
         return true;
      } else {
         return false;
      }
   }

   private int scanForSolid(World world, BlockPos origin, EnumFacing direction, int range, boolean requireBaseOrLava) {
      BlockPos pos = origin;

      for (int i = 0; i < range; i++) {
         pos = pos.func_177972_a(direction);
         IBlockState state = world.func_180495_p(pos);
         if (!isEmptyOrWater(state)) {
            if (requireBaseOrLava && !isBaseOrLava(state)) {
               return Integer.MIN_VALUE;
            }

            return pos.func_177956_o();
         }
      }

      return Integer.MIN_VALUE;
   }

   private static boolean isEmptyOrWater(IBlockState state) {
      return state.func_185904_a() == Material.field_151579_a || state.func_185904_a() == Material.field_151586_h;
   }

   private static boolean isEmptyOrWaterOrLava(IBlockState state) {
      return state.func_185904_a() == Material.field_151579_a
         || state.func_185904_a() == Material.field_151586_h
         || state.func_185904_a() == Material.field_151587_i;
   }

   private static boolean isReplaceableStone(IBlockState state) {
      return BlockUtils.isBaseStone(state);
   }

   private static boolean isBase(IBlockState state) {
      return state.func_177230_c() == DeepslateRegistry.dripstone_block || isReplaceableStone(state);
   }

   private static boolean isBaseOrLava(IBlockState state) {
      return isBase(state) || state.func_185904_a() == Material.field_151587_i;
   }

   private static boolean isLava(World world, int x, int y, int z) {
      return world.func_180495_p(new BlockPos(x, y, z)).func_185904_a() == Material.field_151587_i;
   }

   private static float uniform(Random random, float min, float max) {
      return min + random.nextFloat() * (max - min);
   }

   private static float clampedNormal(Random random, float mean, float deviation, float min, float max) {
      return MathHelper.func_76131_a(mean + deviation * (float)random.nextGaussian(), min, max);
   }

   private static float clampedMap(float value, float fromMin, float fromMax, float toMin, float toMax) {
      float t = MathHelper.func_76131_a((value - fromMin) / (fromMax - fromMin), 0.0F, 1.0F);
      return toMin + t * (toMax - toMin);
   }

   private static double spireHeight(double distance, double radius, double scale, double bluntness) {
      if (distance < bluntness) {
         distance = bluntness;
      }

      double r = distance / radius * 0.384;
      double height = scale * (0.75 * Math.pow(r, 1.3333333333333333) - Math.pow(r, 0.6666666666666666) - Math.log(r) / 3.0);
      return Math.max(height, 0.0) / 0.384 * radius;
   }

   private static boolean isCircleEmbedded(World world, BlockPos center, int radius) {
      if (isEmptyOrWaterOrLava(world.func_180495_p(center))) {
         return false;
      } else {
         float angleStep = 6.0F / radius;

         for (float angle = 0.0F; angle < (float) (Math.PI * 2); angle += angleStep) {
            int dx = (int)(MathHelper.func_76134_b(angle) * radius);
            int dz = (int)(MathHelper.func_76126_a(angle) * radius);
            if (isEmptyOrWaterOrLava(world.func_180495_p(center.func_177982_a(dx, 0, dz)))) {
               return false;
            }
         }

         return true;
      }
   }

   private static final class Spire {
      private BlockPos root;
      private final boolean pointingUp;
      private int radius;
      private final double bluntness;
      private final double scale;

      private Spire(BlockPos root, boolean pointingUp, int radius, double bluntness, double scale) {
         this.root = root;
         this.pointingUp = pointingUp;
         this.radius = radius;
         this.bluntness = bluntness;
         this.scale = scale;
      }

      private int height() {
         return this.heightAt(0.0F);
      }

      private int heightAt(float distance) {
         return (int)DripstoneCavesGenerator.spireHeight((double)distance, (double)this.radius, this.scale, this.bluntness);
      }

      private boolean isSuitableForWind() {
         return this.radius >= 4 && this.bluntness >= 0.6F;
      }

      private boolean embedRoot(World world, DripstoneCavesGenerator.Wind wind) {
         while (this.radius > 1) {
            BlockPos pos = this.root;
            int tries = Math.min(10, this.height());

            for (int i = 0; i < tries; i++) {
               if (world.func_180495_p(pos).func_185904_a() == Material.field_151587_i) {
                  return false;
               }

               if (DripstoneCavesGenerator.isCircleEmbedded(world, wind.offset(pos), this.radius)) {
                  this.root = pos;
                  return true;
               }

               pos = pos.func_177972_a(this.pointingUp ? EnumFacing.DOWN : EnumFacing.UP);
            }

            this.radius /= 2;
         }

         return false;
      }

      private void place(World world, Random random, DripstoneCavesGenerator.Wind wind) {
         for (int dx = -this.radius; dx <= this.radius; dx++) {
            for (int dz = -this.radius; dz <= this.radius; dz++) {
               float distance = MathHelper.func_76129_c(dx * dx + dz * dz);
               if (!(distance > this.radius)) {
                  int height = this.heightAt(distance);
                  if (height > 0) {
                     if (random.nextFloat() < 0.2F) {
                        height = (int)(height * (0.8F + random.nextFloat() * 0.2F));
                     }

                     BlockPos pos = this.root.func_177982_a(dx, 0, dz);
                     boolean brokeSurface = false;
                     int maxY = this.pointingUp ? world.func_189649_b(pos.func_177958_n(), pos.func_177952_p()) : Integer.MAX_VALUE;

                     for (int i = 0; i < height && pos.func_177956_o() < maxY; i++) {
                        BlockPos target = wind.offset(pos);
                        IBlockState state = world.func_180495_p(target);
                        if (DripstoneCavesGenerator.isEmptyOrWaterOrLava(state)) {
                           brokeSurface = true;
                           world.func_180501_a(target, DeepslateRegistry.dripstone_block.func_176223_P(), 2);
                        } else if (brokeSurface && DripstoneCavesGenerator.isReplaceableStone(state)) {
                           break;
                        }

                        pos = pos.func_177972_a(this.pointingUp ? EnumFacing.UP : EnumFacing.DOWN);
                     }
                  }
               }
            }
         }
      }
   }

   private static final class Wind {
      private static final DripstoneCavesGenerator.Wind NONE = new DripstoneCavesGenerator.Wind();
      private final int originY;
      private final double speedX;
      private final double speedZ;
      private final int maxOffset;
      private final boolean active;

      private Wind(int originY, Random random, float maxSpeed, int maxOffset) {
         float speed = random.nextFloat() * maxSpeed;
         float direction = random.nextFloat() * (float) Math.PI;
         this.originY = originY;
         this.speedX = MathHelper.func_76134_b(direction) * speed;
         this.speedZ = MathHelper.func_76126_a(direction) * speed;
         this.maxOffset = maxOffset;
         this.active = true;
      }

      private Wind() {
         this.originY = 0;
         this.speedX = 0.0;
         this.speedZ = 0.0;
         this.maxOffset = 0;
         this.active = false;
      }

      private BlockPos offset(BlockPos pos) {
         if (!this.active) {
            return pos;
         } else {
            int dy = this.originY - pos.func_177956_o();
            int dx = MathHelper.func_76125_a(MathHelper.func_76128_c(this.speedX * dy), -this.maxOffset, this.maxOffset);
            int dz = MathHelper.func_76125_a(MathHelper.func_76128_c(this.speedZ * dy), -this.maxOffset, this.maxOffset);
            return dx == 0 && dz == 0 ? pos : pos.func_177982_a(dx, 0, dz);
         }
      }
   }
}
