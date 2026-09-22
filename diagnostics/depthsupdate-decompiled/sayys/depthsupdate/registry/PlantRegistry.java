package sayys.depthsupdate.registry;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.block.BlockAzalea;
import sayys.depthsupdate.block.BlockBigDripleaf;
import sayys.depthsupdate.block.BlockBigDripleafStem;
import sayys.depthsupdate.block.BlockCaveVines;
import sayys.depthsupdate.block.BlockCaveVinesPlant;
import sayys.depthsupdate.block.BlockHangingRoots;
import sayys.depthsupdate.block.BlockModLeaves;
import sayys.depthsupdate.block.BlockMossCarpet;
import sayys.depthsupdate.block.BlockSimple;
import sayys.depthsupdate.block.BlockSmallDripleaf;
import sayys.depthsupdate.block.BlockSporeBlossom;
import sayys.depthsupdate.item.ItemGlowBerries;

public class PlantRegistry {
   public static final Block moss_block = new BlockSimple("moss_block", Material.field_151577_b, 0.1F, 0.1F, SoundType.field_185850_c);
   public static final Block moss_carpet = new BlockMossCarpet();
   public static final Block rooted_dirt = new BlockSimple("rooted_dirt", Material.field_151578_c, 0.5F, 0.5F, SoundType.field_185849_b);
   public static final Block azalea_leaves = new BlockModLeaves("azalea_leaves");
   public static final Block flowering_azalea_leaves = new BlockModLeaves("flowering_azalea_leaves");
   public static final Block azalea = new BlockAzalea("azalea");
   public static final Block flowering_azalea = new BlockAzalea("flowering_azalea");
   public static final Block hanging_roots = new BlockHangingRoots();
   public static final Block spore_blossom = new BlockSporeBlossom();
   public static final Block small_dripleaf = new BlockSmallDripleaf();
   public static final Block big_dripleaf = new BlockBigDripleaf();
   public static final Block big_dripleaf_stem = new BlockBigDripleafStem();
   public static final Block cave_vines = new BlockCaveVines();
   public static final Block cave_vines_plant = new BlockCaveVinesPlant();
   public static final Item glow_berries = new ItemGlowBerries();
   public static final RegistrationFeature MOSS_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableMossFamily)
      .add(moss_block, moss_carpet);
   public static final RegistrationFeature ROOTED_DIRT_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableRootedDirt).add(rooted_dirt);
   public static final RegistrationFeature AZALEA_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableAzaleaFamily)
      .add(azalea_leaves, flowering_azalea_leaves, azalea, flowering_azalea, hanging_roots);
   public static final RegistrationFeature SPORE_BLOSSOM_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableSporeBlossom)
      .add(spore_blossom);
   public static final RegistrationFeature DRIPLEAF_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableDripleafFamily)
      .add(small_dripleaf, big_dripleaf, big_dripleaf_stem)
      .skipDefaultModel(big_dripleaf_stem)
      .withItemBlockProvider((block, event) -> {
         if (block != big_dripleaf_stem) {
            event.getRegistry().register((Item)new ItemBlock(block).setRegistryName(block.getRegistryName()));
         }
      });
   public static final RegistrationFeature VINE_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableCaveVinesAndBerries)
      .add(cave_vines, cave_vines_plant, glow_berries)
      .skipDefaultModel(cave_vines_plant, cave_vines)
      .withItemBlockProvider((block, event) -> {
         if (block != cave_vines_plant && block != cave_vines) {
            event.getRegistry().register((Item)new ItemBlock(block).setRegistryName(block.getRegistryName()));
         }
      });
}
