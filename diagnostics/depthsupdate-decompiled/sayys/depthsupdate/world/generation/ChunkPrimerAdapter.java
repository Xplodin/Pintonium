package sayys.depthsupdate.world.generation;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import sayys.depthsupdate.core.HeightContext;

public class ChunkPrimerAdapter extends ChunkPrimer {
   private static final IBlockState AIR = Blocks.field_150350_a.func_176223_P();
   private final Chunk chunk;
   private final HeightContext ctx;

   public ChunkPrimerAdapter(Chunk chunk, HeightContext ctx) {
      this.chunk = chunk;
      this.ctx = ctx;
   }

   public IBlockState func_177856_a(int x, int y, int z) {
      int storageIdx = this.ctx.toStorageIndex(y);
      ExtendedBlockStorage[] arrays = this.chunk.func_76587_i();
      if (storageIdx >= 0 && storageIdx < arrays.length) {
         ExtendedBlockStorage section = arrays[storageIdx];
         return section == Chunk.field_186036_a ? AIR : section.func_177485_a(x, y & 15, z);
      } else {
         return AIR;
      }
   }

   public void func_177855_a(int x, int y, int z, IBlockState state) {
      int storageIdx = this.ctx.toStorageIndex(y);
      ExtendedBlockStorage[] arrays = this.chunk.func_76587_i();
      if (storageIdx >= 0 && storageIdx < arrays.length) {
         ExtendedBlockStorage section = arrays[storageIdx];
         if (section == Chunk.field_186036_a) {
            if (state.func_177230_c() == Blocks.field_150350_a) {
               return;
            }

            section = new ExtendedBlockStorage(y >> 4 << 4, this.chunk.func_177412_p().field_73011_w.func_191066_m());
            arrays[storageIdx] = section;
         }

         section.func_177484_a(x, y & 15, z, state);
      }
   }

   public int func_186138_a(int x, int z) {
      ExtendedBlockStorage[] arrays = this.chunk.func_76587_i();

      for (int y = this.ctx.maxY() - 1; y >= this.ctx.minY(); y--) {
         int storageIdx = this.ctx.toStorageIndex(y);
         if (storageIdx >= 0 && storageIdx < arrays.length) {
            ExtendedBlockStorage section = arrays[storageIdx];
            if (section != Chunk.field_186036_a) {
               IBlockState state = section.func_177485_a(x, y & 15, z);
               if (state != null && state != AIR) {
                  return y;
               }
            }
         }
      }

      return this.ctx.minY();
   }
}
