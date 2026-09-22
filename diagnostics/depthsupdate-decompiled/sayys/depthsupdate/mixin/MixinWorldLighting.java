package sayys.depthsupdate.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightManager;

@Mixin(World.class)
public abstract class MixinWorldLighting {
   @Shadow
   public abstract boolean func_175701_a(BlockPos var1);

   @Shadow
   public abstract boolean func_175667_e(BlockPos var1);

   @Shadow
   public abstract int func_175642_b(EnumSkyBlock var1, BlockPos var2);

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/World;func_175705_a(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"
   )
   private void depthsupdate$getLightFromNeighborsFor(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      World world = (World)this;
      if (HeightManager.isExtended(world)) {
         int y = pos.func_177956_o();
         if (y >= HeightManager.getMinY(world) && y < 0 || y >= 256 && y < HeightManager.getMaxY(world)) {
            if (!world.field_73011_w.func_191066_m() && type == EnumSkyBlock.SKY) {
               cir.setReturnValue(0);
            } else if (!this.func_175701_a(pos)) {
               cir.setReturnValue(type.field_77198_c);
            } else if (!this.func_175667_e(pos)) {
               cir.setReturnValue(type.field_77198_c);
            } else if (world.func_180495_p(pos).func_185916_f()) {
               int i1 = this.func_175642_b(type, pos.func_177984_a());
               int i = this.func_175642_b(type, pos.func_177974_f());
               int j = this.func_175642_b(type, pos.func_177976_e());
               int k = this.func_175642_b(type, pos.func_177968_d());
               int l = this.func_175642_b(type, pos.func_177978_c());
               if (i > i1) {
                  i1 = i;
               }

               if (j > i1) {
                  i1 = j;
               }

               if (k > i1) {
                  i1 = k;
               }

               if (l > i1) {
                  i1 = l;
               }

               cir.setReturnValue(i1);
            } else {
               cir.setReturnValue(world.func_175726_f(pos).func_177413_a(type, pos));
            }
         }
      }
   }
}
