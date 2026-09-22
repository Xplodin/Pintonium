package sayys.depthsupdate.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sayys.depthsupdate.registry.PlantRegistry;

public class ItemGlowBerries extends ItemFood {
   public ItemGlowBerries() {
      super(2, 0.4F, false);
      this.setRegistryName("depthsupdate", "glow_berries");
      this.func_77655_b("glow_berries");
      this.func_77637_a(CreativeTabs.field_78039_h);
   }

   public EnumActionResult func_180614_a(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack itemstack = player.func_184586_b(hand);
      if (facing != EnumFacing.DOWN) {
         return EnumActionResult.PASS;
      } else {
         BlockPos placePos = pos.func_177977_b();
         if (!itemstack.func_190926_b() && player.func_175151_a(placePos, facing, itemstack)) {
            IBlockState iblockstate = worldIn.func_180495_p(pos);
            Block block = iblockstate.func_177230_c();
            IBlockState placeState = PlantRegistry.cave_vines.func_176223_P();
            if (worldIn.func_175623_d(placePos) && PlantRegistry.cave_vines.func_176196_c(worldIn, placePos)) {
               worldIn.func_180501_a(placePos, placeState, 11);
               worldIn.func_184133_a(
                  player,
                  placePos,
                  PlantRegistry.cave_vines.func_185467_w().func_185841_e(),
                  SoundCategory.BLOCKS,
                  (PlantRegistry.cave_vines.func_185467_w().func_185843_a() + 1.0F) / 2.0F,
                  PlantRegistry.cave_vines.func_185467_w().func_185847_b() * 0.8F
               );
               itemstack.func_190918_g(1);
               return EnumActionResult.SUCCESS;
            } else {
               return EnumActionResult.PASS;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      }
   }
}
