package com.yungnickyoung.minecraft.bettercaves.noise;

import java.util.ArrayList;
import java.util.List;

public class NoiseTuple {
   private List<Double> noiseValues = new ArrayList<>();
   private int length = 0;

   public NoiseTuple(double... vals) {
      for (double val : vals) {
         this.noiseValues.add(val);
         this.length++;
      }
   }

   public void put(double val) {
      this.noiseValues.add(val);
      this.length++;
   }

   public double get(int index) throws IndexOutOfBoundsException {
      if (index >= 0 && index < this.length) {
         return this.noiseValues.get(index);
      } else {
         throw new IndexOutOfBoundsException("No corresponding noise value in Noise Tuple for index: " + index);
      }
   }

   public void set(int index, double newValue) throws IndexOutOfBoundsException {
      if (index >= 0 && index < this.length) {
         this.noiseValues.set(index, newValue);
      } else {
         throw new IndexOutOfBoundsException("No corresponding noise value in Noise Tuple for index: " + index);
      }
   }

   public NoiseTuple times(float magnitude) {
      NoiseTuple result = new NoiseTuple();

      for (int i = 0; i < this.length; i++) {
         result.put(this.noiseValues.get(i) * magnitude);
      }

      return result;
   }

   public NoiseTuple plus(NoiseTuple other) {
      NoiseTuple result = new NoiseTuple();

      for (int i = 0; i < this.length; i++) {
         result.put(this.noiseValues.get(i) + other.get(i));
      }

      return result;
   }

   public List<Double> getNoiseValues() {
      return this.noiseValues;
   }

   public int size() {
      return this.length;
   }
}
