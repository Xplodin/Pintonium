package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;
import org.jspecify.annotations.NonNull;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;

public class CheeseCaveGenerator implements ICaveGenerator {
   private static final double DEBUG_BAND = 0.25;
   private final int maxY;
   private final IBlockState debugBlockBlockState = BlockUtils.getCheeseDebugBlockState();
   private final DensityField field;

   public CheeseCaveGenerator(long seed, double offsetX, double offsetY, double offsetZ, int caveMinY, int caveMaxY) {
      this.maxY = caveMaxY;
      CheeseCaveNoise noise = new CheeseCaveNoise(seed, offsetX, offsetY, offsetZ, DepthsUpdateConfig.cheeseCavesAbundance);
      this.field = new DensityField(noise::density, caveMinY, caveMaxY);
   }

   @Override
   public boolean canGenerate() {
      return DepthsUpdateConfig.generateCheeseCaves;
   }

   @Override
   public void prepare(int chunkX, int chunkZ) {
      this.field.prepare(chunkX, chunkZ);
   }

   @Override
   public void prepare(int chunkX, int chunkZ, int highestY) {
      this.field.prepare(chunkX, chunkZ, highestY);
   }

   @Override
   public void sample(@NonNull CaveSampleContext context) {
      if (context.y <= this.maxY) {
         double density = this.field.get(context.localX, context.y, context.localZ) + CheeseCaveNoise.surfaceSlide(context.depth);
         context.offer(CaveType.CHEESE, density);
         if (!context.shouldDebug && density >= 0.0 && DepthsUpdateConfig.DEBUG.enableDebugVisualizers && density < 0.25) {
            context.shouldDebug = true;
            context.debugBlock = this.debugBlockBlockState;
         }
      }
   }
}
