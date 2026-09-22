package com.yungnickyoung.minecraft.bettercaves.config.cave;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigVanillaCave {
   @Name("Vanilla Cave Minimum Altitude")
   @Comment("The minimum y-coordinate at which vanilla caves can generate.\nDefault: 8")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveBottom = 8;
   @Name("Vanilla Cave Maximum Altitude")
   @Comment("The maximum y-coordinate at which vanilla caves can generate.\nDefault: 128")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveTop = 128;
   @Name("Vanilla Cave Density")
   @Comment("The density of vanilla caves. Higher = more caves, closer together. \nDefault: 14 (value used in vanilla)")
   @RangeInt(min = 0, max = 100)
   @RequiresWorldRestart
   public int caveDensity = 14;
   @Name("Vanilla Cave Priority")
   @Comment("Determines how frequently vanilla caves spawn. 0 = will not spawn at all.\nDefault: 0")
   @RangeInt(min = 0, max = 10)
   @RequiresWorldRestart
   public int cavePriority = 0;
}
