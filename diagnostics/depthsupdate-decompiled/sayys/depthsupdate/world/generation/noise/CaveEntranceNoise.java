package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class CaveEntranceNoise {
   private static final double DENSITY_OFFSET = 0.37;
   private static final int SURFACE_SLIDE_FROM_DEPTH = 16;
   private static final int SURFACE_SLIDE_TO_DEPTH = 56;
   private static final double SURFACE_SLIDE_MAX = 2.0;
   private static final int FLOOR_FROM_Y = 0;
   private static final int FLOOR_TO_Y = -30;
   private static final double FLOOR_MAX = 2.0;
   private static final double NOISE_NORMALIZER = 2.35;
   private static final double RARITY_BIAS = 1.15;
   private static final double WAVELENGTH = 128.0;
   private static final double Y_SCALE = 0.5;
   private final Perlin noise;
   private final double offsetX;
   private final double offsetY;
   private final double offsetZ;

   public CaveEntranceNoise(long seed, double offsetX, double offsetY, double offsetZ) {
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.noise = new Perlin();
      this.noise.setSeed((int)seed + 517);
      this.noise.setOctaveCount(3);
      this.noise.setPersistence(2.0);
      this.noise.setFrequency(0.0078125);
   }

   public static double surfaceSlide(int depth) {
      if (depth <= 16) {
         return 0.0;
      } else {
         return depth >= 56 ? 2.0 : 2.0 * (depth - 16) / 40.0;
      }
   }

   public static boolean closedAtDepth(int depth) {
      return depth >= 56;
   }

   private static double floor(int y) {
      if (y >= 0) {
         return 0.0;
      } else {
         return y <= -30 ? 2.0 : 2.0 * (0 - y) / 30.0;
      }
   }

   public double density(double x, int y, double z) {
      double value = this.noise.getValue(x + this.offsetX, (y + this.offsetY) * 0.5, z + this.offsetZ) * 2.35;
      return value + 0.37 + 1.15 + floor(y);
   }
}
