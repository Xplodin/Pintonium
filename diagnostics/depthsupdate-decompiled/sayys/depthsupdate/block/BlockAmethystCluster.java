package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.registry.AmethystRegistry;
import sayys.depthsupdate.registry.IHasModel;

public class BlockAmethystCluster extends Block implements IHasModel {
   public static final PropertyDirection FACING = PropertyDirection.func_177714_a("facing");
   private final AxisAlignedBB[] shapes;

   public BlockAmethystCluster(String name, float height, float width, int lightValue) {
      super(Material.field_151592_s);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149711_c(1.5F);
      this.func_149752_b(1.5F);
      this.func_149672_a(SoundType.field_185853_f);
      this.func_149715_a(lightValue / 15.0F);
      this.func_149647_a(CreativeTabs.field_78031_c);
      this.func_180632_j(this.field_176227_L.func_177621_b().func_177226_a(FACING, EnumFacing.UP));
      double w = width / 16.0;
      double h = height / 16.0;
      double offset = (1.0 - w) / 2.0;
      this.shapes = new AxisAlignedBB[6];
      this.shapes[EnumFacing.UP.func_176745_a()] = new AxisAlignedBB(offset, 0.0, offset, 1.0 - offset, h, 1.0 - offset);
      this.shapes[EnumFacing.DOWN.func_176745_a()] = new AxisAlignedBB(offset, 1.0 - h, offset, 1.0 - offset, 1.0, 1.0 - offset);
      this.shapes[EnumFacing.NORTH.func_176745_a()] = new AxisAlignedBB(offset, offset, 1.0 - h, 1.0 - offset, 1.0 - offset, 1.0);
      this.shapes[EnumFacing.SOUTH.func_176745_a()] = new AxisAlignedBB(offset, offset, 0.0, 1.0 - offset, 1.0 - offset, h);
      this.shapes[EnumFacing.WEST.func_176745_a()] = new AxisAlignedBB(1.0 - h, offset, offset, 1.0, 1.0 - offset, 1.0 - offset);
      this.shapes[EnumFacing.EAST.func_176745_a()] = new AxisAlignedBB(0.0, offset, offset, h, 1.0 - offset, 1.0 - offset);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return this.shapes[((EnumFacing)state.func_177229_b(FACING)).func_176745_a()];
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

   protected BlockStateContainer func_180661_e() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   public IBlockState func_176203_a(int meta) {
      return this.func_176223_P().func_177226_a(FACING, EnumFacing.func_82600_a(meta & 7));
   }

   public int func_176201_c(IBlockState state) {
      return ((EnumFacing)state.func_177229_b(FACING)).func_176745_a();
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      for (EnumFacing enumfacing : EnumFacing.values()) {
         if (this.canPlaceOn(worldIn, pos, enumfacing)) {
            return true;
         }
      }

      return false;
   }

   private boolean canPlaceOn(World worldIn, BlockPos pos, EnumFacing facing) {
      BlockPos blockpos = pos.func_177972_a(facing.func_176734_d());
      return worldIn.func_180495_p(blockpos).isSideSolid(worldIn, blockpos, facing);
   }

   public IBlockState func_180642_a(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.func_176223_P().func_177226_a(FACING, facing);
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.canPlaceOn(worldIn, pos, (EnumFacing)state.func_177229_b(FACING))) {
         worldIn.func_175655_b(pos, true);
      }
   }

   public BlockFaceShape func_193383_a(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return BlockFaceShape.UNDEFINED;
   }

   public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
      return true;
   }

   public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      if (this == AmethystRegistry.amethyst_cluster) {
         Random rand = world instanceof World ? ((World)world).field_73012_v : new Random();
         EntityPlayer player = (EntityPlayer)this.harvesters.get();
         boolean isPickaxe = false;
         if (player != null) {
            ItemStack heldItem = player.func_184614_ca();
            if (!heldItem.func_190926_b() && heldItem.func_77973_b().getToolClasses(heldItem).contains("pickaxe")) {
               isPickaxe = true;
            }
         }

         int count = isPickaxe ? 4 : 2;
         if (fortune > 0) {
            if (fortune == 1) {
               if (rand.nextInt(3) == 0) {
                  count *= 2;
               }
            } else if (fortune == 2) {
               int r = rand.nextInt(4);
               if (r == 0) {
                  count *= 2;
               } else if (r == 1) {
                  count *= 3;
               }
            } else {
               int r = rand.nextInt(5);
               if (r == 0) {
                  count *= 2;
               } else if (r == 1) {
                  count *= 3;
               } else if (r == 2) {
                  count *= 4;
               }
            }
         }

         drops.add(new ItemStack(AmethystRegistry.amethyst_shard, count));
      }
   }
}
