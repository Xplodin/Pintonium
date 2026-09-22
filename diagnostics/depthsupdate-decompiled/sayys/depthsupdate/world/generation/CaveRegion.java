package sayys.depthsupdate.world.generation;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import sayys.depthsupdate.core.HeightContext;

public final class CaveRegion {
   public static final int NO_Y = Integer.MIN_VALUE;

   private CaveRegion() {
   }

   public static int randomYInWindow(Random random, HeightContext ctx, int windowMin, int windowMax) {
      int low = Math.max(Math.min(windowMin, windowMax), ctx.minY());
      int high = Math.min(Math.max(windowMin, windowMax), ctx.maxY() - 1);
      return low > high ? Integer.MIN_VALUE : low + random.nextInt(high - low + 1);
   }

   public static double volume(int radiusX, int radiusY, int radiusZ) {
      return (Math.PI * 4.0 / 3.0) * radiusX * radiusY * radiusZ;
   }

   public static BlockPos randomPointInside(Random random, BlockPos center, int radiusX, int radiusY, int radiusZ) {
      double x;
      double y;
      double z;
      do {
         x = random.nextDouble() * 2.0 - 1.0;
         y = random.nextDouble() * 2.0 - 1.0;
         z = random.nextDouble() * 2.0 - 1.0;
      } while (!(x * x + y * y + z * z <= 1.0));

      return center.func_177982_a((int)(x * radiusX), (int)(y * radiusY), (int)(z * radiusZ));
   }
}
