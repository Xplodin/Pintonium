package sayys.depthsupdate.block;

import git.jbredwards.fluidlogged_api.api.block.IFluidloggable;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jspecify.annotations.NonNull;
import sayys.depthsupdate.compat.FluidloggedCompat;
import sayys.depthsupdate.registry.DeepslateRegistry;

@Interface(iface = "git.jbredwards.fluidlogged_api.api.block.IFluidloggable", modid = "fluidlogged_api")
public class BlockPointedDripstone extends Block implements IFluidloggable {
   public static final PropertyEnum<BlockPointedDripstone.DripstoneThickness> THICKNESS = PropertyEnum.func_177709_a(
      "thickness", BlockPointedDripstone.DripstoneThickness.class
   );
   public static final PropertyDirection TIP_DIRECTION = PropertyDirection.func_177712_a("tip_direction", Plane.VERTICAL);
   private static final AxisAlignedBB SHAPE_TIP_MERGE = new AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 1.0, 0.8);
   private static final AxisAlignedBB SHAPE_TIP_UP = new AxisAlignedBB(0.2, 0.0, 0.2, 0.8, 0.7, 0.8);
   private static final AxisAlignedBB SHAPE_TIP_DOWN = new AxisAlignedBB(0.2, 0.3, 0.2, 0.8, 1.0, 0.8);
   private static final AxisAlignedBB SHAPE_FRUSTUM = new AxisAlignedBB(0.15, 0.0, 0.15, 0.85, 1.0, 0.85);
   private static final AxisAlignedBB SHAPE_MIDDLE = new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 1.0, 0.9);
   private static final AxisAlignedBB SHAPE_BASE = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

   public BlockPointedDripstone() {
      super(Material.field_151576_e);
      this.func_180632_j(
         this.field_176227_L.func_177621_b().func_177226_a(TIP_DIRECTION, EnumFacing.UP).func_177226_a(THICKNESS, BlockPointedDripstone.DripstoneThickness.TIP)
      );
      this.func_149711_c(1.5F);
      this.func_149752_b(3.0F);
      this.func_149672_a(SoundType.field_185851_d);
      this.setRegistryName("depthsupdate", "pointed_dripstone");
      this.func_149663_c("pointed_dripstone");
      this.func_149647_a(CreativeTabs.field_78031_c);
      this.func_149675_a(true);
      this.func_149713_g(0);
   }

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{TIP_DIRECTION, THICKNESS});
   }

   public int func_176201_c(@NonNull IBlockState state) {
      int i = 0;
      if (state.func_177229_b(TIP_DIRECTION) == EnumFacing.UP) {
         i |= 8;
      }

      return i | ((BlockPointedDripstone.DripstoneThickness)state.func_177229_b(THICKNESS)).ordinal();
   }

   public IBlockState func_176203_a(int meta) {
      EnumFacing direction = (meta & 8) != 0 ? EnumFacing.UP : EnumFacing.DOWN;
      int thicknessOrd = meta & 7;
      BlockPointedDripstone.DripstoneThickness thickness = BlockPointedDripstone.DripstoneThickness.values()[Math.min(
         thicknessOrd, BlockPointedDripstone.DripstoneThickness.values().length - 1
      )];
      return this.func_176223_P().func_177226_a(TIP_DIRECTION, direction).func_177226_a(THICKNESS, thickness);
   }

   public AxisAlignedBB func_185496_a(@NonNull IBlockState state, IBlockAccess source, BlockPos pos) {
      BlockPointedDripstone.DripstoneThickness thickness = (BlockPointedDripstone.DripstoneThickness)state.func_177229_b(THICKNESS);
      EnumFacing dir = (EnumFacing)state.func_177229_b(TIP_DIRECTION);

      return switch (thickness) {
         case TIP_MERGE -> SHAPE_TIP_MERGE;
         case TIP -> dir == EnumFacing.UP ? SHAPE_TIP_UP : SHAPE_TIP_DOWN;
         case FRUSTUM -> SHAPE_FRUSTUM;
         case MIDDLE -> SHAPE_MIDDLE;
         case BASE -> SHAPE_BASE;
         default -> field_185505_j;
      };
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

   public BlockFaceShape func_193383_a(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return BlockFaceShape.UNDEFINED;
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      return isValidPointedDripstonePlacement(worldIn, pos, EnumFacing.DOWN) || isValidPointedDripstonePlacement(worldIn, pos, EnumFacing.UP);
   }

   public boolean func_176198_a(World worldIn, BlockPos pos, EnumFacing side) {
      return this.func_176196_c(worldIn, pos);
   }

   public IBlockState func_180642_a(
      World world, BlockPos pos, @NonNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer
   ) {
      EnumFacing defaultTipDirection = facing.func_176734_d();
      EnumFacing tipDirection = this.calculateTipDirection(world, pos, defaultTipDirection);
      if (tipDirection == null) {
         return this.func_176223_P();
      } else {
         boolean mergeOpposingTips = !placer.func_70093_af();
         BlockPointedDripstone.DripstoneThickness thickness = calculateDripstoneThickness(world, pos, tipDirection, mergeOpposingTips);
         return this.func_176223_P().func_177226_a(TIP_DIRECTION, tipDirection).func_177226_a(THICKNESS, thickness);
      }
   }

   public void func_176213_c(@NonNull World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.field_72995_K && !isValidPointedDripstonePlacement(worldIn, pos, (EnumFacing)state.func_177229_b(TIP_DIRECTION))) {
         worldIn.func_175655_b(pos, true);
      }
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      super.func_189540_a(state, worldIn, pos, blockIn, fromPos);
      if (!worldIn.field_72995_K) {
         EnumFacing tipDirection = (EnumFacing)state.func_177229_b(TIP_DIRECTION);
         if (!isValidPointedDripstonePlacement(worldIn, pos, tipDirection)) {
            worldIn.func_175684_a(pos, this, 2);
         } else {
            BlockPointedDripstone.DripstoneThickness newThickness = calculateDripstoneThickness(worldIn, pos, tipDirection, true);
            if (newThickness != state.func_177229_b(THICKNESS)) {
               worldIn.func_180501_a(pos, state.func_177226_a(THICKNESS, newThickness), 3);
            }
         }
      }
   }

   public void func_180650_b(World worldIn, BlockPos pos, @NonNull IBlockState state, Random rand) {
      if (!isValidPointedDripstonePlacement(worldIn, pos, (EnumFacing)state.func_177229_b(TIP_DIRECTION))) {
         if (isStalactite(state)) {
            spawnFallingStalactite(state, worldIn, pos);
         } else {
            worldIn.func_175655_b(pos, true);
         }
      } else {
         maybeTransferFluid(state, worldIn, pos, rand.nextFloat());
         if (rand.nextFloat() < 0.011377778F && isStalactiteStartPos(state, worldIn, pos)) {
            growStalactiteOrStalagmiteIfPossible(state, worldIn, pos, rand);
         }
      }
   }

   public void func_180658_a(@NonNull World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
      IBlockState state = worldIn.func_180495_p(pos);
      if (state.func_177229_b(TIP_DIRECTION) == EnumFacing.UP && state.func_177229_b(THICKNESS) == BlockPointedDripstone.DripstoneThickness.TIP) {
         entityIn.func_180430_e(fallDistance + 2.5F, 2.0F);
      } else {
         super.func_180658_a(worldIn, pos, entityIn, fallDistance);
      }
   }

   private static void spawnFallingStalactite(IBlockState state, World world, BlockPos pos) {
      MutableBlockPos fallPos = new MutableBlockPos(pos);
      IBlockState fallState = state;

      while (isStalactite(fallState)) {
         EntityFallingBlock entity = new EntityFallingBlock(
            world, fallPos.func_177958_n() + 0.5, fallPos.func_177956_o(), fallPos.func_177952_p() + 0.5, fallState
         );
         entity.func_145806_a(true);
         entity.field_145813_c = true;
         entity.field_145812_b = 1;
         world.func_72838_d(entity);
         world.func_175698_g(fallPos);
         fallPos.func_189536_c(EnumFacing.DOWN);
         fallState = world.func_180495_p(fallPos);
      }
   }

   public static void growStalactiteOrStalagmiteIfPossible(
      IBlockState stalactiteStartState, @NonNull World world, @NonNull BlockPos stalactiteStartPos, Random random
   ) {
      IBlockState rootState = world.func_180495_p(stalactiteStartPos.func_177981_b(1));
      IBlockState stateAbove = world.func_180495_p(stalactiteStartPos.func_177981_b(2));
      if (canGrow(rootState, stateAbove)) {
         BlockPos stalactiteTipPos = findTip(stalactiteStartState, world, stalactiteStartPos, 7, false);
         if (stalactiteTipPos != null) {
            IBlockState stalactiteTipState = world.func_180495_p(stalactiteTipPos);
            if (canDrip(world, stalactiteTipPos, stalactiteTipState) && canTipGrow(stalactiteTipState, world, stalactiteTipPos)) {
               if (random.nextBoolean()) {
                  grow(world, stalactiteTipPos, EnumFacing.DOWN);
               } else {
                  growStalagmiteBelow(world, stalactiteTipPos);
               }
            }
         }
      }
   }

   private static boolean canGrow(@NonNull IBlockState rootState, IBlockState aboveState) {
      return rootState.func_177230_c() == DeepslateRegistry.dripstone_block && aboveState.func_185904_a() == Material.field_151586_h;
   }

   private static boolean canTipGrow(@NonNull IBlockState tipState, @NonNull World world, @NonNull BlockPos tipPos) {
      EnumFacing growDirection = (EnumFacing)tipState.func_177229_b(TIP_DIRECTION);
      BlockPos growPos = tipPos.func_177972_a(growDirection);
      IBlockState stateAtGrowPos = world.func_180495_p(growPos);
      return stateAtGrowPos.func_185904_a() != Material.field_151586_h && stateAtGrowPos.func_185904_a() != Material.field_151587_i
         ? stateAtGrowPos.func_177230_c().func_176200_f(world, growPos) || isUnmergedTipWithDirection(stateAtGrowPos, growDirection.func_176734_d())
         : false;
   }

   private static boolean isUnmergedTipWithDirection(IBlockState state, EnumFacing tipDirection) {
      return isTip(state, false) && state.func_177229_b(TIP_DIRECTION) == tipDirection;
   }

   private static void grow(@NonNull World world, @NonNull BlockPos growFromPos, EnumFacing growToDirection) {
      BlockPos targetPos = growFromPos.func_177972_a(growToDirection);
      IBlockState existingStateAtTargetPos = world.func_180495_p(targetPos);
      if (isUnmergedTipWithDirection(existingStateAtTargetPos, growToDirection.func_176734_d())) {
         createMergedTips(existingStateAtTargetPos, world, targetPos);
      } else if (existingStateAtTargetPos.func_177230_c().func_176200_f(world, targetPos)) {
         createDripstone(world, targetPos, growToDirection, BlockPointedDripstone.DripstoneThickness.TIP);
      }
   }

   private static void growStalagmiteBelow(World world, BlockPos posAboveStalagmite) {
      MutableBlockPos pos = new MutableBlockPos(posAboveStalagmite);

      for (int i = 0; i < 10; i++) {
         pos.func_189536_c(EnumFacing.DOWN);
         IBlockState state = world.func_180495_p(pos);
         if (state.func_185904_a() == Material.field_151586_h || state.func_185904_a() == Material.field_151587_i) {
            return;
         }

         if (isUnmergedTipWithDirection(state, EnumFacing.UP) && canTipGrow(state, world, pos)) {
            grow(world, pos, EnumFacing.UP);
            return;
         }

         if (isValidPointedDripstonePlacement(world, pos, EnumFacing.UP) && world.func_180495_p(pos.func_177977_b()).func_185904_a() != Material.field_151586_h
            )
          {
            grow(world, pos.func_177977_b(), EnumFacing.UP);
            return;
         }
      }
   }

   private static void createDripstone(@NonNull World world, BlockPos pos, EnumFacing direction, BlockPointedDripstone.DripstoneThickness thickness) {
      boolean inWater = world.func_180495_p(pos).func_185904_a() == Material.field_151586_h;
      IBlockState state = DeepslateRegistry.pointed_dripstone.func_176223_P().func_177226_a(TIP_DIRECTION, direction).func_177226_a(THICKNESS, thickness);
      world.func_180501_a(pos, state, 3);
      if (inWater) {
         FluidloggedCompat.logWater(world, pos, state);
      }
   }

   private static void createMergedTips(@NonNull IBlockState tipState, World world, BlockPos tipPos) {
      BlockPos stalactitePos;
      BlockPos stalagmitePos;
      if (tipState.func_177229_b(TIP_DIRECTION) == EnumFacing.UP) {
         stalagmitePos = tipPos;
         stalactitePos = tipPos.func_177984_a();
      } else {
         stalactitePos = tipPos;
         stalagmitePos = tipPos.func_177977_b();
      }

      createDripstone(world, stalactitePos, EnumFacing.DOWN, BlockPointedDripstone.DripstoneThickness.TIP_MERGE);
      createDripstone(world, stalagmitePos, EnumFacing.UP, BlockPointedDripstone.DripstoneThickness.TIP_MERGE);
   }

   @Nullable
   private EnumFacing calculateTipDirection(World world, BlockPos pos, EnumFacing defaultTipDirection) {
      if (defaultTipDirection != EnumFacing.UP && defaultTipDirection != EnumFacing.DOWN) {
         return null;
      } else if (isValidPointedDripstonePlacement(world, pos, defaultTipDirection)) {
         return defaultTipDirection;
      } else {
         return isValidPointedDripstonePlacement(world, pos, defaultTipDirection.func_176734_d()) ? defaultTipDirection.func_176734_d() : null;
      }
   }

   public static void refreshThickness(@NonNull World world, @NonNull BlockPos pos) {
      IBlockState state = world.func_180495_p(pos);
      if (state.func_177230_c() instanceof BlockPointedDripstone) {
         BlockPointedDripstone.DripstoneThickness thickness = calculateDripstoneThickness(world, pos, (EnumFacing)state.func_177229_b(TIP_DIRECTION), true);
         if (thickness != state.func_177229_b(THICKNESS)) {
            world.func_180501_a(pos, state.func_177226_a(THICKNESS, thickness), 2);
         }
      }
   }

   private static BlockPointedDripstone.DripstoneThickness calculateDripstoneThickness(
      @NonNull World world, @NonNull BlockPos pos, @NonNull EnumFacing tipDirection, boolean mergeOpposingTips
   ) {
      EnumFacing baseDirection = tipDirection.func_176734_d();
      IBlockState inFrontState = world.func_180495_p(pos.func_177972_a(tipDirection));
      if (isPointedDripstoneWithDirection(inFrontState, baseDirection)) {
         return mergeOpposingTips ? BlockPointedDripstone.DripstoneThickness.TIP_MERGE : BlockPointedDripstone.DripstoneThickness.TIP;
      } else if (!isPointedDripstoneWithDirection(inFrontState, tipDirection)) {
         return BlockPointedDripstone.DripstoneThickness.TIP;
      } else {
         BlockPointedDripstone.DripstoneThickness inFrontThickness = (BlockPointedDripstone.DripstoneThickness)inFrontState.func_177229_b(THICKNESS);
         if (inFrontThickness != BlockPointedDripstone.DripstoneThickness.TIP && inFrontThickness != BlockPointedDripstone.DripstoneThickness.TIP_MERGE) {
            IBlockState behindState = world.func_180495_p(pos.func_177972_a(baseDirection));
            return !isPointedDripstoneWithDirection(behindState, tipDirection)
               ? BlockPointedDripstone.DripstoneThickness.BASE
               : BlockPointedDripstone.DripstoneThickness.MIDDLE;
         } else {
            return BlockPointedDripstone.DripstoneThickness.FRUSTUM;
         }
      }
   }

   private static boolean isValidPointedDripstonePlacement(@NonNull World world, @NonNull BlockPos pos, @NonNull EnumFacing tipDirection) {
      BlockPos behindPos = pos.func_177972_a(tipDirection.func_176734_d());
      IBlockState behindState = world.func_180495_p(behindPos);
      return behindState.isSideSolid(world, behindPos, tipDirection) || isPointedDripstoneWithDirection(behindState, tipDirection);
   }

   public static boolean isPointedDripstoneWithDirection(@NonNull IBlockState state, EnumFacing tipDirection) {
      return state.func_177230_c() instanceof BlockPointedDripstone && state.func_177229_b(TIP_DIRECTION) == tipDirection;
   }

   @SideOnly(Side.CLIENT)
   public void func_180655_c(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
      if (canDrip(worldIn, pos, stateIn)) {
         float f = rand.nextFloat();
         if (f <= 0.12F) {
            Material fluidAbove = getFluidAboveStalactite(worldIn, pos, stateIn);
            if (fluidAbove != null && (f < 0.02F || fluidAbove == Material.field_151586_h || fluidAbove == Material.field_151587_i)) {
               spawnDripParticle(worldIn, pos, stateIn, fluidAbove);
            }
         }
      }
   }

   public static boolean canDrip(IBlockAccess world, BlockPos pos, IBlockState state) {
      return isStalactite(state) && state.func_177229_b(THICKNESS) == BlockPointedDripstone.DripstoneThickness.TIP && !FluidloggedCompat.hasFluid(world, pos);
   }

   private static boolean isStalactite(IBlockState state) {
      return isPointedDripstoneWithDirection(state, EnumFacing.DOWN);
   }

   @Nullable
   public static Material getFluidAboveStalactite(World world, BlockPos pos, IBlockState state) {
      if (!isStalactite(state)) {
         return null;
      } else {
         BlockPos rootPos = findRootBlock(world, pos, state, 11);
         if (rootPos != null) {
            BlockPos aboveRootPos = rootPos.func_177984_a();
            IBlockState aboveRootState = world.func_180495_p(aboveRootPos);
            Material material = aboveRootState.func_185904_a();
            if (material == Material.field_151586_h || material == Material.field_151587_i) {
               return material;
            }

            if (aboveRootState.isSideSolid(world, aboveRootPos, EnumFacing.DOWN)) {
               BlockPos liquidPos = aboveRootPos.func_177984_a();
               IBlockState liquidState = world.func_180495_p(liquidPos);
               Material liquidMaterial = liquidState.func_185904_a();
               if (liquidMaterial == Material.field_151586_h || liquidMaterial == Material.field_151587_i) {
                  return liquidMaterial;
               }
            }
         }

         return null;
      }
   }

   @Nullable
   private static BlockPos findRootBlock(World world, BlockPos pos, @NonNull IBlockState state, int maxSearchLength) {
      EnumFacing tipDirection = (EnumFacing)state.func_177229_b(TIP_DIRECTION);
      EnumFacing searchDirection = tipDirection.func_176734_d();
      MutableBlockPos mutablePos = new MutableBlockPos(pos);

      for (int i = 1; i < maxSearchLength; i++) {
         mutablePos.func_189536_c(searchDirection);
         IBlockState checkState = world.func_180495_p(mutablePos);
         if (!isPointedDripstoneWithDirection(checkState, tipDirection)) {
            return mutablePos.func_177972_a(tipDirection).func_185334_h();
         }
      }

      return null;
   }

   @SideOnly(Side.CLIENT)
   private static void spawnDripParticle(World world, @NonNull BlockPos pos, IBlockState state, Material fluidAbove) {
      double x = pos.func_177958_n() + 0.5;
      double y = pos.func_177956_o();
      double z = pos.func_177952_p() + 0.5;
      if (fluidAbove == Material.field_151587_i) {
         world.func_175688_a(EnumParticleTypes.DRIP_LAVA, x, y, z, 0.0, 0.0, 0.0, new int[0]);
      } else {
         world.func_175688_a(EnumParticleTypes.DRIP_WATER, x, y, z, 0.0, 0.0, 0.0, new int[0]);
      }
   }

   public static void maybeTransferFluid(IBlockState state, World world, BlockPos pos, float randomValue) {
      if (!(randomValue > 0.17578125F) || !(randomValue > 0.05859375F)) {
         if (isStalactiteStartPos(state, world, pos)) {
            Material fluidAbove = getFluidAboveStalactite(world, pos, state);
            if (fluidAbove != null) {
               float transferProbability = fluidAbove == Material.field_151586_h ? 0.17578125F : 0.05859375F;
               if (randomValue < transferProbability) {
                  BlockPos tipPos = findTip(state, world, pos, 11, false);
                  if (tipPos != null && fluidAbove == Material.field_151586_h) {
                     BlockPos cauldronPos = findFillableCauldronBelowStalactiteTip(world, tipPos);
                     if (cauldronPos != null) {
                        IBlockState cauldronState = world.func_180495_p(cauldronPos);
                        if (cauldronState.func_177230_c() == Blocks.field_150383_bp) {
                           int level = (Integer)cauldronState.func_177229_b(BlockCauldron.field_176591_a);
                           if (level < 3) {
                              world.func_175656_a(cauldronPos, cauldronState.func_177226_a(BlockCauldron.field_176591_a, level + 1));
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean isStalactiteStartPos(IBlockState state, World world, BlockPos pos) {
      return isStalactite(state) && !(world.func_180495_p(pos.func_177984_a()).func_177230_c() instanceof BlockPointedDripstone);
   }

   @Nullable
   private static BlockPos findTip(IBlockState dripstoneState, World world, BlockPos pos, int maxSearchLength, boolean includeMergedTip) {
      if (isTip(dripstoneState, includeMergedTip)) {
         return pos;
      } else {
         EnumFacing searchDirection = (EnumFacing)dripstoneState.func_177229_b(TIP_DIRECTION);
         MutableBlockPos mutablePos = new MutableBlockPos(pos);

         for (int i = 1; i < maxSearchLength; i++) {
            mutablePos.func_189536_c(searchDirection);
            IBlockState checkState = world.func_180495_p(mutablePos);
            if (!isPointedDripstoneWithDirection(checkState, searchDirection)) {
               return null;
            }

            if (isTip(checkState, includeMergedTip)) {
               return mutablePos.func_185334_h();
            }
         }

         return null;
      }
   }

   private static boolean isTip(@NonNull IBlockState state, boolean includeMergedTip) {
      if (!(state.func_177230_c() instanceof BlockPointedDripstone)) {
         return false;
      } else {
         BlockPointedDripstone.DripstoneThickness thickness = (BlockPointedDripstone.DripstoneThickness)state.func_177229_b(THICKNESS);
         return thickness == BlockPointedDripstone.DripstoneThickness.TIP
            || includeMergedTip && thickness == BlockPointedDripstone.DripstoneThickness.TIP_MERGE;
      }
   }

   @Nullable
   private static BlockPos findFillableCauldronBelowStalactiteTip(World world, BlockPos tipPos) {
      MutableBlockPos mutablePos = new MutableBlockPos(tipPos);

      for (int i = 1; i < 11; i++) {
         mutablePos.func_189536_c(EnumFacing.DOWN);
         IBlockState state = world.func_180495_p(mutablePos);
         if (state.func_177230_c() == Blocks.field_150383_bp) {
            if ((Integer)state.func_177229_b(BlockCauldron.field_176591_a) < 3) {
               return mutablePos.func_185334_h();
            }

            return null;
         }

         if (state.func_185904_a() != Material.field_151579_a) {
            return null;
         }
      }

      return null;
   }

   public static enum DripstoneThickness implements IStringSerializable {
      TIP_MERGE("tip_merge"),
      TIP("tip"),
      FRUSTUM("frustum"),
      MIDDLE("middle"),
      BASE("base");

      private final String name;

      private DripstoneThickness(String name) {
         this.name = name;
      }

      public String func_176610_l() {
         return this.name;
      }
   }
}
