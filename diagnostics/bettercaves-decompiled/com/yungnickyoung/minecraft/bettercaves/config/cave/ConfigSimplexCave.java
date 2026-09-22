package com.yungnickyoung.minecraft.bettercaves.config.cave;

import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresWorldRestart;

public class ConfigSimplexCave {
   @Name("Type 2 Cave Minimum Altitude")
   @Comment("The minimum y-coordinate at which type 2 caves can generate.\nDefault: 1")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveBottom = 1;
   @Name("Type 2 Cave Maximum Altitude")
   @Comment("The maximum y-coordinate at which type 2 caves can generate.\nDefault: 80")
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveTop = 80;
   @Name("Type 2 Cave Surface Cutoff Depth")
   @Comment(
      "The depth from a given point on the surface at which type 2 caves start to close off.\n    Will use the Max Cave Altitude instead of surface height if it is lower.\n    Will use the Max Cave Altitude no matter what if Override Surface Detection is enabled.\nDefault: 15 (recommended)"
   )
   @RangeInt(min = 0, max = 255)
   @RequiresWorldRestart
   public int caveSurfaceCutoff = 15;
   @Name("Compression - Vertical")
   @Comment("Stretches caves vertically. Lower value = taller caves with steeper drops.\nDefault: 2.2 (recommended)")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float yCompression = 2.2F;
   @Name("Compression - Horizontal")
   @Comment("Stretches caves horizontally. Lower value = wider caves.\nDefault: 0.9 (recommended)")
   @RangeDouble(min = 0.0, max = 100.0)
   @RequiresWorldRestart
   public float xzCompression = 0.9F;
   @Name("Type 2 Cave Priority")
   @Comment("Determines how frequently Type 2 Caves spawn. 0 = will not spawn at all.\nDefault: 5")
   @RangeInt(min = 0, max = 10)
   @RequiresWorldRestart
   public int cavePriority = 5;
   @Ignore
   @Name("Advanced Settings")
   @Comment("Don't mess with these if you don't know what you're doing.")
   public ConfigSimplexCave.Advanced advancedSettings = new ConfigSimplexCave.Advanced();

   public class Advanced {
      @Name("Noise Threshold")
      @Comment(
         "Noise threshold for determining which blocks get mined out as part of cave generation\n    Blocks with generated noise values greater than this threshold will be dug out.\nDefault: 0.82f"
      )
      @RangeDouble(min = -1.0, max = 1.0)
      @RequiresWorldRestart
      public float noiseThreshold = 0.82F;
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
         "The frequency for ridged multi-fractal noise generation.\n    This determines how spread out or tightly knit cave systems are.\nDefault: 0.025"
      )
      @RequiresWorldRestart
      public float fractalFrequency = 0.025F;
      @Name("Number of Generators")
      @Comment(
         "The number of noise generation functions used.\n    The intersection of these functions is used to calculate a single noise value.\n    Increasing this may decrease performance.\nDefault: 2"
      )
      @RequiresWorldRestart
      public int numGenerators = 2;
      @Name("Enable y-adjustment")
      @Comment("Enable y-adjustment, giving players more headroom in caves.\nDefault: true")
      public boolean yAdjust = true;
      @Name("y-adjustment Variable 1")
      @Comment(
         "Adjustment factor affecting the block immediately above a given block.\n    Higher value will tend to increase the headroom in caves.\nDefault: 0.95"
      )
      @RangeDouble(min = 0.0, max = 1.0)
      public float yAdjustF1 = 0.95F;
      @Name("y-adjustment Variable 2")
      @Comment(
         "Adjustment factor affecting the block two blocks above a given block.\n    Higher value will tend to increase the headroom in caves.\nDefault: 0.5"
      )
      @RangeDouble(min = 0.0, max = 1.0)
      public float yAdjustF2 = 0.5F;
      @Name("Noise Type")
      @Comment("This value is currently unused for Type 2 caves. ")
      public FastNoise.NoiseType noiseType = FastNoise.NoiseType.SimplexFractal;
   }
}
