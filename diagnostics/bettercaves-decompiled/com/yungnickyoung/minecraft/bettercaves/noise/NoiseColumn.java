package com.yungnickyoung.minecraft.bettercaves.noise;

import java.util.HashMap;
import java.util.Map;

public class NoiseColumn {
   private Map<Integer, NoiseTuple> columnValues = new HashMap<>();
   private int min = Integer.MAX_VALUE;
   private int max = Integer.MIN_VALUE;

   public void put(int y, NoiseTuple noiseTuple) {
      this.columnValues.put(y, noiseTuple);
      if (y < this.min) {
         this.min = y;
      }

      if (y > this.max) {
         this.max = y;
      }
   }

   public NoiseTuple get(int y) throws IndexOutOfBoundsException {
      if (y >= this.min && y <= this.max) {
         return this.columnValues.get(y);
      } else {
         throw new IndexOutOfBoundsException("No corresponding noise value in NoiseColumn for y-value: " + y);
      }
   }

   public Map<Integer, NoiseTuple> getColumnValues() {
      return this.columnValues;
   }
}
