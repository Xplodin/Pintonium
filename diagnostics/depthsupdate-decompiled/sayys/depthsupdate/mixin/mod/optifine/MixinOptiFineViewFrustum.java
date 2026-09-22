package sayys.depthsupdate.mixin.mod.optifine;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.compat.optifine.OptiFineCompatLog;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.mixin.IMixinViewFrustum;

@Mixin(ViewFrustum.class)
public abstract class MixinOptiFineViewFrustum {
   @Shadow
   public RenderChunk[] field_178164_f;
   @Unique
   private static Field neighboursField;
   @Unique
   private static Field neighboursValidField;
   @Unique
   private static Field offset16Field;
   @Unique
   private static Field neighboursUpdatedField;
   @Unique
   private static Field offset16UpdatedField;
   @Unique
   private static boolean reflectionFailed = false;

   @Unique
   private static void initReflection() {
      if (neighboursField == null && !reflectionFailed) {
         try {
            neighboursField = RenderChunk.class.getDeclaredField("renderChunkNeighbours");
            neighboursField.setAccessible(true);
            neighboursValidField = RenderChunk.class.getDeclaredField("renderChunkNeighboursValid");
            neighboursValidField.setAccessible(true);
            offset16Field = RenderChunk.class.getDeclaredField("renderChunksOfset16");
            offset16Field.setAccessible(true);
            neighboursUpdatedField = RenderChunk.class.getDeclaredField("renderChunkNeighboursUpated");
            neighboursUpdatedField.setAccessible(true);
            offset16UpdatedField = RenderChunk.class.getDeclaredField("renderChunksOffset16Updated");
            offset16UpdatedField.setAccessible(true);
         } catch (NoSuchFieldException var1) {
            reflectionFailed = true;
            OptiFineCompatLog.once("ViewFrustum neighbour fields", var1);
         }
      }
   }

   @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/ViewFrustum;func_178163_a(DD)V")
   private void depthsupdate$fixNeighbourLinks(double viewEntityX, double viewEntityZ, CallbackInfo ci) {
      initReflection();
      if (!reflectionFailed) {
         HeightContext ctx = HeightManager.get(Minecraft.func_71410_x().field_71441_e);
         ViewFrustum self = (ViewFrustum)this;

         for (RenderChunk renderChunk : this.field_178164_f) {
            if (renderChunk != null) {
               try {
                  RenderChunk[] neighbours = (RenderChunk[])neighboursField.get(renderChunk);
                  RenderChunk[] neighboursValid = (RenderChunk[])neighboursValidField.get(renderChunk);
                  RenderChunk[] offset16 = (RenderChunk[])offset16Field.get(renderChunk);

                  for (EnumFacing facing : EnumFacing.field_82609_l) {
                     BlockPos neighbourPos = renderChunk.func_178568_j().func_177967_a(facing, 16);
                     int y = neighbourPos.func_177956_o();
                     RenderChunk neighbour = null;
                     if (y >= ctx.minY() && y < ctx.maxY()) {
                        neighbour = ((IMixinViewFrustum)self).invokeGetRenderChunk(neighbourPos);
                     }

                     int idx = facing.func_176745_a();
                     if (neighbours != null) {
                        neighbours[idx] = neighbour;
                     }

                     if (neighboursValid != null) {
                        neighboursValid[idx] = neighbour;
                     }

                     if (offset16 != null) {
                        offset16[idx] = neighbour;
                     }
                  }

                  neighboursUpdatedField.setBoolean(renderChunk, true);
                  offset16UpdatedField.setBoolean(renderChunk, true);
               } catch (Exception var23) {
                  OptiFineCompatLog.once("ViewFrustum neighbour linking", var23);
               }
            }
         }
      }
   }
}
