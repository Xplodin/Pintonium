package com.bean.beanutils.mixin;

import com.bean.beanutils.config.DepthUpdateCompat;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(targets = "com.sosnitzka.taiga.util.Generator", remap = false)
public abstract class MixinGenerator {
   @Overwrite
   public static void generateOreStoneVariant(IBlockState newState, EnumType type, Random random, int chunkX, int chunkZ, World world, int count) {
      List<EnumType> list = Lists.newArrayList(new EnumType[]{type});
      int minY = DepthUpdateCompat.getOverworldMinY();
      int maxY = 96;
      int upperStartMin = Math.min(maxY, Math.max(minY, 32));
      int upperStartRange = Math.max(1, maxY - upperStartMin + 1);
      int lowerStartMax = Math.max(minY, Math.min(maxY, 31));
      int lowerStartRange = Math.max(1, lowerStartMax - minY + 1);

      for (int i = 0; i < count; i += 2) {
         int posX = chunkX + random.nextInt(16);
         int posZ = chunkZ + random.nextInt(16);
         BlockPos cPos = new BlockPos(posX, random.nextInt(upperStartRange) + upperStartMin, posZ);
         IBlockState state = world.func_180495_p(cPos);
         if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())) {
            if (list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
               world.func_175656_a(cPos, newState);
            }
         } else {
            while (cPos.func_177956_o() >= minY) {
               cPos = cPos.func_177977_b();
               state = world.func_180495_p(cPos);
               if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())
                  && list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
                  world.func_175656_a(cPos, newState);
                  break;
               }
            }
         }

         cPos = new BlockPos(posX, random.nextInt(lowerStartRange) + minY, posZ);
         state = world.func_180495_p(cPos);
         if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())) {
            if (list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
               world.func_175656_a(cPos, newState);
            }
         } else {
            while (cPos.func_177956_o() <= maxY) {
               cPos = cPos.func_177984_a();
               state = world.func_180495_p(cPos);
               if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())
                  && list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
                  world.func_175656_a(cPos, newState);
                  break;
               }
            }
         }
      }
   }
}
