package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sayys.depthsupdate.registry.AmethystRegistry;
import sayys.depthsupdate.registry.IHasModel;

public class BlockBuddingAmethyst extends Block implements IHasModel {
   public BlockBuddingAmethyst() {
      super(Material.field_151592_s);
      this.setRegistryName("depthsupdate", "budding_amethyst");
      this.func_149663_c("budding_amethyst");
      this.func_149711_c(1.5F);
      this.func_149752_b(1.5F);
      this.func_149672_a(SoundType.field_185853_f);
      this.func_149647_a(CreativeTabs.field_78030_b);
      this.func_149675_a(true);
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return Items.field_190931_a;
   }

   protected boolean func_149700_E() {
      return false;
   }

   public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      if (rand.nextInt(5) == 0) {
         EnumFacing growDirection = EnumFacing.field_82609_l[rand.nextInt(EnumFacing.field_82609_l.length)];
         BlockPos growPos = pos.func_177972_a(growDirection);
         IBlockState relativeState = worldIn.func_180495_p(growPos);
         Block nextStage = null;
         if (canClusterGrowAtState(relativeState)) {
            nextStage = AmethystRegistry.small_amethyst_bud;
         } else if (relativeState.func_177230_c() == AmethystRegistry.small_amethyst_bud
            && relativeState.func_177229_b(BlockAmethystCluster.FACING) == growDirection) {
            nextStage = AmethystRegistry.medium_amethyst_bud;
         } else if (relativeState.func_177230_c() == AmethystRegistry.medium_amethyst_bud
            && relativeState.func_177229_b(BlockAmethystCluster.FACING) == growDirection) {
            nextStage = AmethystRegistry.large_amethyst_bud;
         } else if (relativeState.func_177230_c() == AmethystRegistry.large_amethyst_bud
            && relativeState.func_177229_b(BlockAmethystCluster.FACING) == growDirection) {
            nextStage = AmethystRegistry.amethyst_cluster;
         }

         if (nextStage != null) {
            IBlockState targetState = nextStage.func_176223_P().func_177226_a(BlockAmethystCluster.FACING, growDirection);
            worldIn.func_175656_a(growPos, targetState);
         }
      }
   }

   public static boolean canClusterGrowAtState(IBlockState state) {
      return state.func_185904_a() == Material.field_151579_a || state.func_185904_a() == Material.field_151586_h;
   }
}
