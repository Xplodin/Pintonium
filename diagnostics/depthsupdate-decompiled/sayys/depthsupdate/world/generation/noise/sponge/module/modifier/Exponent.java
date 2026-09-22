package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Exponent extends Module {
   public static final double DEFAULT_EXPONENT = 1.0;
   private double exponent = 1.0;

   public Exponent() {
      super(1);
   }

   public double getExponent() {
      return this.exponent;
   }

   public void setExponent(double exponent) {
      this.exponent = exponent;
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
         double value = this.sourceModule[0].getValue(x, y, z);
         return Math.pow(value, this.exponent);
      }
   }
}
