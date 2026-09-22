package com.sosnitzka.taiga.util;

import com.google.common.collect.Lists;
import com.sosnitzka.taiga.world.MeteorWorldSaveData;
import com.sosnitzka.taiga.world.WorldGenMinable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.oredict.OreDictionary;

public class Generator {
   public static void generateOre(
      IBlockState newState, IBlockState oldState, Random random, int chunkX, int chunkZ, World world, int count, int minY, int maxY, int minSize, int maxSize
   ) {
      generateOre(newState, oldState, null, null, random, chunkX, chunkZ, world, count, 100, minY, maxY, minSize, maxSize, null);
   }

   public static void generateOre(
      IBlockState newState,
      IBlockState oldState,
      Random random,
      int chunkX,
      int chunkZ,
      World world,
      int count,
      int chance,
      int minY,
      int maxY,
      int minSize,
      int maxSize,
      List<Biome> biome
   ) {
      generateOre(newState, oldState, null, null, random, chunkX, chunkZ, world, count, chance, minY, maxY, minSize, maxSize, biome);
   }

   public static void generateOre(
      IBlockState newState,
      IBlockState oldState,
      IProperty property,
      Comparable comparable,
      Random random,
      int chunkX,
      int chunkZ,
      World world,
      int count,
      int chance,
      int minY,
      int maxY,
      int minSize,
      int maxSize,
      List<Biome> biome
   ) {
      int size = minSize + random.nextInt(maxSize - minSize);
      int height = maxY - minY;

      for (int i = 0; i < count; i++) {
         if (0.01F * chance >= random.nextFloat()) {
            int posX = chunkX + random.nextInt(16);
            int posY = random.nextInt(height) + minY;
            int posZ = chunkZ + random.nextInt(16);
            BlockPos cPos = new BlockPos(posX, posY, posZ);
            if (biome == null || biome.contains(world.func_180494_b(cPos))) {
               new WorldGenMinable(newState, size, StateMatcher.forState(oldState, property, comparable))
                  .func_180709_b(world, random, new BlockPos(posX, posY, posZ));
            }
         }
      }
   }

   public static void generateOre(
      List<IBlockState> replaceBlockList,
      IBlockState replacementBlock,
      Random random,
      int chunkX,
      int chunkZ,
      World world,
      int count,
      int minY,
      int maxY,
      int chance
   ) {
      if (random.nextFloat() < (float)(0.01 * chance)) {
         generateOreDescending(replaceBlockList, replacementBlock, random, chunkX, chunkZ, world, count, minY, maxY);
      }
   }

   public static void generateOreDescending(
      List<IBlockState> replaceBlockList, IBlockState replacementBlock, Random random, int chunkX, int chunkZ, World world, int count, int minY, int maxY
   ) {
      for (int i = 0; i < count; i++) {
         int posX = chunkX + random.nextInt(16);
         int posZ = chunkZ + random.nextInt(16);
         BlockPos cPos = new BlockPos(posX, maxY, posZ);
         if (!replaceBlockList.contains(world.func_180495_p(cPos)) || !replaceBlockList.contains(world.func_180495_p(cPos.func_177984_a()))) {
            if (replaceBlockList.contains(world.func_180495_p(cPos)) && !replaceBlockList.contains(world.func_180495_p(cPos.func_177984_a()))) {
               world.func_175656_a(cPos, replacementBlock);
            }

            while (!replaceBlockList.contains(world.func_180495_p(cPos.func_177977_b())) && cPos.func_177956_o() > minY) {
               cPos = cPos.func_177977_b();
            }

            if (replaceBlockList.contains(world.func_180495_p(cPos.func_177977_b()))) {
               world.func_175656_a(cPos.func_177977_b(), replacementBlock);
            }
         }
      }
   }

