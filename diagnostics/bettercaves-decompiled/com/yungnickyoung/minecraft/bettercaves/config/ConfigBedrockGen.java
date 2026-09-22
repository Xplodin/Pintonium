package com.yungnickyoung.minecraft.bettercaves.config;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigBedrockGen {
   @Name("Flatten Bedrock")
   @Comment(
      "Replaces the usual bedrock generation pattern with flat layers.\n    Activates in all whitelisted dimension, where applicable. The End is unaffected."
   )
   @RequiresWorldRestart
   public boolean flattenBedrock = true;
   @Name("Bedrock Layer Width")
   @Comment("The width of the bedrock layer. Only works if Flatten Bedrock is true.")
   @RequiresWorldRestart
   @RangeInt(min = 0, max = 256)
   public int bedrockWidth = 1;
}
