package sayys.depthsupdate.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import sayys.depthsupdate.registry.DeepslateRegistry;
import sayys.depthsupdate.registry.IHasModel;

public class BlockDeepslateOre extends Block implements IHasModel {
   public BlockDeepslateOre(String name) {
      super(Material.field_151576_e);
      this.setRegistryName("depthsupdate", name);
      this.func_149663_c(name);
      this.func_149711_c(4.5F);
      this.func_149752_b(3.0F);
      this.func_149672_a(SoundType.field_185851_d);
      this.func_149647_a(CreativeTabs.field_78030_b);
   }

   public Item func_180660_a(IBlockState state, Random rand, int fortune) {
      if (this == DeepslateRegistry.deepslate_iron_ore) {
         return DeepslateRegistry.raw_iron;
      } else if (this == DeepslateRegistry.deepslate_gold_ore) {
         return DeepslateRegistry.raw_gold;
      } else if (this == DeepslateRegistry.deepslate_copper_ore) {
         return DeepslateRegistry.raw_copper;
      } else if (this == DeepslateRegistry.deepslate_coal_ore) {
         return Items.field_151044_h;
      } else if (this == DeepslateRegistry.deepslate_diamond_ore) {
         return Items.field_151045_i;
      } else if (this == DeepslateRegistry.deepslate_emerald_ore) {
         return Items.field_151166_bC;
      } else if (this == DeepslateRegistry.deepslate_lapis_ore) {
         return Items.field_151100_aR;
      } else {
         return this == DeepslateRegistry.deepslate_redstone_ore ? Items.field_151137_ax : super.func_180660_a(state, rand, fortune);
      }
   }

   public int func_180651_a(IBlockState state) {
      return this == DeepslateRegistry.deepslate_lapis_ore ? 4 : super.func_180651_a(state);
   }

   public int func_149679_a(int fortune, Random random) {
      if (fortune <= 0 || Item.func_150898_a(this) == this.func_180660_a(this.func_176223_P(), random, fortune)) {
         return this.func_149745_a(random);
      } else {
         return this == DeepslateRegistry.deepslate_redstone_ore
            ? uniformBonus(this.func_149745_a(random), fortune, random)
            : oreBonus(this.func_149745_a(random), fortune, random);
      }
   }

   public int func_149745_a(Random random) {
      if (this == DeepslateRegistry.deepslate_copper_ore) {
         return 2 + random.nextInt(4);
      } else if (this == DeepslateRegistry.deepslate_redstone_ore) {
         return 4 + random.nextInt(2);
      } else {
         return this == DeepslateRegistry.deepslate_lapis_ore ? 4 + random.nextInt(6) : 1;
      }
   }

   static int oreBonus(int count, int fortune, Random random) {
      return count * Math.max(1, random.nextInt(fortune + 2));
   }

   static int uniformBonus(int count, int fortune, Random random) {
      return count + random.nextInt(fortune + 1);
   }
}
