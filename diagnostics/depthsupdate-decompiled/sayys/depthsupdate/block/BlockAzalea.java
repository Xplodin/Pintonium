package sayys.depthsupdate.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.registry.PlantRegistry;
import sayys.depthsupdate.util.BlockUtils;

@ParametersAreNonnullByDefault
public class BlockAzalea extends Block implements IGrowable {
   private static final AxisAlignedBB CANOPY_AABB = new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0);
   private static final AxisAlignedBB TRUNK_AABB = new AxisAlignedBB(0.375, 0.0, 0.375, 0.625, 0.5, 0.625);
   private static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
   private static final float BONEMEAL_SUCCESS_CHANCE = 0.45F;
   private static final int TRUNK_BASE_HEIGHT = 4;
   private static final int TRUNK_HEIGHT_VARIATION = 3;
   private static final int BEND_MIN = 1;
   private static final int BEND_VARIATION = 2;
   private static final int FOLIAGE_RADIUS = 3;
   private static final int FOLIAGE_HEIGHT = 2;
   private static final int FOLIAGE_ATTEMPTS = 50;
   private static final int FLOWERING_LEAF_RARITY = 4;

   public BlockAzalea(String name) {
      super(Material.field_151585_k, MapColor.field_151669_i);
      this.func_149711_c(0.5F);
      this.func_149672_a(SoundType.field_185850_c);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149647_a(CreativeTabs.field_78031_c);
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

   public void func_185477_a(
      IBlockState state,
      World worldIn,
      BlockPos pos,
      AxisAlignedBB entityBox,
      List<AxisAlignedBB> collidingBoxes,
      @Nullable Entity entityIn,
      boolean isActualState
   ) {
      func_185492_a(pos, entityBox, collidingBoxes, CANOPY_AABB);
      func_185492_a(pos, entityBox, collidingBoxes, TRUNK_AABB);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return BOUNDING_BOX;
   }

   @Nullable
   public RayTraceResult func_180636_a(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
      RayTraceResult result1 = this.func_185503_a(pos, start, end, CANOPY_AABB);
      RayTraceResult result2 = this.func_185503_a(pos, start, end, TRUNK_AABB);
      if (result1 == null) {
         return result2;
      } else if (result2 == null) {
         return result1;
      } else {
         double d1 = result1.field_72307_f.func_72436_e(start);
         double d2 = result2.field_72307_f.func_72436_e(start);
         return d1 < d2 ? result1 : result2;
      }
   }

   public boolean func_176473_a(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      return worldIn.func_180495_p(pos.func_177984_a()).func_177230_c() == Blocks.field_150350_a;
   }

   public boolean func_180670_a(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return rand.nextFloat() < 0.45F;
   }

   public void func_176474_b(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      if (!worldIn.field_72995_K) {
         int trunkHeight = 4 + rand.nextInt(3);

         for (int i = 1; i < trunkHeight + 2; i++) {
            if (!worldIn.func_175623_d(pos.func_177981_b(i))) {
               return;
            }
         }

         Block soil = worldIn.func_180495_p(pos.func_177977_b()).func_177230_c();
         if ((soil == Blocks.field_150346_d || soil == Blocks.field_150349_c) && BlockUtils.isRegistered(PlantRegistry.rooted_dirt)) {
            worldIn.func_180501_a(pos.func_177977_b(), PlantRegistry.rooted_dirt.func_176223_P(), 2);
         }

         BlockPos top = pos;

         for (int ix = 0; ix < trunkHeight; ix++) {
            top = pos.func_177981_b(ix);
            worldIn.func_180501_a(top, Blocks.field_150364_r.func_176223_P(), 2);
         }

         EnumFacing bend = Plane.HORIZONTAL.func_179518_a(rand);
         int bendLength = 1 + rand.nextInt(2);

         for (int ix = 0; ix < bendLength; ix++) {
            BlockPos next = top.func_177972_a(bend);
            if (!worldIn.func_175623_d(next)) {
               break;
            }

            top = next;
            worldIn.func_180501_a(
               next, Blocks.field_150364_r.func_176223_P().func_177226_a(BlockLog.field_176299_a, EnumAxis.func_176870_a(bend.func_176740_k())), 2
            );
         }

         this.placeFoliage(worldIn, rand, top);
      }
   }

   private void placeFoliage(World world, Random rand, BlockPos center) {
      for (int i = 0; i < 50; i++) {
         BlockPos leafPos = center.func_177982_a(rand.nextInt(7) - 3, rand.nextInt(3) - 1, rand.nextInt(7) - 3);
         if (world.func_175623_d(leafPos)) {
            Block leaves = rand.nextInt(4) == 0 ? PlantRegistry.flowering_azalea_leaves : PlantRegistry.azalea_leaves;
            world.func_180501_a(leafPos, leaves.func_176223_P().func_177226_a(BlockLeaves.field_176236_b, Boolean.FALSE), 2);
         }
      }
   }

   public boolean func_176205_b(IBlockAccess worldIn, BlockPos pos) {
      return false;
   }
}
