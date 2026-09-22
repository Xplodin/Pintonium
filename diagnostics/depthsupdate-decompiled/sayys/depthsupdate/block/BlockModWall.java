package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.block.BlockWall.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockModWall extends BlockWall {
   public BlockModWall(String name, Block modelBlock) {
      super(modelBlock);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149647_a(CreativeTabs.field_78030_b);
      this.func_180632_j(
         this.field_176227_L
            .func_177621_b()
            .func_177226_a(BlockWall.field_176256_a, false)
            .func_177226_a(BlockWall.field_176254_b, false)
            .func_177226_a(BlockWall.field_176257_M, false)
            .func_177226_a(BlockWall.field_176258_N, false)
            .func_177226_a(BlockWall.field_176259_O, false)
            .func_177226_a(BlockWall.field_176255_P, EnumType.NORMAL)
      );
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(
         this,
         new IProperty[]{
            BlockWall.field_176255_P,
            BlockWall.field_176256_a,
            BlockWall.field_176254_b,
            BlockWall.field_176257_M,
            BlockWall.field_176258_N,
            BlockWall.field_176259_O
         }
      );
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P();
   }

   public int func_176201_c(IBlockState state) {
      return 0;
   }

   public void func_149666_a(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      items.add(new ItemStack(this));
   }
}
