package com.yungnickyoung.minecraft.bettercaves.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

@Config(modid = "bettercaves", name = "bettercaves-1_12_2")
public class Configuration {
   @Name("Underground Generation")
   @Comment("Configure settings related to caves, caverns, ravines and more.")
   public static ConfigUndergroundGen caveSettings = new ConfigUndergroundGen();
   @Name("Bedrock Generation")
   @Comment("Configure how bedrock generates in the overworld and nether.")
   public static ConfigBedrockGen bedrockSettings = new ConfigBedrockGen();
   @Name("Debug settings")
   @Comment("Don't mess with these settings for normal gameplay.")
   public static ConfigDebug debugsettings = new ConfigDebug();
   @Name("Whitelisted Dimension IDs")
   @Comment("List of ID's of dimensions that will have Better Caves. Ignored if Global Whitelisting is enabled.")
   @RequiresWorldRestart
   public static int[] whitelistedDimensionIDs = new int[]{0};
   @Name("Enable Global Whitelist")
   @Comment(
      "Automatically enables Better Caves in every possible dimension, except for the End.\n    If this is enabled, the Whitelisted Dimension IDs option is ignored.\nDefault: false"
   )
   @RequiresWorldRestart
   public static boolean enableGlobalWhitelist = false;
}
