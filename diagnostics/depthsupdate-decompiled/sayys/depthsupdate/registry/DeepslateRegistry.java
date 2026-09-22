package sayys.depthsupdate.registry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockCobbledDeepslate;
import sayys.depthsupdate.block.BlockCopperOre;
import sayys.depthsupdate.block.BlockDeepslate;
import sayys.depthsupdate.block.BlockDeepslateOre;
import sayys.depthsupdate.block.BlockDeepslateVariant;
import sayys.depthsupdate.block.BlockInfestedDeepslate;
import sayys.depthsupdate.block.BlockModSlab;
import sayys.depthsupdate.block.BlockModStairs;
import sayys.depthsupdate.block.BlockModWall;
import sayys.depthsupdate.block.BlockPointedDripstone;
import sayys.depthsupdate.util.BlockUtils;

public class DeepslateRegistry {
   public static final Block deepslate = new BlockDeepslate();
   public static final Block cobbled_deepslate = new BlockCobbledDeepslate();
   public static final Block infested_deepslate = new BlockInfestedDeepslate();
   public static final Block polished_deepslate = new BlockDeepslateVariant("polished_deepslate", 3.5F, 6.0F, SoundType.field_185851_d);
   public static final Block chiseled_deepslate = new BlockDeepslateVariant("chiseled_deepslate", 3.5F, 6.0F, SoundType.field_185851_d);
   public static final Block deepslate_bricks = new BlockDeepslateVariant("deepslate_bricks", 3.5F, 6.0F, SoundType.field_185851_d);
   public static final Block deepslate_tiles = new BlockDeepslateVariant("deepslate_tiles", 3.5F, 6.0F, SoundType.field_185851_d);
   public static final Block cracked_deepslate_bricks = new BlockDeepslateVariant("cracked_deepslate_bricks", 3.5F, 6.0F, SoundType.field_185851_d);
   public static final Block cracked_deepslate_tiles = new BlockDeepslateVariant("cracked_deepslate_tiles", 3.5F, 6.0F, SoundType.field_185851_d);
   public static final Block cobbled_deepslate_stairs = new BlockModStairs("cobbled_deepslate_stairs", cobbled_deepslate.func_176223_P());
   public static final Block polished_deepslate_stairs = new BlockModStairs("polished_deepslate_stairs", polished_deepslate.func_176223_P());
   public static final Block deepslate_brick_stairs = new BlockModStairs("deepslate_brick_stairs", deepslate_bricks.func_176223_P());
   public static final Block deepslate_tile_stairs = new BlockModStairs("deepslate_tile_stairs", deepslate_tiles.func_176223_P());
   public static final Block cobbled_deepslate_wall = new BlockModWall("cobbled_deepslate_wall", cobbled_deepslate);
   public static final Block polished_deepslate_wall = new BlockModWall("polished_deepslate_wall", polished_deepslate);
   public static final Block deepslate_brick_wall = new BlockModWall("deepslate_brick_wall", deepslate_bricks);
   public static final Block deepslate_tile_wall = new BlockModWall("deepslate_tile_wall", deepslate_tiles);
   public static final Block dripstone_block = new BlockDeepslateVariant("dripstone_block", 1.5F, 1.0F, SoundType.field_185851_d);
   public static final Block pointed_dripstone = new BlockPointedDripstone();
   public static final BlockModSlab.Double deepslate_slab_double = new BlockModSlab.Double("deepslate_slab_double", Material.field_151576_e);
   public static final BlockModSlab.Half deepslate_slab_half = new BlockModSlab.Half("deepslate_slab_half", Material.field_151576_e, deepslate_slab_double);
   public static final Block calcite = new BlockDeepslateVariant("calcite", 0.75F, 0.75F, SoundType.field_185851_d);
   public static final Block tuff = new BlockDeepslateVariant("tuff", 1.5F, 6.0F, SoundType.field_185851_d);
   public static final Block smooth_basalt = new BlockDeepslateVariant("smooth_basalt", 1.25F, 4.2F, SoundType.field_185851_d);
   public static final Block copper_ore = new BlockCopperOre();
   public static final Block deepslate_coal_ore = new BlockDeepslateOre("deepslate_coal_ore");
   public static final Block deepslate_iron_ore = new BlockDeepslateOre("deepslate_iron_ore");
   public static final Block deepslate_gold_ore = new BlockDeepslateOre("deepslate_gold_ore");
   public static final Block deepslate_redstone_ore = new BlockDeepslateOre("deepslate_redstone_ore");
   public static final Block deepslate_lapis_ore = new BlockDeepslateOre("deepslate_lapis_ore");
   public static final Block deepslate_diamond_ore = new BlockDeepslateOre("deepslate_diamond_ore");
   public static final Block deepslate_emerald_ore = new BlockDeepslateOre("deepslate_emerald_ore");
   public static final Block deepslate_copper_ore = new BlockDeepslateOre("deepslate_copper_ore");
   public static final Block raw_iron_block = new BlockDeepslateVariant("raw_iron_block", 5.0F, 6.0F, SoundType.field_185851_d);
   public static final Block raw_gold_block = new BlockDeepslateVariant("raw_gold_block", 5.0F, 6.0F, SoundType.field_185851_d);
   public static final Block raw_copper_block = new BlockDeepslateVariant("raw_copper_block", 5.0F, 6.0F, SoundType.field_185851_d);
   public static final Item raw_iron = ((Item)new Item().setRegistryName("depthsupdate", "raw_iron"))
      .func_77655_b("raw_iron")
      .func_77637_a(CreativeTabs.field_78035_l);
   public static final Item raw_gold = ((Item)new Item().setRegistryName("depthsupdate", "raw_gold"))
      .func_77655_b("raw_gold")
      .func_77637_a(CreativeTabs.field_78035_l);
   public static final Item raw_copper = ((Item)new Item().setRegistryName("depthsupdate", "raw_copper"))
      .func_77655_b("raw_copper")
      .func_77637_a(CreativeTabs.field_78035_l);
   public static final Item copper_ingot = ((Item)new Item().setRegistryName("depthsupdate", "copper_ingot"))
      .func_77655_b("copper_ingot")
      .func_77637_a(CreativeTabs.field_78035_l);
   public static final RegistrationFeature DEEPSLATE_FAMILY = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableDeepslateFamily)
      .add(
         deepslate,
         cobbled_deepslate,
         infested_deepslate,
         polished_deepslate,
         chiseled_deepslate,
         deepslate_bricks,
         deepslate_tiles,
         cracked_deepslate_bricks,
         cracked_deepslate_tiles,
         cobbled_deepslate_stairs,
         polished_deepslate_stairs,
         deepslate_brick_stairs,
         deepslate_tile_stairs,
         cobbled_deepslate_wall,
         polished_deepslate_wall,
         deepslate_brick_wall,
         deepslate_tile_wall,
         deepslate_slab_half,
         deepslate_slab_double,
         copper_ore,
         deepslate_coal_ore,
         deepslate_iron_ore,
         deepslate_gold_ore,
         deepslate_redstone_ore,
         deepslate_lapis_ore,
         deepslate_diamond_ore,
         deepslate_emerald_ore,
         deepslate_copper_ore,
         raw_iron,
         raw_gold,
         raw_copper,
         copper_ingot
      )
      .skipDefaultModel(cobbled_deepslate_wall, polished_deepslate_wall, deepslate_brick_wall, deepslate_tile_wall, deepslate_slab_half, deepslate_slab_double)
      .withItemBlockProvider(
         (block, event) -> {
            if (block == deepslate_slab_half) {
               event.getRegistry()
                  .register((Item)new ItemSlab(deepslate_slab_half, deepslate_slab_half, deepslate_slab_double).setRegistryName(block.getRegistryName()));
            } else if (block != deepslate_slab_double) {
               event.getRegistry().register((Item)new ItemBlock(block).setRegistryName(block.getRegistryName()));
            }
         }
      )
      .withModelOverrides(
         event -> {
            registerCustomBlockModel(cobbled_deepslate_wall, "inventory");
            registerCustomBlockModel(polished_deepslate_wall, "inventory");
            registerCustomBlockModel(deepslate_brick_wall, "inventory");
            registerCustomBlockModel(deepslate_tile_wall, "inventory");

            for (BlockModSlab.Variant variant : BlockModSlab.Variant.values()) {
               ModelLoader.setCustomModelResourceLocation(
                  Item.func_150898_a(deepslate_slab_half),
                  variant.getMetadata(),
                  new ModelResourceLocation("depthsupdate:deepslate_slab_" + variant.func_176610_l(), "inventory")
               );
            }
         }
      )
      .withInit(() -> {
         OreDictionary.registerOre("stoneDeepslate", deepslate);
         OreDictionary.registerOre("cobblestoneDeepslate", cobbled_deepslate);
         OreDictionary.registerOre("cobblestone", cobbled_deepslate);
         GameRegistry.addSmelting(cobbled_deepslate, new ItemStack(deepslate), 0.1F);
         GameRegistry.addSmelting(deepslate_bricks, new ItemStack(cracked_deepslate_bricks), 0.1F);
         GameRegistry.addSmelting(deepslate_tiles, new ItemStack(cracked_deepslate_tiles), 0.1F);
         GameRegistry.addSmelting(raw_iron, new ItemStack(Items.field_151042_j), 0.7F);
         GameRegistry.addSmelting(raw_gold, new ItemStack(Items.field_151043_k), 1.0F);
         GameRegistry.addSmelting(raw_copper, new ItemStack(copper_ingot), 0.7F);
         GameRegistry.addSmelting(copper_ore, new ItemStack(copper_ingot), 0.7F);
         GameRegistry.addSmelting(deepslate_coal_ore, new ItemStack(Items.field_151044_h), 0.1F);
         GameRegistry.addSmelting(deepslate_iron_ore, new ItemStack(Items.field_151042_j), 0.7F);
         GameRegistry.addSmelting(deepslate_gold_ore, new ItemStack(Items.field_151043_k), 1.0F);
         GameRegistry.addSmelting(deepslate_redstone_ore, new ItemStack(Items.field_151137_ax), 0.7F);
         GameRegistry.addSmelting(deepslate_lapis_ore, new ItemStack(Items.field_151100_aR, 1, 4), 0.2F);
         GameRegistry.addSmelting(deepslate_diamond_ore, new ItemStack(Items.field_151045_i), 1.0F);
         GameRegistry.addSmelting(deepslate_emerald_ore, new ItemStack(Items.field_151166_bC), 1.0F);
         GameRegistry.addSmelting(deepslate_copper_ore, new ItemStack(copper_ingot), 0.7F);
         copper_ore.setHarvestLevel("pickaxe", 1);
         deepslate_coal_ore.setHarvestLevel("pickaxe", 0);
         deepslate_iron_ore.setHarvestLevel("pickaxe", 1);
         deepslate_gold_ore.setHarvestLevel("pickaxe", 2);
         deepslate_redstone_ore.setHarvestLevel("pickaxe", 2);
         deepslate_lapis_ore.setHarvestLevel("pickaxe", 1);
         deepslate_diamond_ore.setHarvestLevel("pickaxe", 2);
         deepslate_emerald_ore.setHarvestLevel("pickaxe", 2);
         deepslate_copper_ore.setHarvestLevel("pickaxe", 1);
         raw_iron_block.setHarvestLevel("pickaxe", 1);
         raw_copper_block.setHarvestLevel("pickaxe", 1);
         raw_gold_block.setHarvestLevel("pickaxe", 2);
         BlockUtils.initializeOreMap();
      });
   public static final RegistrationFeature DRIPSTONE_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableDripstoneBlock)
      .add(dripstone_block, pointed_dripstone);
   public static final RegistrationFeature RAW_ORE_BLOCK_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableRawOreBlocks)
      .add(raw_iron_block, raw_gold_block, raw_copper_block);
   public static final RegistrationFeature CALCITE_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableCalcite).add(calcite);
   public static final RegistrationFeature TUFF_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableTuff).add(tuff);
   public static final RegistrationFeature SMOOTH_BASALT_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableSmoothBasalt)
      .add(smooth_basalt);

   private static void registerCustomBlockModel(Block block, String variant) {
      if (block.getRegistryName() != null) {
         ModelLoader.setCustomModelResourceLocation(Item.func_150898_a(block), 0, new ModelResourceLocation(block.getRegistryName(), variant));
      }
   }
}
