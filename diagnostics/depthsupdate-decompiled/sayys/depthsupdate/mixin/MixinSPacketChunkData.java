package sayys.depthsupdate.mixin;

import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(SPacketChunkData.class)
public class MixinSPacketChunkData {
   @Unique
   private static final ThreadLocal<HeightContext> depthsupdate$ctx = ThreadLocal.withInitial(() -> HeightContext.VANILLA);

   @Inject(at = @At("HEAD"), method = "Lnet/minecraft/network/play/server/SPacketChunkData;<init>(Lnet/minecraft/world/chunk/Chunk;I)V")
   private static void depthsupdate$captureChunk(Chunk chunkIn, int changedSectionFilter, CallbackInfo ci) {
      depthsupdate$ctx.set(HeightManager.get(chunkIn.func_177412_p()));
   }

   @ModifyConstant(
      constant = @Constant(intValue = 65535),
      method = "Lnet/minecraft/network/play/server/SPacketChunkData;<init>(Lnet/minecraft/world/chunk/Chunk;I)V"
   )
   private int depthsupdate$modifyFullChunkCheck(int original) {
      HeightContext ctx = depthsupdate$ctx.get();
      return ctx.isExtended() ? ctx.fullChunkSectionMask() : original;
   }

   @Inject(at = @At("RETURN"), method = "Lnet/minecraft/network/play/server/SPacketChunkData;<init>(Lnet/minecraft/world/chunk/Chunk;I)V")
   private void depthsupdate$cleanup(Chunk chunkIn, int changedSectionFilter, CallbackInfo ci) {
      depthsupdate$ctx.remove();
   }
}
