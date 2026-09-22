package sayys.depthsupdate.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockModLeaves extends BlockLeaves {
   public BlockModLeaves(String name) {
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149711_c(0.2F);
      this.func_149752_b(0.2F);
      this.func_149647_a(CreativeTabs.field_78031_c);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(field_176236_b, true).func_177226_a(field_176237_a, true));
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{field_176236_b, field_176237_a});
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P().func_177226_a(field_176237_a, (meta & 4) == 0).func_177226_a(field_176236_b, (meta & 8) > 0);
   }

   public int func_176201_c(IBlockState state) {
      int i = 0;
      if (!(Boolean)state.func_177229_b(field_176237_a)) {
         i |= 4;
      }

      if ((Boolean)state.func_177229_b(field_176236_b)) {
         i |= 8;
      }

      return i;
   }

   public EnumType func_176233_b(int meta) {
      return EnumType.OAK;
   }

   public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.func_176223_P().func_177226_a(field_176237_a, Boolean.FALSE).func_177226_a(field_176236_b, Boolean.FALSE);
   }

   public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      return Collections.singletonList(new ItemStack(this));
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return Items.field_190931_a;
   }

   public void func_149666_a(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      items.add(new ItemStack(this));
   }

   public boolean func_149662_c(IBlockState state) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer func_180664_k() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   public boolean func_176225_a(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      return true;
   }

   protected void func_176234_a(World worldIn, BlockPos pos, IBlockState state, int chance) {
   }

   protected int func_176232_d(IBlockState state) {
      return 20;
   }
}
