package sayys.depthsupdate.world.generation.noise.sponge.module.modifier;

import sayys.depthsupdate.world.generation.noise.sponge.exception.NoModuleException;
import sayys.depthsupdate.world.generation.noise.sponge.module.Module;
import sayys.depthsupdate.world.generation.noise.sponge.module.source.Perlin;

public class Turbulence extends Module {
   public static final double DEFAULT_TURBULENCE_POWER = 1.0;
   private double power = 1.0;
   private final Perlin xDistortModule = new Perlin();
   private final Perlin yDistortModule = new Perlin();
   private final Perlin zDistortModule = new Perlin();

   public Turbulence() {
      super(1);
   }

   public double getPower() {
      return this.power;
   }

   public void setPower(double power) {
      this.power = power;
   }

   public int getRoughnessCount() {
      return this.xDistortModule.getOctaveCount();
   }

   public double getFrequency() {
      return this.xDistortModule.getFrequency();
   }

   public int getSeed() {
      return this.xDistortModule.getSeed();
   }

   public void setSeed(int seed) {
      this.xDistortModule.setSeed(seed);
      this.yDistortModule.setSeed(seed + 1);
      this.zDistortModule.setSeed(seed + 2);
   }

   public void setFrequency(double frequency) {
      this.xDistortModule.setFrequency(frequency);
      this.yDistortModule.setFrequency(frequency);
      this.zDistortModule.setFrequency(frequency);
   }

   public void setRoughness(int roughness) {
      this.xDistortModule.setOctaveCount(roughness);
      this.yDistortModule.setOctaveCount(roughness);
      this.zDistortModule.setOctaveCount(roughness);
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
         double x0 = x + 0.1894226F;
         double y0 = y + 0.9937134F;
         double z0 = z + 0.47816467F;
         double x1 = x + 0.40464783F;
         double y1 = y + 0.27661133F;
         double z1 = z + 0.9230499F;
         double x2 = x + 0.821228F;
         double y2 = y + 0.1710968F;
         double z2 = z + 0.6842804F;
         double xDistort = x + this.xDistortModule.getValue(x0, y0, z0) * this.power;
         double yDistort = y + this.yDistortModule.getValue(x1, y1, z1) * this.power;
         double zDistort = z + this.zDistortModule.getValue(x2, y2, z2) * this.power;
         return this.sourceModule[0].getValue(xDistort, yDistort, zDistort);
      }
   }
}
