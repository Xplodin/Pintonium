package sayys.depthsupdate.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import sayys.depthsupdate.item.ItemSpyglass;

@EventBusSubscriber(value = Side.CLIENT, modid = "depthsupdate")
public class SpyglassClientHandler {
   private static final ResourceLocation SCOPE_LOCATION = new ResourceLocation("depthsupdate", "textures/misc/spyglass_scope.png");
   private static float fovModifier = 1.0F;
   private static float prevFovModifier = 1.0F;

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent event) {
      if (event.phase == Phase.START) {
         prevFovModifier = fovModifier;
         Minecraft mc = Minecraft.func_71410_x();
         EntityPlayer player = mc.field_71439_g;
         float target = 1.0F;
         if (player != null && player.func_184587_cr() && player.func_184607_cu().func_77973_b() instanceof ItemSpyglass) {
            target = 0.1F;
         }

         fovModifier = fovModifier + (target - fovModifier) * 0.5F;
      }
   }

   @SubscribeEvent
   public static void onFOVUpdate(FOVModifier event) {
      float lerpedModifier = prevFovModifier + (fovModifier - prevFovModifier) * (float)event.getRenderPartialTicks();
      event.setFOV(event.getFOV() * lerpedModifier);
   }

   @SubscribeEvent
   public static void onRenderOverlay(Post event) {
      if (event.getType() == ElementType.ALL) {
         Minecraft mc = Minecraft.func_71410_x();
         EntityPlayer player = mc.field_71439_g;
         if (player != null && player.func_184587_cr() && player.func_184607_cu().func_77973_b() instanceof ItemSpyglass && mc.field_71474_y.field_74320_O == 0
            )
          {
            renderSpyglassScope(event.getResolution().func_78326_a(), event.getResolution().func_78328_b());
         }
      }
   }

   private static void renderSpyglassScope(int width, int height) {
      GlStateManager.func_179097_i();
      GlStateManager.func_179132_a(false);
      GlStateManager.func_179147_l();
      GlStateManager.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      Minecraft.func_71410_x().func_110434_K().func_110577_a(SCOPE_LOCATION);
      Tessellator tessellator = Tessellator.func_178181_a();
      BufferBuilder bufferbuilder = tessellator.func_178180_c();
      int f = Math.min(width, height);
      int x = (width - f) / 2;
      int y = (height - f) / 2;
      bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181707_g);
      bufferbuilder.func_181662_b(x, y + f, -90.0).func_187315_a(0.0, 1.0).func_181675_d();
      bufferbuilder.func_181662_b(x + f, y + f, -90.0).func_187315_a(1.0, 1.0).func_181675_d();
      bufferbuilder.func_181662_b(x + f, y, -90.0).func_187315_a(1.0, 0.0).func_181675_d();
      bufferbuilder.func_181662_b(x, y, -90.0).func_187315_a(0.0, 0.0).func_181675_d();
      tessellator.func_78381_a();
      GlStateManager.func_179090_x();
      GlStateManager.func_179131_c(0.0F, 0.0F, 0.0F, 1.0F);
      if (x > 0) {
         drawRect(0, 0, x, height);
         drawRect(x + f, 0, width, height);
      }

      if (y > 0) {
         drawRect(0, 0, width, y);
         drawRect(0, y + f, width, height);
      }

      GlStateManager.func_179098_w();
      GlStateManager.func_179132_a(true);
      GlStateManager.func_179126_j();
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private static void drawRect(int left, int top, int right, int bottom) {
      Tessellator tessellator = Tessellator.func_178181_a();
      BufferBuilder bufferbuilder = tessellator.func_178180_c();
      bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181705_e);
      bufferbuilder.func_181662_b(left, bottom, -90.0).func_181675_d();
      bufferbuilder.func_181662_b(right, bottom, -90.0).func_181675_d();
      bufferbuilder.func_181662_b(right, top, -90.0).func_181675_d();
      bufferbuilder.func_181662_b(left, top, -90.0).func_181675_d();
      tessellator.func_78381_a();
   }
}
