package sayys.depthsupdate.core;

import sayys.depthsupdate.api.HeightInfo;

public final class HeightContext implements HeightInfo {
   public static final int MAX_STORAGE_SECTIONS = 32;
   public static final HeightContext VANILLA = new HeightContext(0, 256, -54, -64, 63);
   private final int minY;
   private final int maxY;
   private final int totalHeight;
   private final int minSection;
   private final int maxSection;
   private final int negativeSections;
   private final int upperSections;
   private final int totalStorageSections;
   private final int seaLevel;
   private final int lavaLevel;
   private final int voidDamageLevel;
   private final int yBitShift;
   private final int primerArraySize;

   public HeightContext(int minY, int maxY, int lavaLevel, int voidDamageLevel) {
      this(minY, maxY, lavaLevel, voidDamageLevel, 63);
   }

   public HeightContext(int minY, int maxY, int lavaLevel, int voidDamageLevel, int seaLevel) {
      if (minY % 16 != 0) {
         throw new IllegalArgumentException("minY must be a multiple of 16, got " + minY);
      } else if (maxY % 16 != 0) {
         throw new IllegalArgumentException("maxY must be a multiple of 16, got " + maxY);
      } else if (minY >= maxY) {
         throw new IllegalArgumentException("minY (" + minY + ") must be less than maxY (" + maxY + ")");
      } else if (maxY - minY > 4096) {
         throw new IllegalArgumentException("Total height (" + (maxY - minY) + ") exceeds maximum of 4096");
      } else {
         int sections = 16 + Math.max(0, (maxY - 1 >> 4) - 15) + Math.max(0, -(minY >> 4));
         if (sections > 32) {
            throw new IllegalArgumentException(
               "Height " + minY + ".." + maxY + " needs " + sections + " chunk sections, which exceeds the 32 a section bitmask can hold"
            );
         } else {
            this.minY = minY;
            this.maxY = maxY;
            this.totalHeight = maxY - minY;
            this.minSection = minY >> 4;
            this.maxSection = maxY - 1 >> 4;
            this.negativeSections = Math.max(0, -(minY >> 4));
            this.upperSections = Math.max(0, (maxY - 1 >> 4) - 15);
            this.totalStorageSections = 16 + this.upperSections + this.negativeSections;
            this.seaLevel = seaLevel;
            this.lavaLevel = lavaLevel;
            this.voidDamageLevel = voidDamageLevel;
            int bits = 0;

            for (int h = this.totalHeight - 1; h > 0; bits++) {
               h >>= 1;
            }

            this.yBitShift = bits;
            this.primerArraySize = 256 * (1 << bits);
         }
      }
   }

   public int toStorageIndex(int y) {
      int sectionY = y >> 4;
      if (sectionY >= 0 && sectionY < 16) {
         return sectionY;
      } else if (sectionY >= 16 && sectionY <= this.maxSection) {
         return sectionY;
      } else {
         return sectionY < 0 && sectionY >= this.minSection ? 16 + this.upperSections + (-1 - sectionY) : -1;
      }
   }

   public int fromStorageIndex(int index) {
      if (index >= 0 && index < 16) {
         return index;
      } else {
         int upperEnd = 16 + this.upperSections;
         if (index >= 16 && index < upperEnd) {
            return index;
         } else {
            int negEnd = upperEnd + this.negativeSections;
            return index >= upperEnd && index < negEnd ? -1 - (index - upperEnd) : Integer.MIN_VALUE;
         }
      }
   }

   public int toPrimerIndex(int x, int y, int z) {
      return x << this.yBitShift + 4 | z << this.yBitShift | y - this.minY;
   }

   @Override
   public boolean isInBounds(int y) {
      return y >= this.minY && y < this.maxY;
   }

   @Override
   public boolean isExtended() {
      return this.minY < 0 || this.maxY > 256;
   }

   @Override
   public int minY() {
      return this.minY;
   }

   @Override
   public int maxY() {
      return this.maxY;
   }

   @Override
   public int totalHeight() {
      return this.totalHeight;
   }

   public int minSection() {
      return this.minSection;
   }

   public int maxSection() {
      return this.maxSection;
   }

   public int negativeSections() {
      return this.negativeSections;
   }

   public int upperSections() {
      return this.upperSections;
   }

   public int totalStorageSections() {
      return this.totalStorageSections;
   }

   @Override
   public int seaLevel() {
      return this.seaLevel;
   }

   @Override
   public int lavaLevel() {
      return this.lavaLevel;
   }

   @Override
   public int voidDamageLevel() {
      return this.voidDamageLevel;
   }

   public int yBitShift() {
      return this.yBitShift;
   }

   public int primerArraySize() {
      return this.primerArraySize;
   }

   public int fullChunkSectionMask() {
      return this.totalStorageSections >= 32 ? -1 : (1 << this.totalStorageSections) - 1;
   }

   @Override
   public String toString() {
      return "HeightContext[minY="
         + this.minY
         + ", maxY="
         + this.maxY
         + ", sections="
         + this.totalStorageSections
         + " (vanilla=16, upper="
         + this.upperSections
         + ", negative="
         + this.negativeSections
         + ")]";
   }
}
