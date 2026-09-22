package sayys.depthsupdate.world.generation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.BlockDoublePlant.EnumPlantType;
import net.minecraft.block.BlockTallGrass.EnumType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
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
import org.jspecify.annotations.NonNull;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockCaveVines;
import sayys.depthsupdate.compat.FluidloggedCompat;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.registry.PlantRegistry;
import sayys.depthsupdate.util.BlockUtils;

public class LushCavesGenerator implements IWorldGenerator {
   private static final int VERTICAL_RANGE = 5;
   private static final int PATCH_RADIUS_MIN = 4;
   private static final int PATCH_RADIUS_RANGE = 4;
   private static final float MOSS_VEGETATION_CHANCE = 0.8F;
   private static final float MOSS_EDGE_CHANCE = 0.3F;
   private static final float CEILING_VEGETATION_CHANCE = 0.08F;
   private static final int CEILING_DEPTH_RANGE = 2;
   private static final int CLAY_DEPTH = 3;
   private static final float CLAY_EXTRA_BOTTOM_CHANCE = 0.8F;
   private static final float CLAY_EDGE_CHANCE = 0.7F;
   private static final float CLAY_POOL_VEGETATION_CHANCE = 0.1F;
   private static final float CLAY_DRY_VEGETATION_CHANCE = 0.05F;
   private static final float BERRY_CHANCE = 0.2F;
   private static final int ROOT_HANGING_ATTEMPTS = 6;

   public static void register() {
      GameRegistry.registerWorldGenerator(new LushCavesGenerator(), 100);
   }

