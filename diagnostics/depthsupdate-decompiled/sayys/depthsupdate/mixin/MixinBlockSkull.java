package sayys.depthsupdate.mixin;

import net.minecraft.block.BlockSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sayys.depthsupdate.core.HeightManager;

@Mixin(BlockSkull.class)
public abstract class MixinBlockSkull {
   @Redirect(
      at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/math/BlockPos;func_177956_o()I"),
      method = "Lnet/minecraft/block/BlockSkull;func_180679_a(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/tileentity/TileEntitySkull;)V"
   )
   private int depthsupdate$witherSpawnY(BlockPos instance, World worldIn, BlockPos pos, TileEntitySkull te) {
      return instance.func_177956_o() - HeightManager.getMinY(worldIn);
   }

   @Redirect(
      at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/math/BlockPos;func_177956_o()I"),
      method = "Lnet/minecraft/block/BlockSkull;func_176415_b(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)Z"
   )
   private int depthsupdate$dispenserPlaceY(BlockPos instance, World worldIn, BlockPos pos, ItemStack stack) {
      return instance.func_177956_o() - HeightManager.getMinY(worldIn);
   }
}
