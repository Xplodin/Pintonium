package sayys.depthsupdate.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.registry.PlantRegistry;

public abstract class BlockCaveVinesBase extends Block implements IGrowable {
   public static final PropertyBool BERRIES = PropertyBool.func_177716_a("berries");
   protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375);

   public BlockCaveVinesBase() {
      super(Material.field_151585_k, MapColor.field_151669_i);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(BERRIES, false));
      this.func_149711_c(0.0F);
      this.func_149672_a(SoundType.field_185850_c);
   }

   public boolean func_176473_a(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      return !(Boolean)state.func_177229_b(BERRIES);
   }

   public boolean func_180670_a(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return true;
   }

   public void func_176474_b(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      worldIn.func_180501_a(pos, state.func_177226_a(BERRIES, true), 2);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return SHAPE;
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

   public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
      return true;
   }

   public int func_149750_m(IBlockState state) {
      return state.func_177229_b(BERRIES) ? 14 : 0;
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      IBlockState upState = worldIn.func_180495_p(pos.func_177984_a());
      Block upBlock = upState.func_177230_c();
      return upState.isSideSolid(worldIn, pos.func_177984_a(), EnumFacing.DOWN) || upBlock instanceof BlockCaveVinesBase;
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.func_176196_c(worldIn, pos)) {
         worldIn.func_175655_b(pos, true);
      }
   }

   public boolean func_180639_a(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      if ((Boolean)state.func_177229_b(BERRIES)) {
         if (!worldIn.field_72995_K) {
            worldIn.func_180501_a(pos, state.func_177226_a(BERRIES, false), 2);
            func_180635_a(worldIn, pos, new ItemStack(PlantRegistry.glow_berries, 1));
            worldIn.func_184133_a(playerIn, pos, SoundEvents.field_187575_bT, SoundCategory.BLOCKS, 1.0F, 0.8F + worldIn.field_73012_v.nextFloat() * 0.4F);
         }

         return true;
      } else {
         return super.func_180639_a(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
      }
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return state.func_177229_b(BERRIES) ? PlantRegistry.glow_berries : Items.field_190931_a;
   }

   public int func_149745_a(Random random) {
      return 1;
   }

   public ItemStack func_185473_a(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(PlantRegistry.glow_berries);
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P().func_177226_a(BERRIES, (meta & 1) > 0);
   }

   public int func_176201_c(IBlockState state) {
      return state.func_177229_b(BERRIES) ? 1 : 0;
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{BERRIES});
   }
}
