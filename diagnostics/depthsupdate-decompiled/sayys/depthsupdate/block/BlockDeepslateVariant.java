package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import sayys.depthsupdate.registry.IHasModel;

public class BlockDeepslateVariant extends Block implements IHasModel {
   public BlockDeepslateVariant(String name, float hardness, float resistance, SoundType soundType) {
      super(Material.field_151576_e);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149711_c(hardness);
      this.func_149752_b(resistance);
      this.func_149672_a(soundType);
      this.func_149647_a(CreativeTabs.field_78030_b);
   }

   public MapColor func_180659_g(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.field_151665_m;
   }
}
