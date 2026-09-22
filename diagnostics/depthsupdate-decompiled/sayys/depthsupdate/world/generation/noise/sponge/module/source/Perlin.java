package sayys.depthsupdate.world.generation.noise.sponge.module.source;

import sayys.depthsupdate.world.generation.noise.sponge.Noise;
import sayys.depthsupdate.world.generation.noise.sponge.NoiseQuality;
import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Perlin extends Module {
   public static final double DEFAULT_PERLIN_FREQUENCY = 1.0;
   public static final double DEFAULT_PERLIN_LACUNARITY = 2.0;
   public static final int DEFAULT_PERLIN_OCTAVE_COUNT = 6;
   public static final double DEFAULT_PERLIN_PERSISTENCE = 0.5;
   public static final NoiseQuality DEFAULT_PERLIN_QUALITY = NoiseQuality.STANDARD;
   public static final int DEFAULT_PERLIN_SEED = 0;
   public static final int PERLIN_MAX_OCTAVE = 30;
   private double frequency = 1.0;
   private double lacunarity = 2.0;
   private NoiseQuality noiseQuality = DEFAULT_PERLIN_QUALITY;
   private int octaveCount = 6;
   private double persistence = 0.5;
   private int seed = 0;

   public Perlin() {
      super(0);
   }

   public double getFrequency() {
      return this.frequency;
   }

   public void setFrequency(double frequency) {
      this.frequency = frequency;
   }

   public double getLacunarity() {
      return this.lacunarity;
   }

   public void setLacunarity(double lacunarity) {
      this.lacunarity = lacunarity;
   }

   public NoiseQuality getNoiseQuality() {
      return this.noiseQuality;
   }

   public void setNoiseQuality(NoiseQuality noiseQuality) {
      this.noiseQuality = noiseQuality;
   }

   public int getOctaveCount() {
      return this.octaveCount;
   }

   public void setOctaveCount(int octaveCount) {
      if (octaveCount >= 1 && octaveCount <= 30) {
         this.octaveCount = octaveCount;
      } else {
         throw new IllegalArgumentException("octaveCount must be between 1 and MAX OCTAVE: 30");
      }
   }

   public double getPersistence() {
      return this.persistence;
   }

   public void setPersistence(double persistence) {
      this.persistence = persistence;
   }

   public int getSeed() {
      return this.seed;
   }

   public void setSeed(int seed) {
      this.seed = seed;
   }

   public double getMaxValue() {
      return (Math.pow(this.getPersistence(), this.getOctaveCount()) - 1.0) / (this.getPersistence() - 1.0);
   }

   @Override
   public int getSourceModuleCount() {
      return 0;
   }

   @Override
   public double getValue(double x, double y, double z) {
      double value = 0.0;
      double curPersistence = 1.0;
      double x1 = x * this.frequency;
      double y1 = y * this.frequency;
      double z1 = z * this.frequency;

      for (int curOctave = 0; curOctave < this.octaveCount; curOctave++) {
         double nx = Utils.makeInt32Range(x1);
         double ny = Utils.makeInt32Range(y1);
         double nz = Utils.makeInt32Range(z1);
         int seed = this.seed + curOctave;
         double signal = Noise.gradientCoherentNoise3D(nx, ny, nz, seed, this.noiseQuality);
         value += signal * curPersistence;
         x1 *= this.lacunarity;
         y1 *= this.lacunarity;
         z1 *= this.lacunarity;
         curPersistence *= this.persistence;
      }

      return value - 0.5 * this.getMaxValue();
   }
}
