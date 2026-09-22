package com.yungnickyoung.minecraft.bettercaves.config;

import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigWaterRegions {
   @Name("Water Region Spawn Chance")
   @Comment("Percent chance of a region having water instead of lava at low altitudes.\nDefault: 40%")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float waterRegionSpawnChance = 40.0F;
   @Name("Water Region Size")
   @Comment("Determines how large water regions are.\nDefault: Medium (recommended).")
   @RequiresWorldRestart
   public RegionSize waterRegionSize = RegionSize.Medium;
   @Name("Water Region Size Custom Value")
   @Comment(
      "Custom value for water region size. Smaller value = larger regions. This value is very sensitive to change.\n    ONLY WORKS IF Water Region Size IS Custom.\n    Provided values:\n        Small: 0.008\n        Medium: 0.004\n        Large: 0.0028\n        ExtraLarge: 0.001\nDefault: 0.004"
   )
   @RangeDouble(min = 0.0, max = 0.05)
   @RequiresWorldRestart
   public float waterRegionCustomSize = 0.004F;
}
