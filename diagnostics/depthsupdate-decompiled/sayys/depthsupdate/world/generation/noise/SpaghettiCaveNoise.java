package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class SpaghettiCaveNoise {
   private static final double SCALE = 0.035;
   public static final double THICKNESS = 0.025;
   private static final int FADE_FROM_DEPTH = 8;
   private static final int FADE_TO_DEPTH = 2;
   private final Perlin noiseA = new Perlin();
   private final Perlin noiseB;

   public SpaghettiCaveNoise(long seed) {
      this.noiseA.setSeed((int)seed + 1337);
      this.noiseA.setOctaveCount(2);
      this.noiseB = new Perlin();
      this.noiseB.setSeed((int)seed + 7331);
      this.noiseB.setOctaveCount(2);
   }

   public double ridge(double x, double y, double z) {
      double a = this.noiseA.getValue(x * 0.035, y * 0.035, z * 0.035);
      double b = this.noiseB.getValue(x * 0.035, y * 0.035, z * 0.035);
      return Math.max(Math.abs(a), Math.abs(b));
   }

   public static double thicknessFade(int depth) {
      if (depth >= 8) {
         return 1.0;
      } else {
         return depth <= 2 ? 0.0 : (depth - 2) / 6.0;
      }
   }
}
