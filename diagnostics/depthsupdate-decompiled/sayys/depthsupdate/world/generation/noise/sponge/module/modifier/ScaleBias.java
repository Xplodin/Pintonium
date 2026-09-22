package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class ScaleBias extends Module {
   public static final double DEFAULT_BIAS = 0.0;
   public static final double DEFAULT_SCALE = 1.0;
   private double bias = 0.0;
   private double scale = 1.0;

   public ScaleBias() {
      super(1);
   }

   public double getBias() {
      return this.bias;
   }

   public void setBias(double bias) {
      this.bias = bias;
   }

   public double getScale() {
      return this.scale;
   }

   public void setScale(double scale) {
      this.scale = scale;
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
         return this.sourceModule[0].getValue(x, y, z) * this.scale + this.bias;
      }
   }
}