   public static void generateOreStoneVariant(IBlockState newState, EnumType type, Random random, int chunkX, int chunkZ, World world, int count) {
      List<EnumType> list = Lists.newArrayList(new EnumType[]{type});

      for (int i = 0; i < count; i += 2) {
         int posX = chunkX + random.nextInt(16);
         int posZ = chunkZ + random.nextInt(16);
         BlockPos cPos = new BlockPos(posX, random.nextInt(64) + 32, posZ);
         IBlockState state = world.func_180495_p(cPos);
         if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())) {
            if (list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
               world.func_175656_a(cPos, newState);
            }
         } else {
            while (cPos.func_177956_o() >= 0) {
               cPos = cPos.func_177977_b();
               state = world.func_180495_p(cPos);
               if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())
                  && list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
                  world.func_175656_a(cPos, newState);
                  break;
               }
            }
         }

         cPos = new BlockPos(posX, random.nextInt(32), posZ);
         state = world.func_180495_p(cPos);
         if (state.func_177230_c().equals(Blocks.field_150348_b.func_176223_P().func_177230_c())) {
            if (list.contains(state.func_177229_b(BlockStone.field_176247_a))) {
               world.func_175656_a(cPos, newState);
            }
         } else {
            while (cPos.func_177956_o() <= 96) {
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

   public static void generateOreBottom(
      IBlockState oldState, IBlockState newState, Random random, int chunkX, int chunkZ, World world, int chance, int spread, int maxY
   ) {
      for (int i = 0; i < chance; i++) {
         int posX = chunkX + random.nextInt(16);
         int posY = 0;
         int posZ = chunkZ + random.nextInt(16);
         BlockPos cPos = new BlockPos(posX, posY, posZ);
         if (Blocks.field_150350_a.func_176223_P().equals(world.func_180495_p(cPos))) {
            while (world.func_180495_p(cPos).equals(Blocks.field_150350_a.func_176223_P()) && cPos.func_177956_o() < maxY) {
               cPos = cPos.func_177984_a();
            }

            if (world.func_180495_p(cPos).equals(oldState)) {
               world.func_175656_a(cPos.func_177981_b(random.nextInt(spread)), newState);
            }
         }
      }
   }

   public static void generateCube(
      boolean fly,
      IBlockState centerBlock,
      IBlockState hullBlock,
      Random random,
      int chunkX,
      int chunkZ,
      World world,
      int count,
      int chance,
      int minY,
      int maxY,
      int maxS
   ) {
      for (int i = 0; i < count; i++) {
         if (random.nextFloat() < 0.01 * chance) {
            int outer = Utils.nextInt(random, 1, maxS);
            int inner = random.nextInt(2);
            int posX = chunkX + random.nextInt(16);
            int posY = Utils.nextInt(random, minY, maxY);
            int posZ = chunkZ + random.nextInt(16);
            BlockPos cPos = new BlockPos(posX, posY, posZ);
            if (!fly
               && world.func_180495_p(cPos).equals(Blocks.field_150350_a.func_176223_P())
               && world.func_180495_p(cPos.func_177977_b()).equals(Blocks.field_150350_a.func_176223_P())) {
               while (world.func_180495_p(cPos.func_177977_b()).equals(Blocks.field_150350_a.func_176223_P())) {
                  cPos = cPos.func_177977_b();
               }
            }

            cPos.func_177979_c((random.nextInt(4) + 2) * outer);

            for (int x = -inner; x <= inner; x++) {
               for (int y = -inner; y <= inner; y++) {
                  for (int z = -inner; z <= inner; z++) {
                     if (world.func_180495_p(cPos).equals(Blocks.field_150350_a.func_176223_P())) {
                        world.func_175656_a(new BlockPos(cPos.func_177958_n() + x, cPos.func_177956_o() + y, cPos.func_177952_p() + z), centerBlock);
                     }
                  }
               }
            }

            for (int x = -outer; x <= outer; x++) {
               for (int y = -outer; y <= outer; y++) {
                  for (int zx = -outer; zx <= outer; zx++) {
                     BlockPos nPos = new BlockPos(cPos.func_177958_n() + x, cPos.func_177956_o() + y, cPos.func_177952_p() + zx);
                     if (!world.func_180495_p(nPos).equals(centerBlock) && world.func_180495_p(nPos).equals(Blocks.field_150350_a.func_176223_P())) {
                        world.func_175656_a(nPos, hullBlock);
                     }
                  }
               }
            }
         }
      }
   }

   public static int generateMeteor(
      IBlockState centerBlock, IBlockState hullBlock, Random random, int chunkX, int chunkZ, World world, int count, int chance, int minY, int maxY
   ) {
      Set<Item> validSurface = new HashSet<>();

      for (String e : Lists.newArrayList(new String[]{"dirt", "grass", "stone", "sand", "gravel", "cobblestone", "sandstone"})) {
         for (ItemStack stack : OreDictionary.getOres(e)) {
            validSurface.add(stack.func_77973_b());
         }
      }

      int mGenerated = 0;

      for (int i = 0; i < count; i++) {
         if (random.nextFloat() < 0.01 * chance) {
            int r = Utils.nextInt(random, 1, 5);
            int posX = chunkX + random.nextInt(16);
            int posY = Utils.nextInt(random, minY, maxY);
            int posZ = chunkZ + random.nextInt(16);
            BlockPos cPos = new BlockPos(posX, posY, posZ);
            if (world.func_180495_p(cPos).equals(Blocks.field_150350_a.func_176223_P())
               && world.func_180495_p(cPos.func_177977_b()).equals(Blocks.field_150350_a.func_176223_P())) {
               while (world.func_180495_p(cPos.func_177977_b()).equals(Blocks.field_150350_a.func_176223_P())) {
                  cPos = cPos.func_177977_b();
                  if (cPos.func_177956_o() < minY) {
                     break;
                  }
               }
            }

            if (validSurface.contains(Item.func_150898_a(world.func_180495_p(cPos.func_177977_b()).func_177230_c()))) {
               cPos = cPos.func_177979_c(random.nextInt(r * 2) + r + 1);
               MeteorWorldSaveData saveData = MeteorWorldSaveData.getForWorld(world);
               saveData.addPos(cPos);
               saveData.func_76185_a();
               mGenerated++;
               int t = 1;
               if (r > 3) {
                  t = random.nextInt(r - 1);
               }

               for (int x = -t; x <= t; x++) {
                  for (int y = -t; y <= t; y++) {
                     for (int z = -t; z <= t; z++) {
                        if (!(MathHelper.func_76129_c(x * x + y * y + z * z) > t)) {
                           world.func_175656_a(new BlockPos(cPos.func_177958_n() + x, cPos.func_177956_o() + y, cPos.func_177952_p() + z), centerBlock);
                        }
                     }
                  }
               }

               for (int x = -r; x <= r; x++) {
                  for (int y = -r; y <= r; y++) {
                     for (int zx = -r; zx <= r; zx++) {
                        if (!(MathHelper.func_76129_c(x * x + y * y + zx * zx) > r)) {
                           BlockPos nPos = new BlockPos(cPos.func_177958_n() + x, cPos.func_177956_o() + y, cPos.func_177952_p() + zx);
                           if (!world.func_180495_p(nPos).equals(centerBlock)) {
                              world.func_175656_a(nPos, hullBlock);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return mGenerated;
   }
}
