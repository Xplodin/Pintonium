package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class NoodleCaveNoise {
   private static final double SELECTOR_WAVELENGTH = 256.0;
   private static final double THICKNESS_WAVELENGTH = 256.0;
   private static final double RIDGE_WAVELENGTH = 48.0;
   private static final double RIDGE_WEIGHT = 1.5;
   private static final double THICKNESS_BASE = -0.015;
   private static final double THICKNESS_SPREAD = 0.025;
   private static final double THICKNESS_MIN = -0.02;
   private static final double THICKNESS_MAX = -0.01;
   private static final int FADE_FROM_DEPTH = 6;
   private static final int FADE_TO_DEPTH = 2;
   private final Perlin selectorNoise;
   private final Perlin thicknessNoise;
   private final Perlin ridgeA;
   private final Perlin ridgeB;

   public NoodleCaveNoise(long seed) {
      this.selectorNoise = createNoise((int)seed + 601, 256.0);
      this.thicknessNoise = createNoise((int)seed + 602, 256.0);
      this.ridgeA = createNoise((int)seed + 603, 48.0);
      this.ridgeB = createNoise((int)seed + 604, 48.0);
   }

   private static Perlin createNoise(int seed, double wavelength) {
      Perlin perlin = new Perlin();
      perlin.setSeed(seed);
      perlin.setOctaveCount(1);
      perlin.setFrequency(1.0 / wavelength);
      return perlin;
   }

   public double selector(double x, int y, double z) {
      return this.selectorNoise.getValue(x, y, z);
   }

   public double density(double x, double y, double z, int depth) {
      double fade = thicknessFade(depth);
      if (fade <= 0.0) {
         return 1.5;
      } else {
         double thickness = Math.clamp(-0.015 + this.thicknessNoise.getValue(x, y, z) * 0.025, -0.02, -0.01) * fade;
         double ridge = Math.max(Math.abs(this.ridgeA.getValue(x, y, z)), Math.abs(this.ridgeB.getValue(x, y, z)));
         return 1.5 * ridge + thickness;
      }
   }

   private static double thicknessFade(int depth) {
      if (depth >= 6) {
         return 1.0;
      } else {
         return depth <= 2 ? 0.0 : (depth - 2) / 4.0;
      }
   }
}
