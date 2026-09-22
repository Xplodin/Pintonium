package sayys.depthsupdate.mixin.mod.optifine;

import java.lang.reflect.Field;
import net.minecraft.client.renderer.chunk.RenderChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.compat.optifine.OptiFineCompatLog;

@Mixin(RenderChunk.class)
public class MixinOptiFineRenderChunk {
   @Unique
   private static Field neighboursUpdatedField;
   @Unique
   private static Field offset16UpdatedField;
   @Unique
   private static boolean reflectionInitialized = false;
   @Unique
   private static boolean reflectionFailed = false;

   @Unique
   private static void initReflection() {
      if (!reflectionInitialized) {
         reflectionInitialized = true;

         try {
            neighboursUpdatedField = RenderChunk.class.getDeclaredField("renderChunkNeighboursUpated");
            neighboursUpdatedField.setAccessible(true);
            offset16UpdatedField = RenderChunk.class.getDeclaredField("renderChunksOffset16Updated");
            offset16UpdatedField.setAccessible(true);
         } catch (NoSuchFieldException var1) {
            reflectionFailed = true;
            OptiFineCompatLog.once("RenderChunk neighbour fields", var1);
         }
      }
   }

   @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/renderer/chunk/RenderChunk;func_189562_a(III)V")
   private void depthsupdate$resetNeighbourFlags(int x, int y, int z, CallbackInfo ci) {
      if (!reflectionFailed) {
         initReflection();
         if (!reflectionFailed) {
            try {
               neighboursUpdatedField.setBoolean(this, false);
               offset16UpdatedField.setBoolean(this, false);
            } catch (Exception var6) {
               OptiFineCompatLog.once("RenderChunk neighbour reset", var6);
            }
         }
      }
   }
}
