package sayys.depthsupdate.mixin.mod.optifine;

import java.lang.reflect.Method;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import sayys.depthsupdate.compat.optifine.OptiFineCompatLog;
import sayys.depthsupdate.core.HeightManager;

@Mixin(remap = false, targets = "net.optifine.util.RenderChunkUtils")
public class MixinOptiFineRenderChunkUtils {
   @Unique
   private static Method getChunkMethod;
   @Unique
   private static Method getBlockRefCountMethod;
   @Unique
   private static boolean reflectionInitialized = false;

   @Unique
   private static void initReflection() {
      if (!reflectionInitialized) {
         reflectionInitialized = true;

         try {
            getChunkMethod = RenderChunk.class.getDeclaredMethod("getChunk");
            getChunkMethod.setAccessible(true);
            getBlockRefCountMethod = ExtendedBlockStorage.class.getDeclaredMethod("getBlockRefCount");
            getBlockRefCountMethod.setAccessible(true);
         } catch (Exception var1) {
            OptiFineCompatLog.once("RenderChunkUtils reflection setup", var1);
         }
      }
   }

   @Overwrite
   public static int getCountBlocks(RenderChunk renderChunk) {
      initReflection();

      try {
         Chunk chunk = (Chunk)getChunkMethod.invoke(renderChunk);
         if (chunk == null) {
            return 0;
         }

         ExtendedBlockStorage[] storages = chunk.func_76587_i();
         if (storages == null) {
            return 0;
         }

         int y = renderChunk.func_178568_j().func_177956_o();
         int index = HeightManager.getMaxContext().toStorageIndex(y);
         if (index >= 0 && index < storages.length) {
            ExtendedBlockStorage ebs = storages[index];
            if (ebs != null) {
               return (Integer)getBlockRefCountMethod.invoke(ebs);
            }
         }
      } catch (Exception var6) {
         OptiFineCompatLog.once("RenderChunkUtils block count", var6);
      }

      return 0;
   }
}
