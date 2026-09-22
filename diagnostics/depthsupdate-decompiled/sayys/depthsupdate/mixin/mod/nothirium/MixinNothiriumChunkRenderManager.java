package sayys.depthsupdate.mixin.mod.nothirium;

import meldexun.nothirium.api.renderer.chunk.IChunkRenderer;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkDispatcher;
import meldexun.nothirium.api.renderer.chunk.IRenderChunkProvider;
import meldexun.nothirium.mc.renderer.ChunkRenderManager;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkDispatcher;
import meldexun.nothirium.mc.renderer.chunk.RenderChunkProvider;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = ChunkRenderManager.class, remap = false)
public class MixinNothiriumChunkRenderManager {
   @Shadow
   private static IChunkRenderer<?> chunkRenderer;
   @Shadow
   private static IRenderChunkProvider<?> renderChunkProvider;
   @Shadow
   private static IRenderChunkDispatcher taskDispatcher;

   @Shadow
   private static IChunkRenderer<?> createChunkRenderer(IChunkRenderer<?> oldChunkRenderer) {
      throw new AssertionError();
   }

   @Overwrite
   public static void allChanged() {
      chunkRenderer = createChunkRenderer(chunkRenderer);
      if (renderChunkProvider != null) {
         renderChunkProvider.releaseBuffers();
      } else {
         renderChunkProvider = new RenderChunkProvider();
      }

      if (taskDispatcher == null) {
         taskDispatcher = new RenderChunkDispatcher();
      }

      Minecraft mc = Minecraft.func_71410_x();
      int renderDistance = mc.field_71474_y.field_151451_c;
      HeightContext ctx = mc.field_71441_e != null ? HeightManager.get(mc.field_71441_e) : HeightContext.VANILLA;
      int maxSections = ctx.totalStorageSections();
      int renderDistanceY = Math.min(renderDistance, (maxSections + 1) / 2);
      renderChunkProvider.init(renderDistance, renderDistanceY, renderDistance);
      chunkRenderer.init(renderDistance);
   }
}
