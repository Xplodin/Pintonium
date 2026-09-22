package sayys.depthsupdate.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.item.ItemSpyglass;

@Mixin(LayerHeldItem.class)
public class MixinLayerHeldItem {
   @Shadow
   @Final
   protected RenderLivingBase<?> field_177206_a;

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/client/renderer/entity/layers/LayerHeldItem;func_188358_a(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;Lnet/minecraft/util/EnumHandSide;)V"
   )
   public void depthsupdate$renderHeldItem$HEAD(EntityLivingBase entity, ItemStack stack, TransformType transforms, EnumHandSide hand, CallbackInfo callback) {
      if (entity instanceof EntityPlayer player) {
         if (player.func_184607_cu() == stack && stack.func_77973_b() instanceof ItemSpyglass) {
            if (this.field_177206_a.func_177087_b() instanceof ModelBiped) {
               GlStateManager.func_179094_E();
               ModelRenderer head = ((ModelBiped)this.field_177206_a.func_177087_b()).field_78116_c;
               float rotX = head.field_78795_f;
               head.field_78795_f = MathHelper.func_76131_a(head.field_78795_f, (float) (-Math.PI / 6), (float) (Math.PI / 2));
               head.func_78794_c(0.0625F);
               head.field_78795_f = rotX;
               GlStateManager.func_179109_b(0.0F, -0.25F, 0.0F);
               GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
               GlStateManager.func_179152_a(0.625F, -0.625F, -0.625F);
               GlStateManager.func_179109_b((hand == EnumHandSide.LEFT ? -0.15625F : 0.15625F) - 0.234375F, -0.015625F, -0.3125F);
               Minecraft.func_71410_x().func_175597_ag().func_178099_a(player, stack, TransformType.HEAD);
               GlStateManager.func_179121_F();
               callback.cancel();
            }
         }
      }
   }
}
