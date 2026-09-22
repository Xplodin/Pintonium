package com.yungnickyoung.minecraft.bettercaves.config.cavern;

import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigCaverns {
   @Name("Liquid Caverns")
   @Comment("Settings used in the generation of Liquid Caverns found at low altitudes.\n    These are caverns where the floor is predominantly water or lava.")
   public ConfigLiquidCavern liquidCavern = new ConfigLiquidCavern();
   @Name("Floored Caverns")
   @Comment("Parameters used in the generation of Floored Caverns.\n    These have much more ground to walk on than Liquid Caverns.")
   public ConfigFlooredCavern flooredCavern = new ConfigFlooredCavern();
   @Name("Cavern Spawn Chance")
   @Comment("Percent chance of caverns spawning in a given region.\nDefault: caverns spawn in 25% of regions.")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float cavernSpawnChance = 25.0F;
   @Name("Cavern Region Size")
   @Comment("Determines how large cavern regions are. This controls the average size of caverns.\nDefault: Small")
   @RequiresWorldRestart
   public RegionSize cavernRegionSize = RegionSize.Small;
   @Name("Cavern Region Size Custom Value")
   @Comment(
      "Custom value for cavern region size. Only works if Cavern Region Size is set to Custom.     Smaller value = larger regions. This value is very sensitive to change.\n    Provided values:\n        Small: 0.01\n        Medium: 0.007\n        Large: 0.005\n        ExtraLarge: 0.001\nDefault: 0.01"
   )
   @RangeDouble(min = 0.0, max = 0.05)
   @RequiresWorldRestart
   public float customRegionSize = 0.01F;
}
