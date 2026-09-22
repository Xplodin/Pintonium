package sayys.depthsupdate.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.init.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.util.BlockUtils;

@Mixin(BlockMatcher.class)
public class MixinBlockMatcher {
   @Shadow
   @Final
   private Block field_177644_a;

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/block/state/pattern/BlockMatcher;apply(Lnet/minecraft/block/state/IBlockState;)Z")
   private void depthsupdate$matchDeepslate(IBlockState state, CallbackInfoReturnable<Boolean> cir) {
      if (this.field_177644_a == Blocks.field_150348_b && state != null) {
         IBlockState deepslate = BlockUtils.getDeepslateBlockState();
         if (state == deepslate || state.func_177230_c() == deepslate.func_177230_c()) {
            cir.setReturnValue(true);
         }
      }
   }
}
