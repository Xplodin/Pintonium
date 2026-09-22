package sayys.depthsupdate.world.generation.noise;

public class PillarGenerator {
   private final DensityField field;
   private final int maxY;

   public PillarGenerator(long seed, double offsetX, double offsetY, double offsetZ, int caveMinY, int caveMaxY) {
      PillarNoise noise = new PillarNoise(seed, offsetX, offsetY, offsetZ);
      this.field = new DensityField(noise::density, caveMinY, caveMaxY);
      this.maxY = caveMaxY;
   }

   public void prepare(int chunkX, int chunkZ) {
      this.field.prepare(chunkX, chunkZ);
   }

   public boolean isPillar(int localX, int y, int localZ) {
      return y > this.maxY ? false : PillarNoise.isSolid(this.field.get(localX, y, localZ));
   }
}