   public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
      if (DepthsUpdateConfig.lushCaves.enableLushCaves) {
         if (world.field_73011_w.getDimension() == 0) {
            if (!(chunkGenerator instanceof ChunkGeneratorFlat) && !(chunkGenerator instanceof ChunkGeneratorDebug)) {
               if (random.nextInt(DepthsUpdateConfig.lushCaves.lushCavesRarity) == 0) {
                  int y = CaveRegion.randomYInWindow(
                     random, HeightManager.get(world), DepthsUpdateConfig.lushCaves.lushCavesMinY, DepthsUpdateConfig.lushCaves.lushCavesMaxY
                  );
                  if (y == Integer.MIN_VALUE) {
                     return;
                  }

                  int x = chunkX * 16 + 8 + random.nextInt(16);
                  int z = chunkZ * 16 + 8 + random.nextInt(16);
                  BlockPos center = new BlockPos(x, y, z);
                  GenerationLog.featurePlaced("lush cave", center);
                  this.generateLushCave(world, random, center);
               }
            }
         }
      }
   }

   private void generateLushCave(World world, @NonNull Random random, BlockPos center) {
      int radiusX = DepthsUpdateConfig.lushCaves.lushCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesRadiusVariation));
      int radiusY = DepthsUpdateConfig.lushCaves.lushCavesHeightBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesHeightVariation));
      int radiusZ = DepthsUpdateConfig.lushCaves.lushCavesRadiusBase + random.nextInt(Math.max(1, DepthsUpdateConfig.lushCaves.lushCavesRadiusVariation));
      double volume = CaveRegion.volume(radiusX, radiusY, radiusZ);
      int mossPatches = Math.max(6, (int)(volume / 400.0));
      int ceilingPatches = Math.max(5, (int)(volume / 450.0));
      int clayPatches = Math.max(2, (int)(volume / 1400.0));
      int vineColumns = Math.max(8, (int)(volume / 220.0));
      int sporeBlossoms = Math.max(3, (int)(volume / 900.0));
      int rootPatches = Math.max(2, (int)(volume / 1500.0));
      int vinePatches = Math.max(3, (int)(volume / 800.0));

      for (int i = 0; i < mossPatches; i++) {
         this.placeMossPatch(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }

      for (int i = 0; i < ceilingPatches; i++) {
         this.placeCeilingMossPatch(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }

      for (int i = 0; i < clayPatches; i++) {
         this.placeClayPatch(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ), random.nextBoolean());
      }

      for (int i = 0; i < vineColumns; i++) {
         this.placeCaveVineColumn(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }

      for (int i = 0; i < sporeBlossoms; i++) {
         this.placeSporeBlossom(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }

      for (int i = 0; i < rootPatches; i++) {
         this.placeRootedDirt(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }

      for (int i = 0; i < vinePatches; i++) {
         this.placeWallVine(world, random, CaveRegion.randomPointInside(random, center, radiusX, radiusY, radiusZ));
      }
   }

   private void placeMossPatch(World world, Random random, BlockPos origin) {
      for (BlockPos pos : this.placeGroundPatch(world, random, origin, EnumFacing.DOWN, PlantRegistry.moss_block.func_176223_P(), 1, 0.0F, 0.3F)) {
         if (random.nextFloat() < 0.8F) {
            this.placeMossVegetation(world, random, pos.func_177984_a());
         }
      }
   }

   private void placeCeilingMossPatch(World world, Random random, BlockPos origin) {
      int depth = 1 + random.nextInt(2);

      for (BlockPos pos : this.placeGroundPatch(world, random, origin, EnumFacing.UP, PlantRegistry.moss_block.func_176223_P(), depth, 0.0F, 0.3F)) {
         if (random.nextFloat() < 0.08F) {
            this.growCaveVines(world, random, pos.func_177977_b());
         }
      }
   }

   private void placeClayPatch(World world, Random random, BlockPos origin, boolean pool) {
      Set<BlockPos> ground = this.placeGroundPatch(world, random, origin, EnumFacing.DOWN, Blocks.field_150435_aG.func_176223_P(), 3, 0.8F, 0.7F);
      if (!ground.isEmpty()) {
         Set<BlockPos> surface = ground;
         if (pool) {
            surface = new HashSet<>();

            for (BlockPos pos : ground) {
               if (!isRimBlock(world, pos)) {
                  surface.add(pos);
               }
            }

            for (BlockPos posx : surface) {
               world.func_180501_a(posx, Blocks.field_150355_j.func_176223_P(), 2);
            }
         }

         float chance = pool ? 0.1F : 0.05F;

         for (BlockPos posx : surface) {
            if (random.nextFloat() < chance) {
               this.placeDripleaf(world, random, pool ? posx : posx.func_177984_a(), pool);
            }
         }
      }
   }

   private Set<BlockPos> placeGroundPatch(
      World world, Random random, BlockPos origin, EnumFacing surface, IBlockState ground, int depth, float extraBottomChance, float edgeChance
   ) {
      if (!world.func_175623_d(origin)) {
         return Collections.emptySet();
      } else {
         int xRadius = 4 + random.nextInt(4) + 1;
         int zRadius = 4 + random.nextInt(4) + 1;
         EnumFacing outwards = surface.func_176734_d();
         Set<BlockPos> placed = new HashSet<>();

         for (int dx = -xRadius; dx <= xRadius; dx++) {
            boolean xEdge = dx == -xRadius || dx == xRadius;

            for (int dz = -zRadius; dz <= zRadius; dz++) {
               boolean zEdge = dz == -zRadius || dz == zRadius;
               if ((!xEdge || !zEdge) && (!xEdge && !zEdge || !(random.nextFloat() > edgeChance))) {
                  BlockPos pos = origin.func_177982_a(dx, 0, dz);

                  for (int i = 0; i < 5 && world.func_175623_d(pos); i++) {
                     pos = pos.func_177972_a(surface);
                  }

                  for (int i = 0; i < 5 && !world.func_175623_d(pos); i++) {
                     pos = pos.func_177972_a(outwards);
                  }

                  BlockPos groundPos = pos.func_177972_a(surface);
                  if (world.func_175623_d(pos) && world.func_180495_p(groundPos).isSideSolid(world, groundPos, outwards)) {
                     int columnDepth = depth + (random.nextFloat() < extraBottomChance ? 1 : 0);
                     if (this.placeGroundColumn(world, groundPos, surface, ground, columnDepth)) {
                        placed.add(groundPos);
                     }
                  }
               }
            }
         }

         return placed;
      }
   }

   private boolean placeGroundColumn(World world, BlockPos start, EnumFacing surface, IBlockState ground, int depth) {
      BlockPos pos = start;

      for (int i = 0; i < depth; i++) {
         IBlockState state = world.func_180495_p(pos);
         if (state.func_177230_c() == ground.func_177230_c()) {
            pos = pos.func_177972_a(surface);
         } else {
            if (!isReplaceable(state)) {
               return i != 0;
            }

            world.func_180501_a(pos, ground, 2);
            pos = pos.func_177972_a(surface);
         }
      }

      return true;
   }

   private static boolean isRimBlock(World world, BlockPos pos) {
      for (EnumFacing facing : EnumFacing.field_176754_o) {
         BlockPos side = pos.func_177972_a(facing);
         if (!world.func_180495_p(side).isSideSolid(world, side, facing.func_176734_d())) {
            return true;
         }
      }

      BlockPos below = pos.func_177977_b();
      return !world.func_180495_p(below).isSideSolid(world, below, EnumFacing.UP);
   }

   private void placeMossVegetation(World world, Random random, BlockPos pos) {
      if (world.func_175623_d(pos)) {
         int roll = random.nextInt(96);
         if (roll < 50) {
            world.func_180501_a(pos, Blocks.field_150329_H.func_176223_P().func_177226_a(BlockTallGrass.field_176497_a, EnumType.GRASS), 2);
         } else if (roll < 75) {
            world.func_180501_a(pos, PlantRegistry.moss_carpet.func_176223_P(), 2);
         } else if (roll < 85) {
            placeTallGrass(world, pos);
         } else if (roll < 92) {
            world.func_180501_a(pos, PlantRegistry.azalea.func_176223_P(), 2);
         } else {
            world.func_180501_a(pos, PlantRegistry.flowering_azalea.func_176223_P(), 2);
         }
      }
   }

   private static void placeTallGrass(World world, BlockPos pos) {
      if (!world.func_175623_d(pos.func_177984_a())) {
         world.func_180501_a(pos, Blocks.field_150329_H.func_176223_P().func_177226_a(BlockTallGrass.field_176497_a, EnumType.GRASS), 2);
      } else {
         world.func_180501_a(pos, Blocks.field_150398_cm.func_176223_P().func_177226_a(BlockDoublePlant.field_176493_a, EnumPlantType.GRASS), 2);
         world.func_180501_a(pos.func_177984_a(), Blocks.field_150398_cm.func_176223_P().func_177226_a(BlockDoublePlant.field_176492_b, EnumBlockHalf.UPPER), 2);
      }
   }

   private void placeDripleaf(World world, Random random, BlockPos pos, boolean waterlogged) {
      if (world.func_175623_d(pos) || world.func_180495_p(pos).func_185904_a() == Material.field_151586_h) {
         if (random.nextBoolean()) {
            world.func_180501_a(pos, PlantRegistry.small_dripleaf.func_176223_P(), 2);
         } else {
            if (!world.func_175623_d(pos.func_177984_a()) && world.func_180495_p(pos.func_177984_a()).func_185904_a() != Material.field_151586_h) {
               return;
            }

            world.func_180501_a(pos, PlantRegistry.big_dripleaf_stem.func_176223_P(), 2);
            world.func_180501_a(pos.func_177984_a(), PlantRegistry.big_dripleaf.func_176223_P(), 2);
         }

         if (waterlogged) {
            FluidloggedCompat.logWater(world, pos, world.func_180495_p(pos));
         }
      }
   }

   private void placeCaveVineColumn(World world, Random random, BlockPos origin) {
      if (world.func_175623_d(origin)) {
         BlockPos pos = origin;

         for (int i = 0; i < 5 && world.func_175623_d(pos.func_177984_a()); i++) {
            pos = pos.func_177984_a();
         }

         if (isReplaceable(world.func_180495_p(pos.func_177984_a()))) {
            this.growCaveVines(world, random, pos);
         }
      }
   }

   private void growCaveVines(World world, Random random, BlockPos start) {
      int roll = random.nextInt(15);
      int length = roll < 10 ? random.nextInt(7) : (roll < 13 ? random.nextInt(3) : random.nextInt(20));
      if (length > 0) {
         BlockPos pos = start;

         int placed;
         for (placed = 0; placed < length && world.func_175623_d(pos); placed++) {
            boolean berries = random.nextFloat() < 0.2F;
            world.func_180501_a(pos, PlantRegistry.cave_vines_plant.func_176223_P().func_177226_a(BlockCaveVines.BERRIES, berries), 2);
            pos = pos.func_177977_b();
         }

         if (placed > 0 && world.func_175623_d(pos)) {
            boolean berries = random.nextFloat() < 0.2F;
            world.func_180501_a(pos, PlantRegistry.cave_vines.func_176223_P().func_177226_a(BlockCaveVines.BERRIES, berries), 2);
         }
      }
   }

   private void placeSporeBlossom(World world, Random random, BlockPos origin) {
      if (world.func_175623_d(origin)) {
         BlockPos pos = origin;

         for (int i = 0; i < 5 && world.func_175623_d(pos.func_177984_a()); i++) {
            pos = pos.func_177984_a();
         }

         IBlockState above = world.func_180495_p(pos.func_177984_a());
         if (above.func_177230_c() == PlantRegistry.moss_block || isReplaceable(above)) {
            world.func_180501_a(pos, PlantRegistry.spore_blossom.func_176223_P(), 2);
         }
      }
   }

   private void placeRootedDirt(World world, Random random, BlockPos origin) {
      if (world.func_175623_d(origin)) {
         BlockPos pos = origin;

         for (int i = 0; i < 5 && world.func_175623_d(pos.func_177984_a()); i++) {
            pos = pos.func_177984_a();
         }

         BlockPos ceiling = pos.func_177984_a();
         if (isReplaceable(world.func_180495_p(ceiling))) {
            world.func_180501_a(ceiling, PlantRegistry.rooted_dirt.func_176223_P(), 2);

            for (int i = 0; i < 6; i++) {
               BlockPos rootPos = ceiling.func_177982_a(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
               if (isReplaceable(world.func_180495_p(rootPos)) && world.func_175623_d(rootPos.func_177977_b())) {
                  world.func_180501_a(rootPos, PlantRegistry.rooted_dirt.func_176223_P(), 2);
                  world.func_180501_a(rootPos.func_177977_b(), PlantRegistry.hanging_roots.func_176223_P(), 2);
               }
            }
         }
      }
   }

   private void placeWallVine(World world, Random random, BlockPos origin) {
      if (world.func_175623_d(origin)) {
         for (EnumFacing facing : EnumFacing.field_176754_o) {
            BlockPos wall = origin.func_177972_a(facing);
            if (world.func_180495_p(wall).isSideSolid(world, wall, facing.func_176734_d())) {
               PropertyBool side = vineSide(facing);
               int length = 1 + random.nextInt(6);
               BlockPos pos = origin;

               for (int i = 0; i < length && world.func_175623_d(pos); i++) {
                  BlockPos support = pos.func_177972_a(facing);
                  if (!world.func_180495_p(support).isSideSolid(world, support, facing.func_176734_d())) {
                     break;
                  }

                  world.func_180501_a(pos, Blocks.field_150395_bd.func_176223_P().func_177226_a(side, true), 2);
                  pos = pos.func_177977_b();
               }

               return;
            }
         }
      }
   }

   private static PropertyBool vineSide(EnumFacing facing) {
      return switch (facing) {
         case NORTH -> BlockVine.field_176273_b;
         case SOUTH -> BlockVine.field_176279_N;
         case EAST -> BlockVine.field_176278_M;
         case WEST -> BlockVine.field_176280_O;
         default -> null;
      };
   }

   private static boolean isReplaceable(IBlockState state) {
      Block block = state.func_177230_c();
      return BlockUtils.isBaseStone(state)
         || block == Blocks.field_150346_d
         || block == Blocks.field_150349_c
         || block == Blocks.field_150351_n
         || block == Blocks.field_150435_aG
         || block == PlantRegistry.moss_block;
   }
}
