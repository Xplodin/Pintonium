package com.bean.beanutils.mixin;

import com.bean.beanutils.config.DepthUpdateCompat;
import com.google.common.collect.Lists;
import com.sosnitzka.taiga.TAIGA;
import com.sosnitzka.taiga.TAIGAConfiguration;
import com.sosnitzka.taiga.util.Generator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.sosnitzka.taiga.world.WorldGen", remap = false)
public abstract class MixinWorldGen {
   @Shadow
   private List blackList;
   @Shadow
   private Map meteorGenStats;
   @Shadow
   private Map meteorChunkStats;

   @Unique
   private static int tdp$getInt(Map map, int key) {
      Object value = map.get(key);
      return value instanceof Number ? ((Number)value).intValue() : 0;
   }

   @Unique
   private static void tdp$generateCommonOverworldOres(Random random, int x, int z, World world) {
      int overworldMinY = DepthUpdateCompat.getOverworldMinY();
      int overworldLavaY = DepthUpdateCompat.getOverworldLavaY();
      IBlockState deepHost = DepthUpdateCompat.getDeepHost();
      Generator.generateOreDescending(
         Lists.newArrayList(new IBlockState[]{Blocks.field_150353_l.func_176223_P(), Blocks.field_150356_k.func_176223_P()}),
         com.sosnitzka.taiga.Blocks.basaltBlock.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.BASALT_VAL,
         0,
         64
      );
      Generator.generateOreDescending(
         Lists.newArrayList(new IBlockState[]{Blocks.field_150353_l.func_176223_P(), Blocks.field_150356_k.func_176223_P()}),
         com.sosnitzka.taiga.Blocks.basaltBlock.func_176223_P(),
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.BASALT_VAL / 2),
         overworldMinY,
         overworldLavaY + 20
      );
      Generator.generateOreDescending(
         Lists.newArrayList(new IBlockState[]{Blocks.field_150357_h.func_176223_P()}),
         com.sosnitzka.taiga.Blocks.eezoOre.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.EEZO_VAL,
         overworldMinY,
         overworldMinY + 10
      );
      Generator.generateOreStoneVariant(
         com.sosnitzka.taiga.Blocks.karmesineOre.func_176223_P(), EnumType.ANDESITE, random, x, z, world, TAIGAConfiguration.KARMESINE_VAL
      );
      Generator.generateOreStoneVariant(
         com.sosnitzka.taiga.Blocks.oviumOre.func_176223_P(), EnumType.DIORITE, random, x, z, world, TAIGAConfiguration.OVIUM_VAL
      );
      Generator.generateOreStoneVariant(
         com.sosnitzka.taiga.Blocks.jauxumOre.func_176223_P(), EnumType.GRANITE, random, x, z, world, TAIGAConfiguration.JAUXUM_VAL
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.vibraniumOre.func_176223_P(),
         Blocks.field_150348_b.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.VIBRANIUM_VAL,
         100,
         0,
         64,
         2,
         6,
         Lists.newArrayList(new Biome[]{Biomes.field_76786_s, Biomes.field_76770_e, Biomes.field_76783_v, Biomes.field_150580_W, Biomes.field_76769_d})
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.vibraniumOre.func_176223_P(),
         deepHost,
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.VIBRANIUM_VAL / 2),
         100,
         overworldMinY,
         0,
         2,
         6,
         Lists.newArrayList(new Biome[]{Biomes.field_76786_s, Biomes.field_76770_e, Biomes.field_76783_v, Biomes.field_150580_W, Biomes.field_76769_d})
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.dilithiumOre.func_176223_P(),
         Blocks.field_150348_b.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.DILITHIUM_VAL,
         100,
         0,
         64,
         2,
         8,
         Lists.newArrayList(
            new Biome[]{
               Biomes.field_76769_d,
               Biomes.field_76786_s,
               Biomes.field_185442_R,
               Biomes.field_76771_b,
               Biomes.field_150575_M,
               Biomes.field_76776_l,
               Biomes.field_76787_r
            }
         )
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.dilithiumOre.func_176223_P(),
         deepHost,
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.DILITHIUM_VAL / 2),
         100,
         Math.max(overworldMinY, -96),
         0,
         2,
         8,
         Lists.newArrayList(
            new Biome[]{
               Biomes.field_76769_d,
               Biomes.field_76786_s,
               Biomes.field_185442_R,
               Biomes.field_76771_b,
               Biomes.field_150575_M,
               Biomes.field_76776_l,
               Biomes.field_76787_r
            }
         )
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.vibraniumOre.func_176223_P(), Blocks.field_150348_b.func_176223_P(), random, x, z, world, 1, 15, 0, 128, 1, 5, null
      );
      Generator.generateOre(com.sosnitzka.taiga.Blocks.vibraniumOre.func_176223_P(), deepHost, random, x, z, world, 1, 15, overworldMinY, 0, 1, 5, null);
      if (TAIGAConfiguration.ironGen) {
         Generator.generateOre(
            Blocks.field_150366_p.func_176223_P(), Blocks.field_150348_b.func_176223_P(), random, x, z, world, TAIGAConfiguration.IRON_VAL, 0, 32, 2, 8
         );
         Generator.generateOre(
            Blocks.field_150366_p.func_176223_P(),
            deepHost,
            random,
            x,
            z,
            world,
            Math.max(1, TAIGAConfiguration.IRON_VAL / 2),
            overworldMinY,
            Math.min(-32, -1),
            2,
            8
         );
      }
   }

   @Overwrite
   private void nether(Random random, int x, int z, World world) {
      int netherMinY = DepthUpdateCompat.getNetherMinY();
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.tiberiumOre.func_176223_P(),
         Blocks.field_150424_aL.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.TIBERIUM_VAL,
         32,
         128,
         10,
         35
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.tiberiumOre.func_176223_P(),
         Blocks.field_150424_aL.func_176223_P(),
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.TIBERIUM_VAL / 3),
         netherMinY,
         32,
         10,
         35
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.prometheumOre.func_176223_P(),
         Blocks.field_150424_aL.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.PROMETHEUM_VAL,
         0,
         32,
         2,
         4
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.prometheumOre.func_176223_P(),
         Blocks.field_150424_aL.func_176223_P(),
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.PROMETHEUM_VAL / 2),
         Math.max(netherMinY, -96),
         0,
         2,
         4
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.valyriumOre.func_176223_P(),
         Blocks.field_150424_aL.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.VALYRIUM_VAL,
         0,
         128,
         2,
         4
      );
      Generator.generateOre(
         com.sosnitzka.taiga.Blocks.valyriumOre.func_176223_P(),
         Blocks.field_150424_aL.func_176223_P(),
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.VALYRIUM_VAL / 2),
         netherMinY,
         0,
         2,
         4
      );
      Generator.generateOre(
         Lists.newArrayList(new IBlockState[]{Blocks.field_150353_l.func_176223_P(), Blocks.field_150356_k.func_176223_P()}),
         com.sosnitzka.taiga.Blocks.osramOre.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.OSRAM_VAL,
         0,
         64,
         15
      );
      Generator.generateOre(
         Lists.newArrayList(new IBlockState[]{Blocks.field_150353_l.func_176223_P(), Blocks.field_150356_k.func_176223_P()}),
         com.sosnitzka.taiga.Blocks.osramOre.func_176223_P(),
         random,
         x,
         z,
         world,
         Math.max(1, TAIGAConfiguration.OSRAM_VAL / 2),
         netherMinY,
         0,
         15
      );
   }

   @Overwrite
   private void world(Random random, int x, int z, World world) {
      Generator.generateMeteor(
         com.sosnitzka.taiga.Blocks.duraniteOre.func_176223_P(),
         com.sosnitzka.taiga.Blocks.blockMeteorite.func_176223_P(),
         random,
         x,
         z,
         world,
         TAIGAConfiguration.DURANITE_VAL,
         6,
         16,
         112
      );
      tdp$generateCommonOverworldOres(random, x, z, world);
   }

   @Overwrite
   private void other(Random random, int x, int z, World world) {
      int dim = world.field_73011_w.getDimension();
      if (!this.meteorGenStats.containsKey(dim)) {
         this.meteorGenStats.put(dim, 0);
      }

      if (!this.meteorChunkStats.containsKey(dim)) {
         this.meteorChunkStats.put(dim, 0);
      }

      this.meteorChunkStats.put(dim, tdp$getInt(this.meteorChunkStats, dim) + 1);
      this.meteorGenStats
         .put(
            dim,
            tdp$getInt(this.meteorGenStats, dim)
               + Generator.generateMeteor(
                  com.sosnitzka.taiga.Blocks.duraniteOre.func_176223_P(),
                  com.sosnitzka.taiga.Blocks.blockMeteorite.func_176223_P(),
                  random,
                  x,
                  z,
                  world,
                  TAIGAConfiguration.DURANITE_VAL,
                  6,
                  16,
                  112
               )
         );
      tdp$generateCommonOverworldOres(random, x, z, world);
      if (tdp$getInt(this.meteorChunkStats, dim) > 100 && tdp$getInt(this.meteorGenStats, dim) == 0) {
         this.blackList.add(dim);
         TAIGA.logger.info(String.format("Detected void dimension, adding to blacklist: %d", dim));
      }
   }
}
