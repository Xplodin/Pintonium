package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMossCarpet extends Block {
   protected static final AxisAlignedBB CARPET_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.0625, 1.0);

   public BlockMossCarpet() {
      super(Material.field_151585_k);
      this.setRegistryName("depthsupdate", "moss_carpet");
      this.func_149663_c("moss_carpet");
      this.func_149711_c(0.1F);
      this.func_149752_b(0.1F);
      this.func_149672_a(SoundType.field_185850_c);
      this.func_149647_a(CreativeTabs.field_78031_c);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return CARPET_AABB;
   }

   public boolean func_149662_c(IBlockState state) {
      return false;
   }

   public boolean func_149686_d(IBlockState state) {
      return false;
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      return super.func_176196_c(worldIn, pos) && worldIn.func_180495_p(pos.func_177977_b()).isSideSolid(worldIn, pos.func_177977_b(), EnumFacing.UP);
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!worldIn.func_180495_p(pos.func_177977_b()).isSideSolid(worldIn, pos.func_177977_b(), EnumFacing.UP)) {
         this.func_176226_b(worldIn, pos, state, 0);
         worldIn.func_175698_g(pos);
      }
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer func_180664_k() {
      return BlockRenderLayer.CUTOUT;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_176225_a(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      return true;
   }
}
