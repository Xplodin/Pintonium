package com.yungnickyoung.minecraft.bettercaves.world.bedrock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;

public class FlattenBedrock {
   private static final IBlockState BEDROCK = Blocks.field_150357_h.func_176223_P();

   public static void flattenBedrock(ChunkPrimer primer, int bedrockLayerWidth) {
      IBlockState replacementBlock = Blocks.field_150348_b.func_176223_P();

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int y = 1; y < 5; y++) {
               if (primer.func_177856_a(x, y, z) == BEDROCK) {
                  primer.func_177855_a(x, y, z, replacementBlock);
               }
            }
         }
      }

      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            for (int yx = 1; yx < bedrockLayerWidth; yx++) {
               primer.func_177855_a(x, yx, z, BEDROCK);
            }
         }
      }
   }
}
