package sayys.depthsupdate.world.generation.noise;

public final class DensityField {
   public static final int CELL_WIDTH = 4;
   public static final int CELL_HEIGHT = 8;
   private static final int GRID_WIDTH = 5;
   private final DensityField.Sampler sampler;
   private final int gridMinY;
   private final int gridHeight;
   private final double[] values;

   public DensityField(DensityField.Sampler sampler, int minY, int maxY) {
      this.sampler = sampler;
      this.gridMinY = Math.floorDiv(minY, 8) * 8;
      int gridMaxY = Math.ceilDiv(maxY + 1, 8) * 8;
      this.gridHeight = (gridMaxY - this.gridMinY) / 8 + 1;
      this.values = new double[25 * this.gridHeight];
   }

   public void prepare(int chunkX, int chunkZ) {
      this.prepare(chunkX, chunkZ, Integer.MAX_VALUE);
   }

   public void prepare(int chunkX, int chunkZ, int highestY) {
      int worldX = chunkX * 16;
      int worldZ = chunkZ * 16;
      int index = 0;
      int rows = this.gridHeight;
      if (highestY != Integer.MAX_VALUE) {
         rows = Math.min(rows, Math.max(2, (highestY - this.gridMinY) / 8 + 2));
      }

      for (int gridY = 0; gridY < rows; gridY++) {
         int y = this.gridMinY + gridY * 8;

         for (int gridZ = 0; gridZ < 5; gridZ++) {
            double z = worldZ + gridZ * 4;

            for (int gridX = 0; gridX < 5; gridX++) {
               this.values[index++] = this.sampler.density(worldX + gridX * 4, y, z);
            }
         }
      }
   }

   public double get(int localX, int y, int localZ) {
      int gridX = localX / 4;
      int gridZ = localZ / 4;
      int offsetY = y - this.gridMinY;
      int gridY = offsetY / 8;
      double fractionX = localX % 4 / 4.0;
      double fractionZ = localZ % 4 / 4.0;
      double fractionY = offsetY % 8 / 8.0;
      double lowerZ = lerp(
         fractionX,
         lerp(fractionY, this.at(gridX, gridY, gridZ), this.at(gridX, gridY + 1, gridZ)),
         lerp(fractionY, this.at(gridX + 1, gridY, gridZ), this.at(gridX + 1, gridY + 1, gridZ))
      );
      double upperZ = lerp(
         fractionX,
         lerp(fractionY, this.at(gridX, gridY, gridZ + 1), this.at(gridX, gridY + 1, gridZ + 1)),
         lerp(fractionY, this.at(gridX + 1, gridY, gridZ + 1), this.at(gridX + 1, gridY + 1, gridZ + 1))
      );
      return lerp(fractionZ, lowerZ, upperZ);
   }

   private double at(int gridX, int gridY, int gridZ) {
      return this.values[(gridY * 5 + gridZ) * 5 + gridX];
   }

   private static double lerp(double fraction, double from, double to) {
      return from + fraction * (to - from);
   }

   @FunctionalInterface
   public interface Sampler {
      double density(double var1, int var3, double var4);
   }
}
