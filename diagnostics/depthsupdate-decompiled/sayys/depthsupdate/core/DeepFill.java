package sayys.depthsupdate.core;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import sayys.depthsupdate.DepthsUpdateConfig;

public final class DeepFill {
   private DeepFill() {
   }

   public static IBlockState bandAt(int y, int minY, Random rand, IBlockState bedrock, IBlockState deepslate, IBlockState stone) {
      int deepslateMaxY = DepthsUpdateConfig.deepslateMaxY;
      int transitionRange = DepthsUpdateConfig.deepslateTransitionRange;
      if (y <= minY + rand.nextInt(5)) {
         return bedrock;
      } else if (y <= deepslateMaxY - transitionRange) {
         return deepslate;
      } else if (y < deepslateMaxY) {
         if (rand.nextDouble() < (double)(deepslateMaxY - y) / transitionRange) {
            return deepslate;
         } else {
            return y < 0 ? stone : null;
         }
      } else {
         return y < 0 ? stone : null;
      }
   }
}
