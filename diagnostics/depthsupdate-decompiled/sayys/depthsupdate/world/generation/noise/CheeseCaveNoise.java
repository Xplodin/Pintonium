package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class CheeseCaveNoise {
   private static final double DENSITY_OFFSET = 0.4;
   private static final double LAYER_WEIGHT = 4.0;
   private static final double SURFACE_SLIDE_MAX = 0.7;
   private static final int SURFACE_SLIDE_FROM_DEPTH = 20;
   private static final int SURFACE_SLIDE_TO_DEPTH = 4;
   private static final double Y_SCALE = 1.0;
   private static final double LAYER_Y_SCALE = 8.0;
   private static final double NOISE_NORMALIZER = 2.6;
   private static final double LAYER_NOISE_SCALE = 7.5;
   private static final double WIDE_WAVELENGTH = 256.0;
   private static final double MAIN_WAVELENGTH = 64.0;
   private static final double DETAIL_WAVELENGTH = 16.0;
   private static final double LAYER_WAVELENGTH = 256.0;
   private final Perlin wideNoise;
   private final Perlin mainNoise;
   private final Perlin detailNoise;
   private final Perlin layerNoise;
   private final double offsetX;
   private final double offsetY;
   private final double offsetZ;
   private final double densityOffset;

   public CheeseCaveNoise(long seed, double offsetX, double offsetY, double offsetZ, double abundance) {
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.densityOffset = 0.4 * abundance;
      this.wideNoise = new Perlin();
      this.wideNoise.setSeed((int)seed + 420);
      this.wideNoise.setOctaveCount(2);
      this.wideNoise.setPersistence(2.0);
      this.wideNoise.setFrequency(0.00390625);
      this.mainNoise = new Perlin();
      this.mainNoise.setSeed((int)seed + 421);
      this.mainNoise.setOctaveCount(2);
      this.mainNoise.setPersistence(0.5);
      this.mainNoise.setFrequency(0.015625);
      this.detailNoise = new Perlin();
      this.detailNoise.setSeed((int)seed + 422);
      this.detailNoise.setOctaveCount(2);
      this.detailNoise.setPersistence(0.5);
      this.detailNoise.setFrequency(0.0625);
      this.layerNoise = new Perlin();
      this.layerNoise.setSeed((int)seed + 423);
      this.layerNoise.setOctaveCount(1);
      this.layerNoise.setFrequency(0.00390625);
   }

   public static double surfaceSlide(int depth) {
      if (depth >= 20) {
         return 0.0;
      } else {
         return depth <= 4 ? 0.7 : 0.7 * (20 - depth) / 16.0;
      }
   }

   public double density(double x, int y, double z) {
      double noiseX = x + this.offsetX;
      double noiseY = y + this.offsetY;
      double noiseZ = z + this.offsetZ;
      double stretchedY = noiseY * 1.0;
      double cheese = (
            this.wideNoise.getValue(noiseX, stretchedY, noiseZ) * 0.5
               + this.mainNoise.getValue(noiseX, stretchedY, noiseZ) * 2.0
               + this.detailNoise.getValue(noiseX, stretchedY, noiseZ) * 2.0
         )
         * 2.6;
      double layer = this.layerNoise.getValue(noiseX, noiseY * 8.0, noiseZ) * 7.5;
      return Math.clamp(cheese + this.densityOffset, -1.0, 1.0) + 4.0 * layer * layer;
   }
}
