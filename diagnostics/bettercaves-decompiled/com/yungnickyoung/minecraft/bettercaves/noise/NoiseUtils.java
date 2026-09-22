package com.yungnickyoung.minecraft.bettercaves.noise;

public class NoiseUtils {
   private NoiseUtils() {
   }

   public static float simplexNoiseOffsetByPercent(float baseNoise, float percent) {
      float currCDFPercent = 0.0F;
      float basePercent = baseNoise == -1.0F ? 0.0F : noiseToCDF(baseNoise);

      float currNoise;
      for (currNoise = baseNoise; currCDFPercent < percent && currNoise < 1.0F; currNoise += 0.01F) {
         currCDFPercent = noiseToCDF(currNoise) - basePercent;
      }

      return currNoise - 0.01F;
   }

   public static float simplexNoiseNegativeOffsetByPercent(float baseNoise, float percent) {
      float currCDFPercent = 0.0F;
      float currNoise = baseNoise;

      for (float basePercent = baseNoise == 1.0F ? 1.0F : noiseToCDF(baseNoise); currCDFPercent < percent && currNoise > -1.0F; currNoise -= 0.01F) {
         currCDFPercent = basePercent - noiseToCDF(currNoise);
      }

      return currNoise + 0.01F;
   }

   public static float noiseToCDF(float x) {
      return -0.435999F * x * x * x + 3.03E-4F * x * x + 0.916298F * x + 0.499721F;
   }
}
