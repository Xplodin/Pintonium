package sayys.depthsupdate.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleSporeBlossomFall extends Particle {
   public ParticleSporeBlossomFall(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn) {
      super(worldIn, xCoordIn, yCoordIn, zCoordIn);
      this.field_187129_i = 0.0;
      this.field_187130_j = 0.0;
      this.field_187131_k = 0.0;
      this.field_70552_h = 0.32F;
      this.field_70553_i = 0.5F;
      this.field_70551_j = 0.22F;
      this.func_70536_a(113);
      this.func_187115_a(0.01F, 0.01F);
      this.field_70545_g = 0.015F;
      this.field_70547_e = (int)(64.0 / (Math.random() * 0.8 + 0.2));
   }

   public void func_189213_a() {
      this.field_187123_c = this.field_187126_f;
      this.field_187124_d = this.field_187127_g;
      this.field_187125_e = this.field_187128_h;
      if (this.field_70547_e-- <= 0) {
         this.func_187112_i();
      }

      this.field_187130_j = this.field_187130_j - this.field_70545_g;
      this.func_187110_a(this.field_187129_i, this.field_187130_j, this.field_187131_k);
      this.field_187129_i *= 0.98F;
      this.field_187130_j *= 0.98F;
      this.field_187131_k *= 0.98F;
      if (this.field_187132_l) {
         this.func_187112_i();
      }
   }
}
