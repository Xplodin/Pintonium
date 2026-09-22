package com.yungnickyoung.minecraft.bettercaves.config.cave;

import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigCaves {
   @Name("Type 1 Caves")
   @Comment("Settings used in the generation of type 1 caves, which are more worm-like.")
   public ConfigCubicCave cubicCave = new ConfigCubicCave();
   @Name("Type 2 Caves")
   @Comment("Settings used in the generation of type 2 caves, which tend to be more open and spacious.")
   public ConfigSimplexCave simplexCave = new ConfigSimplexCave();
   @Name("Surface Caves")
   @Comment("Settings used in the generation of vanilla-like caves near the surface.")
   public ConfigSurfaceCave surfaceCave = new ConfigSurfaceCave();
   @Name("Vanilla Caves")
   @Comment("Settings controlling vanilla Minecraft cave generation.")
   public ConfigVanillaCave vanillaCave = new ConfigVanillaCave();
   @Name("Cave Spawn Chance")
   @Comment("Percent chance of caves spawning in a given region.\nDefault: caves spawn in 100% of regions.")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float caveSpawnChance = 100.0F;
   @Name("Cave Region Size")
   @Comment(
      "Determines how large cave regions are.\n    Controls how long a cave system of a certain cave type extends before intersecting with a cave system of another type.\n    Larger = more cave interconnectivity for a given area, but less variation.\nDefault: Small (recommended)."
   )
   @RequiresWorldRestart
   public RegionSize caveRegionSize = RegionSize.Small;
   @Name("Cave Region Size Custom Value")
   @Comment(
      "Custom value for cave region size. Smaller value = larger regions. This value is very sensitive to change.\n    ONLY WORKS IF Cave Region Size IS Custom.\n    Provided values:\n        Small: 0.008\n        Medium: 0.005\n        Large: 0.0032\n        ExtraLarge: 0.001\nDefault: 0.008"
   )
   @RangeDouble(min = 0.0, max = 0.05)
   @RequiresWorldRestart
   public float customRegionSize = 0.008F;
}
