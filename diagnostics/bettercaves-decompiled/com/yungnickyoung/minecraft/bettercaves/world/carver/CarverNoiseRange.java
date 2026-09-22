package com.yungnickyoung.minecraft.bettercaves.world.carver;

import com.yungnickyoung.minecraft.bettercaves.noise.NoiseCube;
import com.yungnickyoung.minecraft.bettercaves.noise.NoiseUtils;

public class CarverNoiseRange {
   private float bottom;
   private float top;
   private float smoothBottomCutoff;
   private float smoothTopCutoff;
   private ICarver carver;
   private NoiseCube noiseCube;
   private static final float SMOOTH_PERCENT = 0.3F;

   public CarverNoiseRange(float bottom, float top, ICarver carver) {
      this.bottom = bottom;
      this.top = top;
      float smoothRangePercent = this.getPercentLength() * 0.3F;
      this.smoothBottomCutoff = NoiseUtils.simplexNoiseOffsetByPercent(bottom, smoothRangePercent);
      this.smoothTopCutoff = NoiseUtils.simplexNoiseNegativeOffsetByPercent(top, smoothRangePercent);
      this.carver = carver;
      this.noiseCube = null;
   }

   public boolean contains(float noiseValue) {
      return this.bottom <= noiseValue && noiseValue < this.top;
   }

   public float getSmoothAmp(float noiseValue) {
      if (this.bottom <= noiseValue && noiseValue <= this.smoothBottomCutoff) {
         return (noiseValue - this.bottom) / (this.smoothBottomCutoff - this.bottom);
      } else {
         return this.smoothTopCutoff <= noiseValue && noiseValue < this.top ? (noiseValue - this.top) / (this.smoothTopCutoff - this.top) : 1.0F;
      }
   }

   public float getPercentLength() {
      return (this.top == 1.0F ? 1.0F : NoiseUtils.noiseToCDF(this.top)) - (this.bottom == -1.0F ? 0.0F : NoiseUtils.noiseToCDF(this.bottom));
   }

   public ICarver getCarver() {
      return this.carver;
   }

   public NoiseCube getNoiseCube() {
      return this.noiseCube;
   }

   public void setNoiseCube(NoiseCube noiseCube) {
      this.noiseCube = noiseCube;
   }

   @Override
   public String toString() {
      return String.format(
         "[%2.2f, %2.2f] (%2.4f%%) -- smooth cutoffs: [%2.2f, %2.2f]",
         this.bottom,
         this.top,
         this.getPercentLength(),
         this.smoothBottomCutoff,
         this.smoothTopCutoff
      );
   }
}
