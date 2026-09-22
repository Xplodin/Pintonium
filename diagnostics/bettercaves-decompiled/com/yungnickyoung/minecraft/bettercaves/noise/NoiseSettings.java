package com.yungnickyoung.minecraft.bettercaves.noise;

public class NoiseSettings {
   private FastNoise.NoiseType noiseType = FastNoise.NoiseType.SimplexFractal;
   private FastNoise.FractalType fractalType = FastNoise.FractalType.FBM;
   private int octaves = 3;
   private float gain = 0.5F;
   private float frequency = 0.01F;

   public FastNoise.NoiseType getNoiseType() {
      return this.noiseType;
   }

   public FastNoise.FractalType getFractalType() {
      return this.fractalType;
   }

   public int getOctaves() {
      return this.octaves;
   }

   public float getGain() {
      return this.gain;
   }

   public float getFrequency() {
      return this.frequency;
   }

   public NoiseSettings setNoiseType(FastNoise.NoiseType noiseType) {
      this.noiseType = noiseType;
      return this;
   }

   public NoiseSettings setFractalType(FastNoise.FractalType fractalType) {
      this.fractalType = fractalType;
      return this;
   }

   public NoiseSettings setOctaves(int octaves) {
      this.octaves = octaves;
      return this;
   }

   public NoiseSettings setGain(float gain) {
      this.gain = gain;
      return this;
   }

   public NoiseSettings setFrequency(float frequency) {
      this.frequency = frequency;
      return this;
   }
}
