package sayys.depthsupdate.mixin;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.ChunkPrimer;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.BedrockFilter;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = ChunkPrimer.class, priority = 2000)
public abstract class MixinChunkPrimer {
   private static final IBlockState DEPTHSUPDATE_DEFAULT_STATE = Blocks.field_150350_a.func_176223_P();

   @ModifyConstant(constant = @Constant(intValue = 65536), method = "<init>")
   private int depthsupdate$expandDataArrays(int original) {
      return HeightManager.getMaxContext().primerArraySize();
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/ChunkPrimer;func_177855_a(IIILnet/minecraft/block/state/IBlockState;)V")
   private void depthsupdate$filterVanillaBedrock(int x, int y, int z, @NonNull IBlockState state, CallbackInfo ci) {
      if (y >= 0 && y <= 4 && state.func_177230_c() == Blocks.field_150357_h && BedrockFilter.active()) {
         ci.cancel();
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/ChunkPrimer;func_186137_b(III)I")
   private static void depthsupdate$getBlockIndex(int x, int y, int z, @NonNull CallbackInfoReturnable<Integer> cir) {
      HeightContext ctx = HeightManager.getMaxContext();
      int yBitShift = ctx.yBitShift();
      cir.setReturnValue(x << yBitShift + 4 | z << yBitShift | y - ctx.minY());
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/ChunkPrimer;func_186138_a(II)I")
   private void depthsupdate$findGroundBlockIdx(int x, int z, @NonNull CallbackInfoReturnable<Integer> cir) {
      HeightContext ctx = HeightManager.getMaxContext();
      int minY = ctx.minY();
      ChunkPrimer self = (ChunkPrimer)this;

      for (int y = ctx.maxY() - 1; y >= minY; y--) {
         IBlockState state = self.func_177856_a(x, y, z);
         if (state != null && state != DEPTHSUPDATE_DEFAULT_STATE) {
            cir.setReturnValue(y);
            return;
         }
      }

      cir.setReturnValue(minY);
   }
}
