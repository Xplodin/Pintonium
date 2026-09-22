package sayys.depthsupdate.mixin;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.core.HeightManager;

@Mixin(EntityBoat.class)
public abstract class MixinEntityBoat {
   @Shadow
   private double field_184465_aD;
   @Unique
   private static final double depthsupdate$floatTargetSubmersion = 0.35;
   @Unique
   private static final double depthsupdate$floatSeekGain = 0.1;
   @Unique
   private static final double depthsupdate$floatMaxRiseSpeed = 0.1;
   @Unique
   private static final double depthsupdate$floatMaxSinkSpeed = 0.02;

   @Inject(at = @At("HEAD"), method = "Lnet/minecraft/entity/item/EntityBoat;func_184450_w()V")
   private void depthsupdate$sanitizeBuoyancy(CallbackInfo ci) {
      EntityBoat self = (EntityBoat)this;
      if (HeightManager.isExtended(self.field_70170_p)) {
         if (this.field_184465_aD == Double.MIN_VALUE) {
            this.field_184465_aD = self.func_174813_aQ().field_72338_b;
         }
      }
   }

   @Inject(at = @At("RETURN"), method = "Lnet/minecraft/entity/item/EntityBoat;func_184450_w()V")
   private void depthsupdate$floatInDepths(CallbackInfo ci) {
      EntityBoat self = (EntityBoat)this;
      if (!(self.field_70163_u >= 0.0) && HeightManager.isExtended(self.field_70170_p)) {
         AxisAlignedBB bb = self.func_174813_aQ();
         double surface = this.depthsupdate$findWaterSurface(self, bb);
         if (!Double.isNaN(surface) && !(surface < bb.field_72338_b - 0.05)) {
            double error = surface - 0.35 - bb.field_72338_b;
            self.field_70181_x = MathHelper.func_151237_a(error * 0.1, -0.02, 0.1);
         }
      }
   }

   @Unique
   private double depthsupdate$findWaterSurface(EntityBoat self, AxisAlignedBB bb) {
      double inset = 0.1;
      double[][] columns = new double[][]{
         {bb.field_72340_a + inset, bb.field_72339_c + inset},
         {bb.field_72340_a + inset, bb.field_72334_f - inset},
         {bb.field_72336_d - inset, bb.field_72339_c + inset},
         {bb.field_72336_d - inset, bb.field_72334_f - inset},
         {(bb.field_72340_a + bb.field_72336_d) / 2.0, (bb.field_72339_c + bb.field_72334_f) / 2.0}
      };
      int yTop = MathHelper.func_76128_c(bb.field_72337_e) + 1;
      int yBottom = MathHelper.func_76128_c(bb.field_72338_b) - 1;
      double best = Double.NaN;
      MutableBlockPos pos = new MutableBlockPos();

      for (double[] column : columns) {
         int x = MathHelper.func_76128_c(column[0]);
         int z = MathHelper.func_76128_c(column[1]);

         for (int y = yTop; y >= yBottom; y--) {
            pos.func_181079_c(x, y, z);
            IBlockState state = self.field_70170_p.func_180495_p(pos);
            if (state.func_185904_a() == Material.field_151586_h) {
               double height = y + BlockLiquid.func_190973_f(state, self.field_70170_p, pos);
               if (Double.isNaN(best) || height > best) {
                  best = height;
               }
               break;
            }
         }
      }

      return best;
   }
}
