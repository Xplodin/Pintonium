package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Invert extends Module {
   public static final double DEFAULT_MIDDLE = 0.0;
   private double middle = 0.0;

   public Invert() {
      super(1);
   }

   public double getMiddle() {
      return this.middle;
   }

   public void setMiddle(double middle) {
      this.middle = middle;
   }

   @Override
   public int getSourceModuleCount() {
      return 1;
   }

   @Override
   public double getValue(double x, double y, double z) {
      if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         return this.middle - this.sourceModule[0].getValue(x, y, z);
      }
   }
}
