package sayys.depthsupdate.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import sayys.depthsupdate.registry.PlantRegistry;

public class BlockSmallDripleaf extends BlockBush implements IGrowable, IShearable {
   public static final PropertyDirection FACING = BlockHorizontal.field_185512_D;
   public static final PropertyEnum<BlockSmallDripleaf.EnumBlockHalf> HALF = PropertyEnum.func_177709_a("half", BlockSmallDripleaf.EnumBlockHalf.class);
   protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125, 0.0, 0.09375, 0.875, 0.8125, 0.90625);

   public BlockSmallDripleaf() {
      super(Material.field_151585_k, MapColor.field_151669_i);
      this.func_180632_j(
         this.field_176227_L.func_177621_b().func_177226_a(HALF, BlockSmallDripleaf.EnumBlockHalf.LOWER).func_177226_a(FACING, EnumFacing.NORTH)
      );
      this.func_149711_c(0.0F);
      this.func_149672_a(SoundType.field_185850_c);
      this.setRegistryName("depthsupdate", "small_dripleaf");
      this.func_149663_c("small_dripleaf");
      this.func_149647_a(CreativeTabs.field_78031_c);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return SHAPE;
   }

   protected boolean func_185514_i(IBlockState state) {
      Block block = state.func_177230_c();
      return block == Blocks.field_150346_d
         || block == Blocks.field_150349_c
         || block == Blocks.field_150458_ak
         || block == Blocks.field_150435_aG
         || block == Blocks.field_150391_bh;
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      return super.func_176196_c(worldIn, pos) && worldIn.func_175623_d(pos.func_177984_a());
   }

   public void func_180633_a(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      worldIn.func_180501_a(
         pos.func_177984_a(),
         this.func_176223_P().func_177226_a(HALF, BlockSmallDripleaf.EnumBlockHalf.UPPER).func_177226_a(FACING, (EnumFacing)state.func_177229_b(FACING)),
         2
      );
   }

   public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.func_176223_P().func_177226_a(FACING, placer.func_174811_aO().func_176734_d());
   }

   public void func_176208_a(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
      if (state.func_177229_b(HALF) == BlockSmallDripleaf.EnumBlockHalf.UPPER) {
         if (worldIn.func_180495_p(pos.func_177977_b()).func_177230_c() == this) {
            if (player.field_71075_bZ.field_75098_d) {
               worldIn.func_175698_g(pos.func_177977_b());
            } else {
               worldIn.func_175655_b(pos.func_177977_b(), true);
            }
         }
      } else if (worldIn.func_180495_p(pos.func_177984_a()).func_177230_c() == this) {
         worldIn.func_180501_a(pos.func_177984_a(), Blocks.field_150350_a.func_176223_P(), 2);
      }

      super.func_176208_a(worldIn, pos, state, player);
   }

   public boolean func_180671_f(World worldIn, BlockPos pos, IBlockState state) {
      if (state.func_177229_b(HALF) == BlockSmallDripleaf.EnumBlockHalf.UPPER) {
         return worldIn.func_180495_p(pos.func_177977_b()).func_177230_c() == this;
      } else {
         IBlockState iblockstate = worldIn.func_180495_p(pos.func_177984_a());
         return iblockstate.func_177230_c() == this && super.func_180671_f(worldIn, pos, state);
      }
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return Items.field_190931_a;
   }

   public ItemStack func_185473_a(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(this);
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P()
         .func_177226_a(HALF, (meta & 8) > 0 ? BlockSmallDripleaf.EnumBlockHalf.UPPER : BlockSmallDripleaf.EnumBlockHalf.LOWER)
         .func_177226_a(FACING, EnumFacing.func_176731_b(meta & 3));
   }

   public int func_176201_c(IBlockState state) {
      int i = 0;
      i |= ((EnumFacing)state.func_177229_b(FACING)).func_176736_b();
      if (state.func_177229_b(HALF) == BlockSmallDripleaf.EnumBlockHalf.UPPER) {
         i |= 8;
      }

      return i;
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{HALF, FACING});
   }

   public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
      return true;
   }

   public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      return Collections.singletonList(new ItemStack(this, 1));
   }

   public boolean func_176473_a(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      return true;
   }

   public boolean func_180670_a(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return true;
   }

   public void func_176474_b(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      BlockPos bottomPos = state.func_177229_b(HALF) == BlockSmallDripleaf.EnumBlockHalf.LOWER ? pos : pos.func_177977_b();
      EnumFacing facing = (EnumFacing)state.func_177229_b(FACING);
      worldIn.func_175698_g(bottomPos.func_177984_a());
      worldIn.func_175698_g(bottomPos);
      int height = rand.nextInt(4) + 2;
      MutableBlockPos mpos = new MutableBlockPos(bottomPos);
      int currentHeight = 0;

      while (currentHeight < height && worldIn.func_175623_d(mpos)) {
         currentHeight++;
         mpos.func_189536_c(EnumFacing.UP);
      }

      int targetHeadY = bottomPos.func_177956_o() + currentHeight - 1;
      mpos.func_181079_c(bottomPos.func_177958_n(), bottomPos.func_177956_o(), bottomPos.func_177952_p());
      if (PlantRegistry.big_dripleaf != null && PlantRegistry.big_dripleaf_stem != null) {
         while (mpos.func_177956_o() < targetHeadY) {
            worldIn.func_180501_a(mpos, PlantRegistry.big_dripleaf_stem.func_176223_P().func_177226_a(BlockHorizontal.field_185512_D, facing), 3);
            mpos.func_189536_c(EnumFacing.UP);
         }

         worldIn.func_180501_a(mpos, PlantRegistry.big_dripleaf.func_176223_P().func_177226_a(BlockHorizontal.field_185512_D, facing), 3);
      }
   }

   public static enum EnumBlockHalf implements IStringSerializable {
      UPPER,
      LOWER;

      @Override
      public String toString() {
         return this.func_176610_l();
      }

      public String func_176610_l() {
         return this == UPPER ? "upper" : "lower";
      }
   }
}
