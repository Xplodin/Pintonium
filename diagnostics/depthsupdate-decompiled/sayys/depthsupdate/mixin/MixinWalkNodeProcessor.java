package sayys.depthsupdate.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.NodeProcessor;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.Constant.Condition;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightManager;

@Mixin(WalkNodeProcessor.class)
public abstract class MixinWalkNodeProcessor extends NodeProcessor {
   @Shadow
   protected EntityLiving currentEntity;

   @Shadow
   protected abstract PathNodeType func_189553_b(IBlockAccess var1, int var2, int var3, int var4);

   @Shadow
   public abstract PathNodeType func_193578_a(IBlockAccess var1, int var2, int var3, int var4, PathNodeType var5);

   @Unique
   private int depthsupdate$minY() {
      EntityLiving pathing = this.currentEntity != null ? this.currentEntity : this.field_186326_b;
      return pathing != null ? HeightManager.getMinY(pathing.field_70170_p) : 0;
   }

   @Unique
   private int depthsupdate$minY(IBlockAccess blockaccessIn) {
      EntityLiving pathing = this.currentEntity != null ? this.currentEntity : this.field_186326_b;
      if (pathing != null) {
         return HeightManager.getMinY(pathing.field_70170_p);
      } else {
         return blockaccessIn instanceof World world ? HeightManager.getMinY(world) : 0;
      }
   }

   @Redirect(
      at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/util/math/BlockPos;func_177956_o()I"),
      method = "Lnet/minecraft/pathfinding/WalkNodeProcessor;func_186318_b()Lnet/minecraft/pathfinding/PathPoint;"
   )
   private int depthsupdate$startGroundY(BlockPos pos) {
      return pos.func_177956_o() - this.depthsupdate$minY();
   }

   @ModifyConstant(
      constant = @Constant(intValue = 0, expandZeroConditions = Condition.GREATER_THAN_ZERO),
      slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;func_184143_b(Lnet/minecraft/util/math/AxisAlignedBB;)Z")),
      method = "Lnet/minecraft/pathfinding/WalkNodeProcessor;func_186332_a(IIIIDLnet/minecraft/util/EnumFacing;)Lnet/minecraft/pathfinding/PathPoint;"
   )
   private int depthsupdate$safePointFloor(int original) {
      return this.depthsupdate$minY();
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/pathfinding/WalkNodeProcessor;func_186330_a(Lnet/minecraft/world/IBlockAccess;III)Lnet/minecraft/pathfinding/PathNodeType;"
   )
   private void depthsupdate$getPathNodeType(IBlockAccess blockaccessIn, int x, int y, int z, CallbackInfoReturnable<PathNodeType> cir) {
      int minY = this.depthsupdate$minY(blockaccessIn);
      PathNodeType pathnodetype = this.func_189553_b(blockaccessIn, x, y, z);
      if (pathnodetype == PathNodeType.OPEN && y > minY) {
         Block block = blockaccessIn.func_180495_p(new BlockPos(x, y - 1, z)).func_177230_c();
         PathNodeType below = this.func_189553_b(blockaccessIn, x, y - 1, z);
         pathnodetype = below != PathNodeType.WALKABLE && below != PathNodeType.OPEN && below != PathNodeType.WATER && below != PathNodeType.LAVA
            ? PathNodeType.WALKABLE
            : PathNodeType.OPEN;
         if (below == PathNodeType.DAMAGE_FIRE || block == Blocks.field_189877_df) {
            pathnodetype = PathNodeType.DAMAGE_FIRE;
         }

         if (below == PathNodeType.DAMAGE_CACTUS) {
            pathnodetype = PathNodeType.DAMAGE_CACTUS;
         }

         if (below == PathNodeType.DAMAGE_OTHER) {
            pathnodetype = PathNodeType.DAMAGE_OTHER;
         }
      }

      cir.setReturnValue(this.func_193578_a(blockaccessIn, x, y, z, pathnodetype));
   }
}
