package sayys.depthsupdate.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IPlantable;
import sayys.depthsupdate.registry.IHasModel;

public class BlockSimple extends Block implements IHasModel {
   public BlockSimple(String name, Material material, float hardness, float resistance, SoundType soundType) {
      super(material);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149711_c(hardness);
      this.func_149752_b(resistance);
      this.func_149672_a(soundType);
      this.func_149647_a(CreativeTabs.field_78030_b);
   }

   public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
      return state.func_185904_a() != Material.field_151577_b && state.func_185904_a() != Material.field_151578_c
         ? super.canSustainPlant(state, world, pos, direction, plantable)
         : true;
   }
}
