package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockCopperOre extends Block {
   public BlockCopperOre() {
      super(Material.field_151576_e);
      this.setRegistryName("depthsupdate", "copper_ore");
      this.func_149663_c("copper_ore");
      this.func_149711_c(3.0F);
      this.func_149752_b(3.0F);
      this.func_149672_a(SoundType.field_185851_d);
      this.func_149647_a(CreativeTabs.field_78030_b);
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      return DeepslateRegistry.raw_copper;
   }

   public int func_149745_a(Random random) {
      return 2 + random.nextInt(4);
   }

   public int func_149679_a(int fortune, Random random) {
      return fortune > 0 ? BlockDeepslateOre.oreBonus(this.func_149745_a(random), fortune, random) : this.func_149745_a(random);
   }
}
