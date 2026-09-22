package sayys.depthsupdate.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.registry.PlantRegistry;

public class BlockBigDripleafStem extends Block implements IGrowable {
   public static final PropertyDirection FACING = BlockHorizontal.field_185512_D;
   protected static final AxisAlignedBB SHAPE_NORTH = new AxisAlignedBB(0.3125, 0.0, 0.25, 0.6875, 1.0, 1.0);
   protected static final AxisAlignedBB SHAPE_SOUTH = new AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 1.0, 0.75);
   protected static final AxisAlignedBB SHAPE_WEST = new AxisAlignedBB(0.25, 0.0, 0.3125, 1.0, 1.0, 0.6875);
   protected static final AxisAlignedBB SHAPE_EAST = new AxisAlignedBB(0.0, 0.0, 0.3125, 0.75, 1.0, 0.6875);

   public BlockBigDripleafStem() {
      super(Material.field_151585_k, MapColor.field_151669_i);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(FACING, EnumFacing.NORTH));
      this.func_149711_c(0.1F);
      this.setHarvestLevel("axe", 0);
      this.func_149672_a(SoundType.field_185850_c);
      this.setRegistryName("depthsupdate", "big_dripleaf_stem");
      this.func_149663_c("big_dripleaf_stem");
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      switch ((EnumFacing)state.func_177229_b(FACING)) {
         case SOUTH:
            return SHAPE_SOUTH;
         case WEST:
            return SHAPE_WEST;
         case EAST:
            return SHAPE_EAST;
         case NORTH:
         default:
            return SHAPE_NORTH;
      }
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
      IBlockState downState = worldIn.func_180495_p(pos.func_177977_b());
      IBlockState upState = worldIn.func_180495_p(pos.func_177984_a());
      Block upBlock = upState.func_177230_c();
      Block downBlock = downState.func_177230_c();
      boolean canStayDown = downBlock == this
         || downState.isSideSolid(worldIn, pos.func_177977_b(), EnumFacing.UP)
         || downBlock == Blocks.field_150346_d
         || downBlock == Blocks.field_150349_c
         || downBlock == Blocks.field_150435_aG
         || downBlock == Blocks.field_150458_ak;
      boolean canStayUp = upBlock == this || PlantRegistry.big_dripleaf != null && upBlock == PlantRegistry.big_dripleaf;
      return canStayDown && canStayUp;
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.func_176196_c(worldIn, pos)) {
         worldIn.func_175655_b(pos, true);
      }
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return PlantRegistry.big_dripleaf == null ? Items.field_190931_a : Item.func_150898_a(PlantRegistry.big_dripleaf);
   }

   public ItemStack func_185473_a(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(PlantRegistry.big_dripleaf);
   }

   public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.func_176223_P().func_177226_a(FACING, placer.func_174811_aO().func_176734_d());
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P().func_177226_a(FACING, EnumFacing.func_176731_b(meta & 3));
   }

   public int func_176201_c(IBlockState state) {
      return ((EnumFacing)state.func_177229_b(FACING)).func_176736_b();
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   public boolean func_176473_a(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      BlockPos headPos = this.findHead(worldIn, pos);
      return headPos != null && worldIn.func_175623_d(headPos.func_177984_a());
   }

   public boolean func_180670_a(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return true;
   }

   public void func_176474_b(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      BlockPos headPos = this.findHead(worldIn, pos);
      if (headPos != null) {
         BlockPos placePos = headPos.func_177984_a();
         IBlockState headState = worldIn.func_180495_p(headPos);
         EnumFacing facing = headState.func_177228_b().containsKey(FACING)
            ? (EnumFacing)headState.func_177229_b(FACING)
            : (EnumFacing)state.func_177229_b(FACING);
         worldIn.func_180501_a(headPos, this.func_176223_P().func_177226_a(FACING, facing), 3);
         worldIn.func_180501_a(placePos, headState, 3);
      }
   }

   private BlockPos findHead(World worldIn, BlockPos pos) {
      MutableBlockPos mpos = new MutableBlockPos(pos);

      for (int i = 0; i < 256; i++) {
         mpos.func_189536_c(EnumFacing.UP);
         Block block = worldIn.func_180495_p(mpos).func_177230_c();
         if (block == PlantRegistry.big_dripleaf) {
            return mpos.func_185334_h();
         }

         if (block != this) {
            break;
         }
      }

      return null;
   }
}
