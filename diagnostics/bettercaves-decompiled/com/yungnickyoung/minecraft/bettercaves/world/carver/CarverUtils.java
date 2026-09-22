package com.yungnickyoung.minecraft.bettercaves.world.carver;

import com.google.common.collect.ImmutableSet;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class CarverUtils {
   private static final IBlockState AIR = Blocks.field_150350_a.func_176223_P();
   private static final IBlockState SAND = Blocks.field_150354_m.func_176223_P();
   private static final IBlockState SANDSTONE = Blocks.field_150322_A.func_176223_P();
   private static final IBlockState REDSANDSTONE = Blocks.field_180395_cM.func_176223_P();
   private static final IBlockState GRAVEL = Blocks.field_150351_n.func_176223_P();
   private static final IBlockState ANDESITE = Blocks.field_150348_b.func_176223_P().func_177226_a(BlockStone.field_176247_a, EnumType.ANDESITE);
   private static final ImmutableSet<IBlockState> DEBUG_BLOCKS = ImmutableSet.of(
      Blocks.field_150340_R.func_176223_P(),
      Blocks.field_150344_f.func_176223_P(),
      Blocks.field_150347_e.func_176223_P(),
      Blocks.field_150451_bX.func_176223_P(),
      Blocks.field_150475_bE.func_176223_P(),
      Blocks.field_150336_V.func_176223_P(),
      new IBlockState[0]
   );

   private CarverUtils() {
   }

   public static void digBlock(
      World world, ChunkPrimer primer, BlockPos blockPos, IBlockState airBlockState, IBlockState liquidBlockState, int liquidAltitude, boolean replaceGravel
   ) {
      int localX = BetterCavesUtils.getLocal(blockPos.func_177958_n());
      int localZ = BetterCavesUtils.getLocal(blockPos.func_177952_p());
      int y = blockPos.func_177956_o();
      IBlockState blockState = primer.func_177856_a(localX, y, localZ);
      IBlockState blockStateAbove = primer.func_177856_a(localX, y + 1, localZ);
      Biome biome = world.func_180494_b(blockPos);
      Block biomeTopBlock = biome.field_76752_A.func_177230_c();
      Block biomeFillerBlock = biome.field_76753_B.func_177230_c();
      if (canReplaceBlock(blockState, blockStateAbove) || blockState.func_177230_c() == biomeTopBlock || blockState.func_177230_c() == biomeFillerBlock) {
         if (airBlockState == AIR && y <= liquidAltitude) {
            if (liquidBlockState != null) {
               primer.func_177855_a(localX, y, localZ, liquidBlockState);
            }
         } else {
            if (airBlockState == AIR && isWaterAdjacent(primer, blockPos)) {
               return;
            }

            if (isTopBlock(world, primer, blockPos) && canReplaceBlock(primer.func_177856_a(localX, y - 1, localZ), AIR)) {
               primer.func_177855_a(localX, y - 1, localZ, biome.field_76752_A);
            }

            if (blockStateAbove == SAND) {
               primer.func_177855_a(localX, y + 1, localZ, SANDSTONE);
            } else if (blockStateAbove == SAND.func_177226_a(BlockSand.field_176504_a, net.minecraft.block.BlockSand.EnumType.RED_SAND)) {
               primer.func_177855_a(localX, y + 1, localZ, REDSANDSTONE);
            }

            if (replaceGravel && blockStateAbove == GRAVEL) {
               primer.func_177855_a(localX, y + 1, localZ, ANDESITE);
            }

            primer.func_177855_a(localX, y, localZ, airBlockState);
         }
      }
   }

   public static void digBlock(World world, ChunkPrimer primer, BlockPos blockPos, IBlockState liquidBlockState, int liquidAltitude, boolean replaceGravel) {
      digBlock(world, primer, blockPos, Blocks.field_150350_a.func_176223_P(), liquidBlockState, liquidAltitude, replaceGravel);
   }

   public static void digBlock(World world, ChunkPrimer primer, int x, int y, int z, IBlockState liquidBlockState, int liquidAltitude, boolean replaceGravel) {
      digBlock(world, primer, new BlockPos(x, y, z), liquidBlockState, liquidAltitude, replaceGravel);
   }

   public static void digBlock(
      World world, ChunkPrimer primer, int x, int y, int z, IBlockState airBlockState, IBlockState liquidBlockState, int liquidAltitude, boolean replaceGravel
   ) {
      digBlock(world, primer, new BlockPos(x, y, z), airBlockState, liquidBlockState, liquidAltitude, replaceGravel);
   }

   public static void debugDigBlock(ChunkPrimer primer, BlockPos blockPos, IBlockState blockState, boolean digBlock) {
      int localX = BetterCavesUtils.getLocal(blockPos.func_177958_n());
      int localZ = BetterCavesUtils.getLocal(blockPos.func_177952_p());
      int y = blockPos.func_177956_o();
      if (!DEBUG_BLOCKS.contains(primer.func_177856_a(localX, y, localZ))) {
         if (digBlock) {
            primer.func_177855_a(localX, y, localZ, blockState);
         } else {
            primer.func_177855_a(localX, y, localZ, Blocks.field_150350_a.func_176223_P());
         }
      }
   }

   public static void debugDigBlock(ChunkPrimer primer, int x, int y, int z, IBlockState blockState, boolean digBlock) {
      debugDigBlock(primer, new BlockPos(x, y, z), blockState, digBlock);
   }

   public static boolean isTopBlock(World world, ChunkPrimer primer, BlockPos blockPos) {
      int localX = BetterCavesUtils.getLocal(blockPos.func_177958_n());
      int localZ = BetterCavesUtils.getLocal(blockPos.func_177952_p());
      int y = blockPos.func_177956_o();
      Biome biome = world.func_180494_b(blockPos);
      IBlockState blockState = primer.func_177856_a(localX, y, localZ);
      return blockState == biome.field_76752_A;
   }

   public static boolean canReplaceBlock(IBlockState blockState, IBlockState blockStateAbove) {
      Block block = blockState.func_177230_c();
      if (block == Blocks.field_150362_t || block == Blocks.field_150361_u || block == Blocks.field_150364_r || block == Blocks.field_150363_s) {
         return false;
      } else if (blockStateAbove == Blocks.field_150364_r.func_176223_P() || blockStateAbove == Blocks.field_150363_s.func_176223_P()) {
         return false;
      } else if (blockState == Blocks.field_150357_h.func_176223_P()) {
         return false;
      } else if (blockState.func_185904_a() == Material.field_151576_e) {
         return true;
      } else {
         return block != Blocks.field_150348_b
               && block != Blocks.field_150346_d
               && block != Blocks.field_150349_c
               && block != Blocks.field_150405_ch
               && block != Blocks.field_150406_ce
               && block != Blocks.field_150322_A
               && block != Blocks.field_180395_cM
               && block != Blocks.field_150391_bh
               && block != Blocks.field_150431_aC
            ? (block == Blocks.field_150354_m || block == Blocks.field_150351_n) && blockStateAbove.func_185904_a() != Material.field_151586_h
            : true;
      }
   }

   private static boolean isWaterAdjacent(ChunkPrimer primer, BlockPos blockPos) {
      int localX = BetterCavesUtils.getLocal(blockPos.func_177958_n());
      int localZ = BetterCavesUtils.getLocal(blockPos.func_177952_p());
      int y = blockPos.func_177956_o();
      return primer.func_177856_a(localX, y + 1, localZ).func_185904_a() == Material.field_151586_h
         || localX < 15 && primer.func_177856_a(localX + 1, y, localZ).func_185904_a() == Material.field_151586_h
         || localX > 0 && primer.func_177856_a(localX - 1, y, localZ).func_185904_a() == Material.field_151586_h
         || localZ < 15 && primer.func_177856_a(localX, y, localZ + 1).func_185904_a() == Material.field_151586_h
         || localZ > 0 && primer.func_177856_a(localX, y, localZ - 1).func_185904_a() == Material.field_151586_h;
   }
}
