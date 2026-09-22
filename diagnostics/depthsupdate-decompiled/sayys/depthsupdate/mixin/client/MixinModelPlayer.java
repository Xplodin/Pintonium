package sayys.depthsupdate.mixin.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.item.ItemSpyglass;

@Mixin(ModelPlayer.class)
public abstract class MixinModelPlayer extends ModelBiped {
   @Inject(at = @At("RETURN"), method = "Lnet/minecraft/client/model/ModelPlayer;func_78087_a(FFFFFFLnet/minecraft/entity/Entity;)V")
   private void depthsupdate$spyglassPose(
      float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci
   ) {
      if (entityIn instanceof EntityLivingBase living) {
         if (living.func_184587_cr() && living.func_184607_cu().func_77973_b() instanceof ItemSpyglass) {
            boolean isRightHand = living.func_184591_cq() == EnumHandSide.RIGHT;
            if (living.func_184600_cs() == EnumHand.OFF_HAND) {
               isRightHand = !isRightHand;
            }

            if (isRightHand) {
               this.field_178723_h.field_78795_f = this.field_78116_c.field_78795_f - 1.9198622F;
               this.field_178723_h.field_78796_g = this.field_78116_c.field_78796_g - (float) (Math.PI / 12);
            } else {
               this.field_178724_i.field_78795_f = this.field_78116_c.field_78795_f - 1.9198622F;
               this.field_178724_i.field_78796_g = this.field_78116_c.field_78796_g + (float) (Math.PI / 12);
            }
         }
      }
   }
}
