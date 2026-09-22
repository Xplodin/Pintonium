package sayys.depthsupdate.mixin;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import sayys.depthsupdate.core.HeightManager;

@Mixin(WorldGenMinable.class)
public abstract class MixinWorldGenMinable {
   @ModifyVariable(
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 0,
      method = "Lnet/minecraft/world/gen/feature/WorldGenMinable;func_180709_b(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z"
   )
   private @NonNull BlockPos depthsupdate$redistributeOreIntoDepths(@NonNull BlockPos pos, World worldIn, Random rand) {
      if (!HeightManager.isExtended(worldIn)) {
         return pos;
      } else {
         int minY = HeightManager.getMinY(worldIn);
         int y = pos.func_177956_o();
         if (minY < 0 && y >= 0 && y <= 64 && rand.nextFloat() < 0.5F) {
            int newY = minY + rand.nextInt(y - minY + 1);
            return new BlockPos(pos.func_177958_n(), newY, pos.func_177952_p());
         } else {
            return pos;
         }
      }
   }
}
