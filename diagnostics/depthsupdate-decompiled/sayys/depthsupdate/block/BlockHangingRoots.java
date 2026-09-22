package sayys.depthsupdate.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockHangingRoots extends Block implements IShearable {
   protected static final AxisAlignedBB HANGING_AABB = new AxisAlignedBB(0.125, 0.4, 0.125, 0.875, 1.0, 0.875);

   public BlockHangingRoots() {
      super(Material.field_151585_k, MapColor.field_151664_l);
      this.func_149711_c(0.1F);
      this.func_149672_a(SoundType.field_185850_c);
      this.setRegistryName("depthsupdate", "hanging_roots");
      this.func_149663_c("hanging_roots");
      this.func_149647_a(CreativeTabs.field_78031_c);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return HANGING_AABB;
   }

   @Nullable
   public AxisAlignedBB func_180646_a(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
      return field_185506_k;
   }

   public boolean func_149662_c(IBlockState state) {
      return false;
   }

   public boolean func_149686_d(IBlockState state) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer func_180664_k() {
      return BlockRenderLayer.CUTOUT;
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      IBlockState stateAbove = worldIn.func_180495_p(pos.func_177984_a());
      return stateAbove.isSideSolid(worldIn, pos.func_177984_a(), EnumFacing.DOWN)
         || stateAbove.func_177230_c() instanceof BlockAzalea
         || stateAbove.func_177230_c() instanceof BlockModLeaves;
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.func_176196_c(worldIn, pos)) {
         this.func_176226_b(worldIn, pos, state, 0);
         worldIn.func_175698_g(pos);
      }
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return Items.field_190931_a;
   }

   public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
      return true;
   }

   public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      return Collections.singletonList(new ItemStack(this));
   }
}
