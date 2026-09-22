package sayys.depthsupdate.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.client.particle.ParticleSporeBlossomAir;
import sayys.depthsupdate.client.particle.ParticleSporeBlossomFall;

public class BlockSporeBlossom extends BlockBush implements IShearable {
   protected static final AxisAlignedBB SHAPE = new AxisAlignedBB(0.125, 0.1875, 0.125, 0.875, 1.0, 0.875);

   public BlockSporeBlossom() {
      super(Material.field_151585_k, MapColor.field_151669_i);
      this.func_149711_c(0.0F);
      this.func_149672_a(SoundType.field_185850_c);
      this.setRegistryName("depthsupdate", "spore_blossom");
      this.func_149663_c("spore_blossom");
      this.func_149647_a(CreativeTabs.field_78031_c);
   }

   public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos) {
      return SHAPE;
   }

   protected boolean func_185514_i(IBlockState state) {
      return false;
   }

   public boolean func_176196_c(World worldIn, BlockPos pos) {
      IBlockState upState = worldIn.func_180495_p(pos.func_177984_a());
      Block upBlock = upState.func_177230_c();
      return worldIn.func_180495_p(pos).func_177230_c().func_176200_f(worldIn, pos)
         && upState.isSideSolid(worldIn, pos.func_177984_a(), EnumFacing.DOWN)
         && upBlock != Blocks.field_150362_t
         && upBlock != Blocks.field_150361_u;
   }

   public IBlockState func_180642_a(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.func_176223_P();
   }

   public boolean func_180671_f(World worldIn, BlockPos pos, IBlockState state) {
      IBlockState upState = worldIn.func_180495_p(pos.func_177984_a());
      Block upBlock = upState.func_177230_c();
      return upState.isSideSolid(worldIn, pos.func_177984_a(), EnumFacing.DOWN) && upBlock != Blocks.field_150362_t && upBlock != Blocks.field_150361_u;
   }

   public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      if (!this.func_180671_f(worldIn, pos, state)) {
         worldIn.func_175655_b(pos, true);
      }
   }

   @SideOnly(Side.CLIENT)
   public void func_180655_c(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
      int x = pos.func_177958_n();
      int y = pos.func_177956_o();
      int z = pos.func_177952_p();
      double xFalling = x + rand.nextDouble();
      double yFalling = y + 0.7;
      double zFalling = z + rand.nextDouble();
      ParticleSporeBlossomFall fallParticle = new ParticleSporeBlossomFall(worldIn, xFalling, yFalling, zFalling);
      FMLClientHandler.instance().getClient().field_71452_i.func_78873_a(fallParticle);
      MutableBlockPos ambientPos = new MutableBlockPos();

      for (int i = 0; i < 14; i++) {
         int cx = x + MathHelper.func_76136_a(rand, -10, 10);
         int cy = y - rand.nextInt(10);
         int cz = z + MathHelper.func_76136_a(rand, -10, 10);
         ambientPos.func_181079_c(cx, cy, cz);
         if (worldIn.func_175623_d(ambientPos)) {
            double pa_x = cx + rand.nextDouble();
            double pa_y = cy + rand.nextDouble();
            double pa_z = cz + rand.nextDouble();
            ParticleSporeBlossomAir airParticle = new ParticleSporeBlossomAir(worldIn, pa_x, pa_y, pa_z, 0.0, -0.8F, 0.0);
            FMLClientHandler.instance().getClient().field_71452_i.func_78873_a(airParticle);
         }
      }
   }

   public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
      return true;
   }

   public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
      return Collections.singletonList(new ItemStack(this, 1));
   }
}
