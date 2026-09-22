package com.yungnickyoung.minecraft.bettercaves.config.cave;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigSurfaceCave {
   @Name("Enable Surface Caves")
   @Comment("Set to true to enable vanilla-like caves which provide nice, natural-looking openings at the surface.\nDefault: true")
   public boolean enableSurfaceCaves = true;
   @Name("Surface Cave Minimum Altitude")
   @Comment("The minimum y-coordinate at which surface caves can generate.\nDefault: 40")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveBottom = 40;
   @Name("Surface Cave Maximum Altitude")
   @Comment("The maximum y-coordinate at which surface caves can generate.\nDefault: 128")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveTop = 128;
   @Name("Surface Cave Density")
   @Comment("The density of surface caves. Higher = more caves, closer together. \nDefault: 17")
   @RangeInt(min = 0, max = 100)
   @RequiresWorldRestart
   public int caveDensity = 17;
}
