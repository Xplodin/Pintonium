package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Range extends Module {
   public static final double DEFAULT_CURRENT_LOWER_BOUND = -1.0;
   public static final double DEFAULT_CURRENT_UPPER_BOUND = 1.0;
   public static final double DEFAULT_NEW_LOWER_BOUND = 0.0;
   public static final double DEFAULT_NEW_UPPER_BOUND = 1.0;
   private double currentLowerBound = -1.0;
   private double currentUpperBound = 1.0;
   private double newLowerBound = 0.0;
   private double newUpperBound = 1.0;
   private double scale;
   private double bias;

   public Range() {
      super(1);
   }

   public double getCurrentLowerBound() {
      return this.currentLowerBound;
   }

   public double getCurrentUpperBound() {
      return this.currentUpperBound;
   }

   public double getNewLowerBound() {
      return this.newLowerBound;
   }

   public double getNewUpperBound() {
      return this.newUpperBound;
   }

   private void recalculateScaleBias() {
      this.scale = (this.getNewUpperBound() - this.getNewLowerBound()) / (this.getCurrentUpperBound() - this.getCurrentLowerBound());
      this.bias = this.getNewLowerBound() - this.getCurrentLowerBound() * this.scale;
   }

   public void setBounds(double currentLower, double currentUpper, double newLower, double newUpper) {
      if (currentLower == currentUpper) {
         throw new IllegalArgumentException("currentLower must not equal currentUpper. Both are " + currentUpper);
      } else if (newLower == newUpper) {
         throw new IllegalArgumentException("newLowerBound must not equal newUpperBound. Both are " + newUpper);
      } else {
         this.currentLowerBound = currentLower;
         this.currentUpperBound = currentUpper;
         this.newLowerBound = newLower;
         this.newUpperBound = newUpper;
         this.recalculateScaleBias();
      }
   }

   @Override
   public int getSourceModuleCount() {
      return 1;
   }

   @Override
   public double getValue(double x, double y, double z) {
      if (this.sourceModule == null) {
         throw new NoModuleException();
      } else if (this.sourceModule[0] == null) {
         throw new NoModuleException();
      } else {
         double oldVal = this.sourceModule[0].getValue(x, y, z);
         return oldVal * this.scale + this.bias;
      }
   }
}
