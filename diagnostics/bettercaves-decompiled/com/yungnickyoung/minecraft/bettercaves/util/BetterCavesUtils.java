package com.yungnickyoung.minecraft.bettercaves.util;

import com.yungnickyoung.minecraft.bettercaves.config.Configuration;
import java.util.function.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public class BetterCavesUtils {
   private BetterCavesUtils() {
   }

   public static int getSurfaceAltitudeForColumn(ChunkPrimer primer, int localX, int localZ) {
      return searchSurfaceAltitudeInRangeForColumn(primer, localX, localZ, 255, 0);
   }

   public static int searchSurfaceAltitudeInRangeForColumn(ChunkPrimer primer, int localX, int localZ, int topY, int bottomY) {
      if (topY == 255
         && primer.func_177856_a(localX, 255, localZ) != Blocks.field_150350_a.func_176223_P()
         && primer.func_177856_a(localX, 255, localZ).func_185904_a() != Material.field_151586_h) {
         return 255;
      } else {
         for (int y = bottomY; y <= topY; y++) {
            IBlockState blockState = primer.func_177856_a(localX, y, localZ);
            if (blockState == Blocks.field_150350_a.func_176223_P() || blockState.func_185904_a() == Material.field_151586_h) {
               return y;
            }
         }

         return 1;
      }
   }

   public static String dimensionAsString(int dimensionID, String dimensionName) {
      return String.format("%d (%s)", dimensionID, dimensionName);
   }

   public static int getLocal(int coordinate) {
      return coordinate & 15;
   }

   public static boolean isDimensionWhitelisted(int dimID) {
      if (Configuration.enableGlobalWhitelist) {
         return true;
      } else {
         for (int dim : Configuration.whitelistedDimensionIDs) {
            if (dimID == dim) {
               return true;
            }
         }

         return false;
      }
   }

   public static float biomeDistanceFactor(World world, BlockPos pos, int radius, Predicate<Biome> isTargetBiome) {
      MutableBlockPos checkpos = new MutableBlockPos();

      for (int i = 1; i <= radius; i++) {
         for (int j = 0; j <= i; j++) {
            for (EnumFacing direction : Plane.HORIZONTAL) {
               checkpos.func_189533_g(pos).func_189534_c(direction, i).func_189534_c(direction.func_176746_e(), j);
               if (isTargetBiome.test(world.func_180494_b(checkpos))) {
                  return (float)(i + j) / (2 * radius);
               }

               if (j != 0 && i != j) {
                  checkpos.func_189533_g(pos).func_189534_c(direction, i).func_189534_c(direction.func_176735_f(), j);
                  if (isTargetBiome.test(world.func_180494_b(checkpos))) {
                     return (float)(i + j) / (2 * radius);
                  }
               }
            }
         }
      }

      return 1.0F;
   }
}
