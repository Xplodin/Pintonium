package com.bean.beanutils.config;

import java.io.File;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class DepthUpdateCompat {
   private static final String CATEGORY_GENERAL = "general";
   private static final String CATEGORY_HEIGHT = "general.height extension";
   private static int overworldMinY = -128;
   private static int overworldLavaY = -116;
   private static int netherMinY = -128;
   private static String deepslateBlockId = "depthsupdate:deepslate";
   private static boolean loaded;

   private DepthUpdateCompat() {
   }

   public static void reload() {
      loaded = true;
      overworldMinY = -128;
      overworldLavaY = -116;
      netherMinY = -128;
      deepslateBlockId = "depthsupdate:deepslate";
      File configFile = new File(Loader.instance().getConfigDir(), "depthsupdate.cfg");
      if (configFile.isFile()) {
         Configuration cfg = new Configuration(configFile);

         try {
            cfg.load();
            deepslateBlockId = cfg.getString("Deepslate Block", "general", deepslateBlockId, "Registry name of the block to use as deepslate.");
            overworldMinY = cfg.getInt("Global Minimum Y", "general.height extension", overworldMinY, -2048, 0, "Minimum Y for extended dimensions.");
            overworldLavaY = cfg.getInt(
               "Lava Level", "general.height extension", overworldLavaY, -2048, 2048, "Y level at which underground air is replaced with lava."
            );
            String[] overrides = cfg.getStringList(
               "Dimension Overrides", "general.height extension", new String[0], "Format: dim:minY:maxY or dim:minY:maxY:lava:void"
            );

            for (String raw : overrides) {
               if (raw != null) {
                  String line = raw.trim();
                  if (!line.isEmpty()) {
                     String[] parts = line.split(":");
                     if (parts.length >= 3) {
                        try {
                           int dim = Integer.parseInt(parts[0].trim());
                           int minY = Integer.parseInt(parts[1].trim());
                           if (dim == 0) {
                              overworldMinY = minY;
                           } else if (dim == -1) {
                              netherMinY = minY;
                           }
                        } catch (NumberFormatException var14) {
                        }
                     }
                  }
               }
            }
         } finally {
            if (cfg.hasChanged()) {
               cfg.save();
            }
         }
      }
   }

   private static void ensureLoaded() {
      if (!loaded) {
         reload();
      }
   }

   public static int getOverworldMinY() {
      ensureLoaded();
      return overworldMinY;
   }

   public static int getOverworldLavaY() {
      ensureLoaded();
      return overworldLavaY;
   }

   public static int getNetherMinY() {
      ensureLoaded();
      return netherMinY;
   }

   public static IBlockState getDeepHost() {
      ensureLoaded();
      Block block = (Block)ForgeRegistries.BLOCKS.getValue(new ResourceLocation(deepslateBlockId));
      return block != null ? block.func_176223_P() : Blocks.field_150348_b.func_176223_P();
   }
}
