package com.yungnickyoung.minecraft.bettercaves.config.cavern;

import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigLiquidCavern {
   @Name("Liquid Cavern Minimum Altitude")
   @Comment("The minimum y-coordinate at which Liquid Caverns can generate.\nDefault: 1")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int cavernBottom = 1;
   @Name("Liquid Cavern Maximum Altitude")
   @Comment(
      "The maximum y-coordinate at which Liquid Caverns can generate.\n    Caverns will attempt to close off anyway if this value is greater than the surface's altitude.\nDefault: 35"
   )
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int cavernTop = 35;
   @Name("Compression - Vertical")
   @Comment("Stretches caverns vertically. Lower value = more open caverns with larger features.\nDefault: 1.3")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float yCompression = 1.3F;
   @Name("Compression - Horizontal")
   @Comment("Stretches caverns horizontally. Lower value = more open caverns with larger features.\nDefault: 0.7")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float xzCompression = 0.7F;
   @Name("Liquid Cavern Priority")
   @Comment("Determines how frequently Liquid Caverns spawn. 0 = will not spawn at all.\nDefault: 10")
   @RangeInt(min = 0, max = 10)
   @RequiresWorldRestart
   public int cavernPriority = 10;
   @Ignore
   @Name("Advanced Settings")
   @Comment("Don't mess with these if you don't know what you're doing.")
   public ConfigLiquidCavern.Advanced advancedSettings = new ConfigLiquidCavern.Advanced();

   public class Advanced {
      @Name("Noise Threshold")
      @Comment(
         "Noise threshold for determining which blocks get mined out as part of cavern generation\n    Blocks with generated noise values lower than this threshold will be dug out.\nDefault: 0.6"
      )
      @RangeDouble(min = -1.0, max = 1.0)
      @RequiresWorldRestart
      public float noiseThreshold = 0.6F;
      @Name("Fractal Octaves")
      @Comment("The number of octaves used for ridged multi-fractal noise generation.\nDefault: 1")
      @RequiresWorldRestart
      public int fractalOctaves = 1;
      @Name("Fractal Gain")
      @Comment("The gain for successive octaves of ridged multi-fractal noise generation.\nDefault: 0.3")
      @RequiresWorldRestart
      public float fractalGain = 0.3F;
      @Name("Fractal Frequency")
      @Comment(
         "The frequency for ridged multi-fractal noise generation.\n    This determines how spread out or tightly knit the formations in caverns are.\nDefault: 0.03"
      )
      @RequiresWorldRestart
      public float fractalFrequency = 0.03F;
      @Name("Number of Generators")
      @Comment(
         "The number of noise generation functions used.\n    The intersection of these functions is used to calculate a single noise value.\n    Increasing this may decrease performance.\nDefault: 2"
      )
      @RequiresWorldRestart
      public int numGenerators = 2;
      @Name("Noise Type")
      @Comment("Type of noise to use for this cavern. \nDefault: SimplexFractal")
      public FastNoise.NoiseType noiseType = FastNoise.NoiseType.SimplexFractal;
   }
}
