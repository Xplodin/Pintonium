package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import sayys.depthsupdate.registry.PlantRegistry;

public class BlockCaveVines extends BlockCaveVinesBase {
   public BlockCaveVines() {
      this.setRegistryName("depthsupdate", "cave_vines");
      this.func_149663_c("cave_vines");
      this.func_149675_a(true);
   }

   public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      if (!worldIn.field_72995_K && worldIn.func_175623_d(pos.func_177977_b()) && rand.nextFloat() < 0.11F) {
         boolean berriesOnNew = rand.nextFloat() < 0.11F;
         worldIn.func_175656_a(pos.func_177977_b(), this.func_176223_P().func_177226_a(BERRIES, berriesOnNew));
         worldIn.func_175656_a(pos, PlantRegistry.cave_vines_plant.func_176223_P().func_177226_a(BERRIES, (Boolean)state.func_177229_b(BERRIES)));
      }
   }
}
