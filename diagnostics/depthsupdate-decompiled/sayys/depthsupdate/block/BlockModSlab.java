package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockModSlab extends BlockSlab {
   public static final PropertyEnum<BlockModSlab.Variant> VARIANT = PropertyEnum.func_177709_a("variant", BlockModSlab.Variant.class);

   public BlockModSlab(String name, Material material) {
      super(material);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149647_a(CreativeTabs.field_78030_b);
      IBlockState iblockstate = this.field_176227_L.func_177621_b();
      if (!this.func_176552_j()) {
         iblockstate = iblockstate.func_177226_a(field_176554_a, EnumBlockHalf.BOTTOM);
      }

      this.func_180632_j(iblockstate.func_177226_a(VARIANT, BlockModSlab.Variant.COBBLED));
      this.field_149783_u = true;
   }

   public String func_150002_b(int meta) {
      return super.func_149739_a() + "." + BlockModSlab.Variant.byMetadata(meta & 7).func_176610_l();
   }

   public IProperty<?> func_176551_l() {
      return VARIANT;
   }

   public Comparable<?> func_185674_a(ItemStack stack) {
      return BlockModSlab.Variant.byMetadata(stack.func_77960_j() & 7);
   }

   public IBlockState func_176203_a(int meta) {
      IBlockState iblockstate = this.func_176223_P().func_177226_a(VARIANT, BlockModSlab.Variant.byMetadata(meta & 7));
      if (!this.func_176552_j()) {
         iblockstate = iblockstate.func_177226_a(field_176554_a, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
      }

      return iblockstate;
   }

   public int func_176201_c(IBlockState state) {
      int i = ((BlockModSlab.Variant)state.func_177229_b(VARIANT)).getMetadata();
      if (!this.func_176552_j() && state.func_177229_b(field_176554_a) == EnumBlockHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   public int func_180651_a(IBlockState state) {
      return ((BlockModSlab.Variant)state.func_177229_b(VARIANT)).getMetadata();
   }

   public void func_149666_a(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockModSlab.Variant variant : BlockModSlab.Variant.values()) {
         items.add(new ItemStack(this, 1, variant.getMetadata()));
      }
   }

   protected BlockStateContainer func_180661_e() {
      return this.func_176552_j()
         ? new BlockStateContainer(this, new IProperty[]{VARIANT})
         : new BlockStateContainer(this, new IProperty[]{field_176554_a, VARIANT});
   }

   public static class Double extends BlockModSlab {
      private Block halfSlab;

      public Double(String name, Material material) {
         super(name, material);
      }

      public boolean func_176552_j() {
         return true;
      }

      public Item func_180660_a(IBlockState state, Random rand, int fortune) {
         return this.halfSlab == null ? Items.field_190931_a : Item.func_150898_a(this.halfSlab);
      }

      public ItemStack func_185473_a(World world, BlockPos pos, IBlockState state) {
         return this.halfSlab == null
            ? ItemStack.field_190927_a
            : new ItemStack(this.halfSlab, 1, ((BlockModSlab.Variant)state.func_177229_b(VARIANT)).getMetadata());
      }
   }

   public static class Half extends BlockModSlab {
      public Half(String name, Material material, BlockModSlab.Double doubleSlab) {
         super(name, material);
         doubleSlab.halfSlab = this;
      }

      public boolean func_176552_j() {
         return false;
      }
   }

   public static enum Variant implements IStringSerializable {
      COBBLED(0, "cobbled"),
      POLISHED(1, "polished"),
      BRICKS(2, "bricks"),
      TILES(3, "tiles");

      private final int meta;
      private final String name;

      private Variant(int meta, String name) {
         this.meta = meta;
         this.name = name;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String func_176610_l() {
         return this.name;
      }

      public static BlockModSlab.Variant byMetadata(int meta) {
         if (meta < 0 || meta >= values().length) {
            meta = 0;
         }

         return values()[meta];
      }
   }
}
