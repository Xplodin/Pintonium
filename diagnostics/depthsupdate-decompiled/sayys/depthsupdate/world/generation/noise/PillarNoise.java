package sayys.depthsupdate.world.generation.noise;

import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public final class PillarNoise {
   private static final double CUTOFF = 0.03;
   private static final double SHAPE_WAVELENGTH = 128.0;
   private static final double JITTER_WAVELENGTH = 32.0;
   private static final double JITTER_WEIGHT = 0.55;
   private static final double XZ_SCALE = 6.5;
   private static final double Y_SCALE = 1.0;
   private static final double REGION_WAVELENGTH = 256.0;
   private static final double PILLAR_NOISE_SCALE = 5.4;
   private static final double REGION_NOISE_SCALE = 5.0;
   private final Perlin shapeNoise;
   private final Perlin jitterNoise;
   private final Perlin rarenessNoise;
   private final Perlin thicknessNoise;
   private final double offsetX;
   private final double offsetY;
   private final double offsetZ;

   public PillarNoise(long seed, double offsetX, double offsetY, double offsetZ) {
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.offsetZ = offsetZ;
      this.shapeNoise = new Perlin();
      this.shapeNoise.setSeed((int)seed + 510);
      this.shapeNoise.setOctaveCount(1);
      this.shapeNoise.setFrequency(0.0078125);
      this.jitterNoise = new Perlin();
      this.jitterNoise.setSeed((int)seed + 513);
      this.jitterNoise.setOctaveCount(1);
      this.jitterNoise.setFrequency(0.03125);
      this.rarenessNoise = new Perlin();
      this.rarenessNoise.setSeed((int)seed + 511);
      this.rarenessNoise.setOctaveCount(1);
      this.rarenessNoise.setFrequency(0.00390625);
      this.thicknessNoise = new Perlin();
      this.thicknessNoise.setSeed((int)seed + 512);
      this.thicknessNoise.setOctaveCount(1);
      this.thicknessNoise.setFrequency(0.00390625);
   }

   public double density(double x, int y, double z) {
      double noiseX = x + this.offsetX;
      double noiseY = y + this.offsetY;
      double noiseZ = z + this.offsetZ;
      double scaledX = noiseX * 6.5;
      double scaledY = noiseY * 1.0;
      double scaledZ = noiseZ * 6.5;
      double pillar = (this.shapeNoise.getValue(scaledX, scaledY, scaledZ) + this.jitterNoise.getValue(scaledX, scaledY, scaledZ) * 0.55) * 5.4;
      double rareness = this.rarenessNoise.getValue(noiseX, noiseY, noiseZ) * 5.0;
      double thickness = 0.55 + 0.55 * Math.clamp(this.thicknessNoise.getValue(noiseX, noiseY, noiseZ) * 5.0, -1.0, 1.0);
      return (2.0 * pillar - 1.0 - rareness) * thickness * thickness * thickness;
   }

   public static boolean isSolid(double density) {
      return density > 0.03;
   }
}
