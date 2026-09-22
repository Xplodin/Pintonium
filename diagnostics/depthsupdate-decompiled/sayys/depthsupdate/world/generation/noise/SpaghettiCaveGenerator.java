package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.util.BlockUtils;

public class SpaghettiCaveGenerator implements ICaveGenerator {
   private static final double DEBUG_BAND = 0.03;
   private final IBlockState debugBlockBlockState = BlockUtils.getSpaghettiDebugBlockState();
   private final SpaghettiCaveNoise noise;

   public SpaghettiCaveGenerator(long seed) {
      this.noise = new SpaghettiCaveNoise(seed);
   }

   @Override
   public boolean canGenerate() {
      return DepthsUpdateConfig.generateSpaghettiCaves;
   }

   @Override
   public void sample(CaveSampleContext context) {
      double fade = SpaghettiCaveNoise.thicknessFade(context.depth);
      if (!(fade <= 0.0)) {
         double value = this.noise.ridge(context.realX, context.realY, context.realZ);
         context.offer(CaveType.SPAGHETTI, value - 0.025 * fade);
         if (!context.shouldDebug && DepthsUpdateConfig.DEBUG.enableDebugVisualizers && value >= 0.025 && value < 0.055) {
            context.shouldDebug = true;
            context.debugBlock = this.debugBlockBlockState;
         }
      }
   }
}
