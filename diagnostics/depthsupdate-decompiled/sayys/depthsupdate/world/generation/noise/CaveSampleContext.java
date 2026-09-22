package sayys.depthsupdate.world.generation.noise;

import net.minecraft.block.state.IBlockState;

public class CaveSampleContext {
   public double realX;
   public double realY;
   public double realZ;
   public int localX;
   public int localZ;
   public int y;
   public int depth;
   public double density;
   public int openMask;
   public boolean shouldDebug;
   public IBlockState debugBlock;

   public void reset(double realX, double realY, double realZ, int localX, int y, int localZ, int depth) {
      this.realX = realX;
      this.realY = realY;
      this.realZ = realZ;
      this.localX = localX;
      this.localZ = localZ;
      this.y = y;
      this.depth = depth;
      this.density = Double.POSITIVE_INFINITY;
      this.openMask = 0;
      this.shouldDebug = false;
      this.debugBlock = null;
   }

   public void offer(CaveType type, double density) {
      if (density < this.density) {
         this.density = density;
      }

      if (density < 0.0) {
         this.openMask = this.openMask | 1 << type.ordinal();
      }
   }

   public boolean shouldCarve() {
      return this.density < 0.0;
   }
}
