package sayys.depthsupdate.mixin;

import net.minecraft.block.BlockFalling;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant.Condition;
import sayys.depthsupdate.core.HeightManager;

@Mixin(BlockFalling.class)
public abstract class MixinBlockFalling {
   @ModifyConstant(
      constant = @Constant(intValue = 0, expandZeroConditions = Condition.GREATER_THAN_OR_EQUAL_TO_ZERO),
      method = "Lnet/minecraft/block/BlockFalling;func_176503_e(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
   )
   private int depthsupdate$fallFloor(int original, World worldIn, BlockPos pos) {
      return HeightManager.getMinY(worldIn);
   }

   @ModifyConstant(
      constant = @Constant(intValue = 0, expandZeroConditions = Condition.GREATER_THAN_ZERO),
      method = "Lnet/minecraft/block/BlockFalling;func_176503_e(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"
   )
   private int depthsupdate$teleportFloor(int original, World worldIn, BlockPos pos) {
      return HeightManager.getMinY(worldIn);
   }
}
