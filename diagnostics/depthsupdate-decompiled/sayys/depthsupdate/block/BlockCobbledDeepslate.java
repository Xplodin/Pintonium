package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockCobbledDeepslate extends Block {
   public BlockCobbledDeepslate() {
      super(Material.field_151576_e);
      this.setRegistryName("depthsupdate", "cobbled_deepslate");
      this.func_149663_c("cobbled_deepslate");
      this.func_149711_c(3.5F);
      this.func_149752_b(6.0F);
      this.func_149672_a(SoundType.field_185851_d);
      this.func_149647_a(CreativeTabs.field_78030_b);
   }

   public MapColor func_180659_g(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.field_151665_m;
   }
}
