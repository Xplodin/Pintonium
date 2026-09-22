package sayys.depthsupdate.block;

import com.google.common.base.Predicate;
import java.util.Random;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockDeepslate extends BlockRotatedPillar {
   public BlockDeepslate() {
      super(Material.field_151576_e);
      this.setRegistryName("depthsupdate", "deepslate");
      this.func_149663_c("deepslate");
      this.func_149711_c(3.0F);
      this.func_149752_b(6.0F);
      this.func_149672_a(SoundType.field_185851_d);
      this.func_149647_a(CreativeTabs.field_78030_b);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(field_176298_M, Axis.Y));
   }

   public MapColor func_180659_g(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.field_151665_m;
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return Item.func_150898_a(DeepslateRegistry.cobbled_deepslate);
   }

   public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
      return true;
   }

   public boolean isReplaceableOreGen(IBlockState state, IBlockAccess world, BlockPos pos, Predicate<IBlockState> target) {
      return target != null && target.apply(Blocks.field_150348_b.func_176223_P()) ? true : super.isReplaceableOreGen(state, world, pos, target);
   }

   public IBlockState func_176203_a(int meta) {
      Axis axis = Axis.Y;
      int i = meta & 12;
      if (i == 4) {
         axis = Axis.X;
      } else if (i == 8) {
         axis = Axis.Z;
      }

      return this.func_176223_P().func_177226_a(field_176298_M, axis);
   }

   public int func_176201_c(IBlockState state) {
      int i = 0;
      Axis axis = (Axis)state.func_177229_b(field_176298_M);
      if (axis == Axis.X) {
         i |= 4;
      } else if (axis == Axis.Z) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{field_176298_M});
   }
}
