package sayys.depthsupdate.mixin.mod.nothirium;

import meldexun.nothirium.api.renderer.chunk.IRenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import sayys.depthsupdate.core.HeightManager;

@Mixin(remap = false, targets = "meldexun.nothirium.util.Direction$2")
public class MixinNothiriumDirectionUp {
   @Overwrite
   public boolean isFaceCulled(IRenderChunk renderChunk, double cameraX, double cameraY, double cameraZ) {
      return renderChunk.getSectionY() < HeightManager.getMaxContext().minSection() ? true : cameraY < renderChunk.getY() + 16;
   }
}
