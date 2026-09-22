package sayys.depthsupdate.world.generation.noise.sponge.module.source;

import sayys.depthsupdate.world.generation.noise.sponge.Noise;
import sayys.depthsupdate.world.generation.noise.sponge.Utils;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;

public class Voronoi extends Module {
   private static final double SQRT_3 = 1.7320508075688772;
   public static final double DEFAULT_VORONOI_DISPLACEMENT = 1.0;
   public static final double DEFAULT_VORONOI_FREQUENCY = 1.0;
   public static final int DEFAULT_VORONOI_SEED = 0;
   private double displacement = 1.0;
   private boolean enableDistance = false;
   private double frequency = 1.0;
   private int seed = 0;

   public Voronoi() {
      super(0);
   }

   public double getDisplacement() {
      return this.displacement;
   }

   public void setDisplacement(double displacement) {
      this.displacement = displacement;
   }

   public boolean isEnableDistance() {
      return this.enableDistance;
   }

   public void setEnableDistance(boolean enableDistance) {
      this.enableDistance = enableDistance;
   }

   public double getFrequency() {
      return this.frequency;
   }

   public void setFrequency(double frequency) {
      this.frequency = frequency;
   }

   public int getSeed() {
      return this.seed;
   }

   public void setSeed(int seed) {
      this.seed = seed;
   }

   @Override
   public int getSourceModuleCount() {
      return 0;
   }

   @Override
   public double getValue(double x, double y, double z) {
      double x1 = x * this.frequency;
      double y1 = y * this.frequency;
      double z1 = z * this.frequency;
      int xInt = x1 > 0.0 ? (int)x1 : (int)x1 - 1;
      int yInt = y1 > 0.0 ? (int)y1 : (int)y1 - 1;
      int zInt = z1 > 0.0 ? (int)z1 : (int)z1 - 1;
      double minDist = 2.147483647E9;
      double xCandidate = 0.0;
      double yCandidate = 0.0;
      double zCandidate = 0.0;

      for (int zCur = zInt - 2; zCur <= zInt + 2; zCur++) {
         for (int yCur = yInt - 2; yCur <= yInt + 2; yCur++) {
            for (int xCur = xInt - 2; xCur <= xInt + 2; xCur++) {
               double xPos = xCur + Noise.valueNoise3D(xCur, yCur, zCur, this.seed);
               double yPos = yCur + Noise.valueNoise3D(xCur, yCur, zCur, this.seed + 1);
               double zPos = zCur + Noise.valueNoise3D(xCur, yCur, zCur, this.seed + 2);
               double xDist = xPos - x1;
               double yDist = yPos - y1;
               double zDist = zPos - z1;
               double dist = xDist * xDist + yDist * yDist + zDist * zDist;
               if (dist < minDist) {
                  minDist = dist;
                  xCandidate = xPos;
                  yCandidate = yPos;
                  zCandidate = zPos;
               }
            }
         }
      }

      double value;
      if (this.enableDistance) {
         double xDist = xCandidate - x1;
         double yDist = yCandidate - y1;
         double zDist = zCandidate - z1;
         value = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist) / 1.7320508075688772;
      } else {
         value = 0.0;
      }

      return value + this.displacement * Noise.valueNoise3D(Utils.floor(xCandidate), Utils.floor(yCandidate), Utils.floor(zCandidate), this.seed);
   }
}
