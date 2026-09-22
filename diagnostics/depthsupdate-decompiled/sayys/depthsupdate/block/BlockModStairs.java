package sayys.depthsupdate.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;

public class BlockModStairs extends BlockStairs {
   public BlockModStairs(String name, IBlockState modelState) {
      super(modelState);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149647_a(CreativeTabs.field_78030_b);
      this.field_149783_u = true;
   }
}
