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
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.registry.PlantRegistry;

public class BlockBigDripleaf extends Block implements IGrowable {
   public static final PropertyDirection FACING = BlockHorizontal.field_185512_D;
   public static final PropertyEnum<BlockBigDripleaf.EnumTilt> TILT = PropertyEnum.func_177709_a("tilt", BlockBigDripleaf.EnumTilt.class);
   protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0);
   protected static final AxisAlignedBB LEAF_COLLISION_NONE = new AxisAlignedBB(0.0, 0.6875, 0.0, 1.0, 0.9375, 1.0);
   protected static final AxisAlignedBB LEAF_COLLISION_PARTIAL = new AxisAlignedBB(0.0, 0.6875, 0.0, 1.0, 0.8125, 1.0);

   public BlockBigDripleaf() {
      super(Material.field_151585_k, MapColor.field_151669_i);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(FACING, EnumFacing.NORTH).func_177226_a(TILT, BlockBigDripleaf.EnumTilt.NONE));
      this.func_149711_c(0.1F);
      this.setHarvestLevel("axe", 0);
      this.func_149672_a(SoundType.field_185850_c);
      this.setRegistryName("depthsupdate", "big_dripleaf");
      this.func_149663_c("big_dripleaf");
      this.func_149647_a(CreativeTabs.field_78031_c);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return SHAPE;
   }

   @Nullable
   public AxisAlignedBB func_180646_a(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
      BlockBigDripleaf.EnumTilt tilt = (BlockBigDripleaf.EnumTilt)blockState.func_177229_b(TILT);
      if (tilt == BlockBigDripleaf.EnumTilt.FULL) {
         return field_185506_k;
      } else {
         return tilt == BlockBigDripleaf.EnumTilt.PARTIAL ? LEAF_COLLISION_PARTIAL : LEAF_COLLISION_NONE;
      }
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
      Block downBlock = downState.func_177230_c();
      return downBlock == this
         || PlantRegistry.big_dripleaf_stem != null && downBlock == PlantRegistry.big_dripleaf_stem
         || downState.isSideSolid(worldIn, pos.func_177977_b(), EnumFacing.UP)
         || downBlock == Blocks.field_150346_d
         || downBlock == Blocks.field_150349_c
         || downBlock == Blocks.field_150435_aG
         || downBlock == Blocks.field_150458_ak;
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.func_176196_c(worldIn, pos)) {
         worldIn.func_175655_b(pos, true);
      } else if (!worldIn.field_72995_K && worldIn.func_175640_z(pos)) {
         this.resetTilt(state, worldIn, pos);
      }
   }

   public void func_180634_a(World worldIn, BlockPos pos, IBlockState state, Entity entityIn) {
      if (!worldIn.field_72995_K) {
         if (entityIn instanceof IProjectile) {
            this.setTiltAndScheduleTick(state, worldIn, pos, BlockBigDripleaf.EnumTilt.FULL, SoundEvents.field_187571_bR);
         } else if (state.func_177229_b(TILT) == BlockBigDripleaf.EnumTilt.NONE && canEntityTilt(pos, entityIn) && !worldIn.func_175640_z(pos)) {
            this.setTiltAndScheduleTick(state, worldIn, pos, BlockBigDripleaf.EnumTilt.UNSTABLE, null);
         }
      }
   }

   public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      if (worldIn.func_175640_z(pos)) {
         this.resetTilt(state, worldIn, pos);
      } else {
         BlockBigDripleaf.EnumTilt tilt = (BlockBigDripleaf.EnumTilt)state.func_177229_b(TILT);
         if (tilt == BlockBigDripleaf.EnumTilt.UNSTABLE) {
            this.setTiltAndScheduleTick(state, worldIn, pos, BlockBigDripleaf.EnumTilt.PARTIAL, SoundEvents.field_187571_bR);
         } else if (tilt == BlockBigDripleaf.EnumTilt.PARTIAL) {
            this.setTiltAndScheduleTick(state, worldIn, pos, BlockBigDripleaf.EnumTilt.FULL, SoundEvents.field_187571_bR);
         } else if (tilt == BlockBigDripleaf.EnumTilt.FULL) {
            this.resetTilt(state, worldIn, pos);
         }
      }
   }

   private void setTiltAndScheduleTick(IBlockState state, World world, BlockPos pos, BlockBigDripleaf.EnumTilt tilt, @Nullable SoundEvent sound) {
      this.setTilt(state, world, pos, tilt);
      if (sound != null) {
         playTiltSound(world, pos, sound);
      }

      int tickDelay = -1;
      if (tilt == BlockBigDripleaf.EnumTilt.UNSTABLE) {
         tickDelay = 10;
      } else if (tilt == BlockBigDripleaf.EnumTilt.PARTIAL) {
         tickDelay = 10;
      } else if (tilt == BlockBigDripleaf.EnumTilt.FULL) {
         tickDelay = 100;
      }

      if (tickDelay != -1) {
         world.func_175684_a(pos, this, tickDelay);
      }
   }

   private void resetTilt(IBlockState state, World level, BlockPos pos) {
      this.setTilt(state, level, pos, BlockBigDripleaf.EnumTilt.NONE);
      if (state.func_177229_b(TILT) != BlockBigDripleaf.EnumTilt.NONE) {
         playTiltSound(level, pos, SoundEvents.field_187577_bU);
      }
   }

   private void setTilt(IBlockState state, World world, BlockPos pos, BlockBigDripleaf.EnumTilt tilt) {
      world.func_180501_a(pos, state.func_177226_a(TILT, tilt), 2);
   }

   private static boolean canEntityTilt(BlockPos pos, Entity entity) {
      return entity.field_70122_E && entity.field_70163_u > pos.func_177956_o() + 0.6875F;
   }

   private static void playTiltSound(World level, BlockPos pos, SoundEvent tiltSound) {
      float pitch = 0.8F + level.field_73012_v.nextFloat() * 0.4F;
      level.func_184133_a(null, pos, tiltSound, SoundCategory.BLOCKS, 1.0F, pitch);
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return Item.func_150898_a(this);
   }

   public ItemStack func_185473_a(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(this);
   }

   public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState belowState = world.func_180495_p(pos.func_177977_b());
      boolean belowIsDripleafPart = belowState.func_177230_c() == this
         || PlantRegistry.big_dripleaf_stem != null && belowState.func_177230_c() == PlantRegistry.big_dripleaf_stem;
      return this.func_176223_P()
         .func_177226_a(FACING, belowIsDripleafPart ? (EnumFacing)belowState.func_177229_b(FACING) : placer.func_174811_aO().func_176734_d());
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P()
         .func_177226_a(FACING, EnumFacing.func_176731_b(meta & 3))
         .func_177226_a(TILT, BlockBigDripleaf.EnumTilt.values()[meta >> 2 & 3]);
   }

   public int func_176201_c(IBlockState state) {
      int i = ((EnumFacing)state.func_177229_b(FACING)).func_176736_b();
      return i | ((BlockBigDripleaf.EnumTilt)state.func_177229_b(TILT)).ordinal() << 2;
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{FACING, TILT});
   }

   public boolean func_176473_a(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      return worldIn.func_175623_d(pos.func_177984_a());
   }

   public boolean func_180670_a(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return true;
   }

   public void func_176474_b(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      BlockPos upPos = pos.func_177984_a();
      if (worldIn.func_175623_d(upPos) && PlantRegistry.big_dripleaf_stem != null) {
         worldIn.func_180501_a(pos, PlantRegistry.big_dripleaf_stem.func_176223_P().func_177226_a(FACING, (EnumFacing)state.func_177229_b(FACING)), 3);
         worldIn.func_180501_a(upPos, state, 3);
      }
   }

   public static enum EnumTilt implements IStringSerializable {
      NONE("none"),
      UNSTABLE("unstable"),
      PARTIAL("partial"),
      FULL("full");

      private final String name;

      private EnumTilt(String name) {
         this.name = name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      public String func_176610_l() {
         return this.name;
      }
   }
}
