package sayys.depthsupdate.world.generation.noise;

public interface ICaveGenerator {
   boolean canGenerate();

   default void prepare(int chunkX, int chunkZ) {
   }

   default void prepare(int chunkX, int chunkZ, int highestY) {
      this.prepare(chunkX, chunkZ);
   }

   void sample(CaveSampleContext var1);
}
