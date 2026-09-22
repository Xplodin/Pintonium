package sayys.depthsupdate.registry;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.DepthsUpdateConfig;

public final class ModCreativeTab {
   private static final String[] ICON_CANDIDATES = new String[]{"glow_berries", "amethyst_shard", "deepslate", "moss_block", "calcite"};
   private static CreativeTabs instance;

   private ModCreativeTab() {
   }

   public static void init() {
      if (instance == null && DepthsUpdateConfig.REGISTRY.useCustomCreativeTab) {
         instance = new CreativeTabs("depthsupdate") {
            @SideOnly(Side.CLIENT)
            public ItemStack func_78016_d() {
               return ModCreativeTab.icon();
            }
         };
      }
   }

   public static CreativeTabs get() {
      return instance;
   }

   @SideOnly(Side.CLIENT)
   private static ItemStack icon() {
      for (String candidate : ICON_CANDIDATES) {
         ResourceLocation name = new ResourceLocation("depthsupdate", candidate);
         if (Item.field_150901_e.func_148741_d(name)) {
            return new ItemStack((Item)Item.field_150901_e.func_82594_a(name));
         }
      }

      return new ItemStack(Blocks.field_150348_b);
   }
}
