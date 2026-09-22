package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockInfestedDeepslate extends BlockRotatedPillar {
   public BlockInfestedDeepslate() {
      super(Material.field_151571_B);
      this.setRegistryName("depthsupdate", "infested_deepslate");
      this.func_149663_c("infested_deepslate");
      this.func_149711_c(0.75F);
      this.func_149752_b(0.75F);
      this.func_149672_a(SoundType.field_185851_d);
      this.func_149647_a(CreativeTabs.field_78031_c);
   }

   public int func_149745_a(Random random) {
      return 0;
   }

   public void func_180653_a(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
      if (!worldIn.field_72995_K && worldIn.func_82736_K().func_82766_b("doTileDrops")) {
         EntitySilverfish entitysilverfish = new EntitySilverfish(worldIn);
         entitysilverfish.func_70012_b(pos.func_177958_n() + 0.5, pos.func_177956_o(), pos.func_177952_p() + 0.5, 0.0F, 0.0F);
         worldIn.func_72838_d(entitysilverfish);
         entitysilverfish.func_70656_aK();
      }
   }

   protected ItemStack func_180643_i(IBlockState state) {
      return new ItemStack(DeepslateRegistry.deepslate);
   }
}
