package com.yungnickyoung.minecraft.bettercaves.config;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigMisc {
   @Name("Liquid Altitude")
   @Comment("Lava (or water in water regions) spawns at and below this y-coordinate.\nDefault: 10")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int liquidAltitude = 10;
   @Name("Lava Block")
   @Comment(
      "The block used for lava generation at and below the Liquid Altitude.\n    Defaults to regular lava if an invalid block is given.\nDefault: minecraft:lava"
   )
   @RequiresWorldRestart
   public String lavaBlock = "minecraft:lava";
   @Name("Water Block")
   @Comment(
      "The block used for water generation in water caves/caverns at and below the Liquid Altitude.\n    Defaults to regular water if an invalid block is given.\nDefault: minecraft:water"
   )
   @RequiresWorldRestart
   public String waterBlock = "minecraft:water";
   @Name("Prevent Cascading Gravel")
   @Comment(
      "Replace naturally generated floating gravel on the ocean floor with andesite.\n    Can prevent lag due to cascading gravel falling into caverns under the ocean.\nDefault: true"
   )
   @RequiresWorldRestart
   public boolean replaceFloatingGravel = true;
   @Name("Override Surface Detection")
   @Comment(
      "Ignores surface detection for closing off caves and caverns, forcing them to spawn\n    up until their max height. Useful for Nether-like dimensions with no real \"surface\".\nDefault: false"
   )
   @RequiresWorldRestart
   public boolean overrideSurfaceDetection = false;
   @Name("Enable Flooded Underground")
   @Comment("Set to true to enable flooded underground in ocean biomes.\nDefault: true")
   public boolean enableFloodedUnderground = true;
}
