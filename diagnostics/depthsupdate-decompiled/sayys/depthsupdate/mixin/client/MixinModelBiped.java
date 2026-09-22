package sayys.depthsupdate.mixin.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.item.ItemSpyglass;

@Mixin(ModelBiped.class)
public class MixinModelBiped {
   @Shadow
   public ModelRenderer field_178723_h;
   @Shadow
   public ModelRenderer field_178724_i;
   @Shadow
   public ArmPose field_187075_l;
   @Shadow
   public ArmPose field_187076_m;
   @Shadow
   public ModelRenderer field_78116_c;

   @Inject(at = @At("HEAD"), method = "Lnet/minecraft/client/model/ModelBiped;func_78087_a(FFFFFFLnet/minecraft/entity/Entity;)V")
   public void depthsupdate$setRotationAngles$HEAD(
      float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity, CallbackInfo ci
   ) {
      if (entity instanceof EntityPlayer player) {
         if (player.func_184607_cu().func_77973_b() instanceof ItemSpyglass) {
            this.field_187075_l = ArmPose.EMPTY;
            this.field_187076_m = ArmPose.EMPTY;
         }
      }
   }

   @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/model/ModelBiped;func_78087_a(FFFFFFLnet/minecraft/entity/Entity;)V")
   public void depthsupdate$setRotationAngles$TAIL(
      float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity, CallbackInfo ci
   ) {
      if (entity instanceof EntityPlayer player) {
         if (player.func_184607_cu().func_77973_b() instanceof ItemSpyglass) {
            boolean isRight = player.func_184600_cs() == EnumHand.MAIN_HAND ^ player.func_184591_cq() == EnumHandSide.LEFT;
            ModelRenderer arm = isRight ? this.field_178723_h : this.field_178724_i;
            arm.field_78795_f = MathHelper.func_76131_a(
               this.field_78116_c.field_78795_f - 1.9198622F - (player.func_70093_af() ? (float) (Math.PI / 12) : 0.0F), -2.4F, 3.3F
            );
            arm.field_78796_g = this.field_78116_c.field_78796_g + (isRight ? (float) (-Math.PI / 12) : (float) (Math.PI / 12));
         }
      }
   }
}
