package sayys.depthsupdate.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.debug.DebugRendererChunkBorder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(DebugRendererChunkBorder.class)
public abstract class MixinDebugRendererChunkBorder {
   @Shadow
   @Final
   private Minecraft field_190072_a;

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/client/renderer/debug/DebugRendererChunkBorder;func_190060_a(FJ)V")
   private void depthsupdate$renderWithExtendedBounds(float partialTicks, long finishTimeNano, CallbackInfo ci) {
      if (HeightManager.isExtended(this.field_190072_a.field_71441_e)) {
         ci.cancel();
         HeightContext ctx = HeightManager.get(this.field_190072_a.field_71441_e);
         int worldMinY = ctx.minY();
         int worldMaxY = ctx.maxY();
         EntityPlayer entityplayer = this.field_190072_a.field_71439_g;
         Tessellator tessellator = Tessellator.func_178181_a();
         BufferBuilder bufferbuilder = tessellator.func_178180_c();
         double d0 = entityplayer.field_70142_S + (entityplayer.field_70165_t - entityplayer.field_70142_S) * partialTicks;
         double d1 = entityplayer.field_70137_T + (entityplayer.field_70163_u - entityplayer.field_70137_T) * partialTicks;
         double d2 = entityplayer.field_70136_U + (entityplayer.field_70161_v - entityplayer.field_70136_U) * partialTicks;
         double d3 = worldMinY - d1;
         double d4 = worldMaxY - d1;
         GlStateManager.func_179090_x();
         GlStateManager.func_179084_k();
         double d5 = (entityplayer.field_70176_ah << 4) - d0;
         double d6 = (entityplayer.field_70164_aj << 4) - d2;
         GlStateManager.func_187441_d(1.0F);
         bufferbuilder.func_181668_a(3, DefaultVertexFormats.field_181706_f);

         for (int i = -16; i <= 32; i += 16) {
            for (int j = -16; j <= 32; j += 16) {
               bufferbuilder.func_181662_b(d5 + i, d3, d6 + j).func_181666_a(1.0F, 0.0F, 0.0F, 0.0F).func_181675_d();
               bufferbuilder.func_181662_b(d5 + i, d3, d6 + j).func_181666_a(1.0F, 0.0F, 0.0F, 0.5F).func_181675_d();
               bufferbuilder.func_181662_b(d5 + i, d4, d6 + j).func_181666_a(1.0F, 0.0F, 0.0F, 0.5F).func_181675_d();
               bufferbuilder.func_181662_b(d5 + i, d4, d6 + j).func_181666_a(1.0F, 0.0F, 0.0F, 0.0F).func_181675_d();
            }
         }

         for (int k = 2; k < 16; k += 2) {
            bufferbuilder.func_181662_b(d5 + k, d3, d6).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d3, d6).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d4, d6).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d4, d6).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d3, d6 + 16.0).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d3, d6 + 16.0).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d4, d6 + 16.0).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + k, d4, d6 + 16.0).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
         }

         for (int l = 2; l < 16; l += 2) {
            bufferbuilder.func_181662_b(d5, d3, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d3, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d4, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d4, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d3, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d3, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d4, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d4, d6 + l).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
         }

         for (int i1 = worldMinY; i1 <= worldMaxY; i1 += 2) {
            double d7 = i1 - d1;
            bufferbuilder.func_181662_b(d5, d7, d6).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d7, d6).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d7, d6 + 16.0).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d7, d6 + 16.0).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d7, d6).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d7, d6).func_181666_a(1.0F, 1.0F, 0.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d7, d6).func_181666_a(1.0F, 1.0F, 0.0F, 0.0F).func_181675_d();
         }

         tessellator.func_78381_a();
         GlStateManager.func_187441_d(2.0F);
         bufferbuilder.func_181668_a(3, DefaultVertexFormats.field_181706_f);

         for (int j1 = 0; j1 <= 16; j1 += 16) {
            for (int l1 = 0; l1 <= 16; l1 += 16) {
               bufferbuilder.func_181662_b(d5 + j1, d3, d6 + l1).func_181666_a(0.25F, 0.25F, 1.0F, 0.0F).func_181675_d();
               bufferbuilder.func_181662_b(d5 + j1, d3, d6 + l1).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
               bufferbuilder.func_181662_b(d5 + j1, d4, d6 + l1).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
               bufferbuilder.func_181662_b(d5 + j1, d4, d6 + l1).func_181666_a(0.25F, 0.25F, 1.0F, 0.0F).func_181675_d();
            }
         }

         for (int k1 = worldMinY; k1 <= worldMaxY; k1 += 16) {
            double d8 = k1 - d1;
            bufferbuilder.func_181662_b(d5, d8, d6).func_181666_a(0.25F, 0.25F, 1.0F, 0.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d8, d6).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d8, d6 + 16.0).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d8, d6 + 16.0).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5 + 16.0, d8, d6).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d8, d6).func_181666_a(0.25F, 0.25F, 1.0F, 1.0F).func_181675_d();
            bufferbuilder.func_181662_b(d5, d8, d6).func_181666_a(0.25F, 0.25F, 1.0F, 0.0F).func_181675_d();
         }

         tessellator.func_78381_a();
         GlStateManager.func_187441_d(1.0F);
         GlStateManager.func_179147_l();
         GlStateManager.func_179098_w();
      }
   }
}
