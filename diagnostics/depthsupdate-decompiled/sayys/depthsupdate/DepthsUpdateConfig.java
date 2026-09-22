package sayys.depthsupdate;

import com.cleanroommc.configanytime.ConfigAnytime;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;

@Config(modid = "depthsupdate")
public class DepthsUpdateConfig {
   @Name("Debug")
   @Comment("Debug Settings")
   public static final DepthsUpdateConfig.Debug DEBUG = new DepthsUpdateConfig.Debug();
   @Name("Height Extension")
   @Comment("Settings for extended world height.")
   public static final DepthsUpdateConfig.HeightExtension heightExtension = new DepthsUpdateConfig.HeightExtension();
   @Name("Generate Underground Rivers")
   public static boolean generateUndergroundRivers = false;
   @Name("Generate Cheese Caves")
   public static boolean generateCheeseCaves = true;
   @Name("Cheese Caves Size")
   @RangeDouble(min = 0.25, max = 2.0)
   public static double cheeseCavesAbundance = 1.0;
   @Name("Generate Spaghetti Caves")
   public static boolean generateSpaghettiCaves = true;
   @Name("Generate Noodle Caves")
   public static boolean generateNoodleCaves = true;
   @Name("Generate Cave Entrances")
   public static boolean generateCaveEntrances = true;
   @Name("Generate Cave Pillars")
   public static boolean generateCavePillars = true;
   @Name("Deepslate Max Y")
   @RangeInt(min = -256, max = 512)
   public static int deepslateMaxY = 8;
   @Name("Deepslate Transition Range")
   @Comment("The number of blocks over which stone transitions into Deepslate.")
   @RangeInt(min = 0, max = 64)
   public static int deepslateTransitionRange = 8;
   @Name("Deepslate Block")
   @Comment("The registry name of the block to use as 'Deepslate'.")
   public static String deepslateBlock = "depthsupdate:deepslate";
   @Name("Registry")
   @Comment("Registry Toggles for blocks and items.")
   public static final DepthsUpdateConfig.Registry REGISTRY = new DepthsUpdateConfig.Registry();
   @Name("Lush Caves")
   @Comment("Settings related to the Lush Caves biome.")
   public static final DepthsUpdateConfig.LushCaves lushCaves = new DepthsUpdateConfig.LushCaves();
   @Name("Dripstone Caves")
   @Comment("Settings related to the Dripstone Caves biome.")
   public static final DepthsUpdateConfig.DripstoneCaves dripstoneCaves = new DepthsUpdateConfig.DripstoneCaves();
   @Name("Amethyst Geodes")
   public static final DepthsUpdateConfig.AmethystGeodes amethystGeodes = new DepthsUpdateConfig.AmethystGeodes();
   @Name("Aquifers")
   public static final DepthsUpdateConfig.Aquifers aquifers = new DepthsUpdateConfig.Aquifers();

   static {
      ConfigAnytime.register(DepthsUpdateConfig.class);
   }

   public static class AmethystGeodes {
      @Name("Enable Amethyst Geodes")
      public boolean enableAmethystGeodes = true;
      @Name("Geode Rarity")
      @Comment("The rarity of Amethyst Geodes. Higher numbers mean rarer geodes. (1 in X chance per chunk)")
      @RangeInt(min = 1, max = 1000)
      public int geodeRarity = 24;
      @Name("Minimum Height")
      @RangeInt(min = -256, max = 512)
      public int geodeMinY = -58;
      @Name("Maximum Height")
      @RangeInt(min = -256, max = 512)
      public int geodeMaxY = 30;
   }

   public static class Aquifers {
      @Name("Enable Aquifers")
      @RequiresMcRestart
      public boolean enableAquifers = true;
   }

   public static class Debug {
      @Name("Enable Debug Visualizer")
      @Comment("Outlines cave and river borders with distinctive blocks.")
      public boolean enableDebugVisualizers = false;
      @Name("Cheese Cave Debug Block")
      public String cheeseDebugBlock = "minecraft:gold_block";
      @Name("Spaghetti Cave Debug Block")
      public String spaghettiDebugBlock = "minecraft:red_mushroom_block";
      @Name("River Debug Block")
      public String riverDebugBlock = "minecraft:diamond_block";
      @Name("Log Generation")
      public boolean enableGenerationLog = false;
      @Name("Generation Log Interval")
      @RangeInt(min = 1, max = 4096)
      public int generationLogInterval = 64;
      @Name("Log Feature Placements")
      @Comment("Log the coordinates of each lush cave, dripstone cave and amethyst geode as it is placed.")
      public boolean logFeaturePlacements = false;
   }

   public static class DripstoneCaves {
      @Name("Enable Dripstone Caves")
      @Comment("Allow Dripstone Caves to generate underground.")
      public boolean enableDripstoneCaves = true;
      @Name("Dripstone Caves Rarity")
      @Comment("The rarity of Dripstone Caves. Higher numbers mean rarer caves. (1 in X chance per chunk)")
      @RangeInt(min = 1, max = 1000)
      public int dripstoneCavesRarity = 15;
      @Name("Minimum Height")
      @RangeInt(min = -256, max = 512)
      public int dripstoneCavesMinY = -64;
      @Name("Maximum Height")
      @RangeInt(min = -256, max = 512)
      public int dripstoneCavesMaxY = 63;
      @Name("Radius Base Size")
      @RangeInt(min = 1, max = 100)
      public int dripstoneCavesRadiusBase = 20;
      @Name("Radius Variation")
      @RangeInt(min = 0, max = 100)
      public int dripstoneCavesRadiusVariation = 10;
      @Name("Height Base Size")
      @RangeInt(min = 1, max = 100)
      public int dripstoneCavesHeightBase = 12;
      @Name("Height Variation")
      @RangeInt(min = 0, max = 100)
      public int dripstoneCavesHeightVariation = 8;
   }

   @EventBusSubscriber(modid = "depthsupdate")
   private static class EventHandler {
      @SubscribeEvent
      public static void onConfigChanged(OnConfigChangedEvent event) {
         if (event.getModID().equals("depthsupdate")) {
            ConfigManager.sync("depthsupdate", Type.INSTANCE);
            BlockUtils.clearCaches();
            HeightManager.initialize();
         }
      }
   }

   public static class HeightExtension {
      @Name("Global Minimum Y")
      @Comment("The minimum Y coordinate.")
      @RangeInt(min = -256, max = 0)
      @RequiresMcRestart
      public int globalMinY = -64;
      @Name("Global Maximum Y")
      @Comment("The maximum Y coordinate.")
      @RangeInt(min = 256, max = 512)
      @RequiresMcRestart
      public int globalMaxY = 320;
      @Name("Extended Dimensions")
      @Comment("Dimension IDs to apply height extension to. Default: [0].")
      @RequiresMcRestart
      public int[] extendedDimensions = new int[]{0};
      @Name("Dimension Overrides")
      @Comment(
         {
               "Per-dimension height overrides.",
               "Format: \"dimId:minY:maxY\" or \"dimId:minY:maxY:lavaLevel:voidDamageLevel\"",
               "Example: \"-1:-64:256\" extends the Nether to -64..256.",
               "Example: \"0:-64:320:-54:-128\" sets custom lava/void levels for the Overworld.",
               "Overrides globalMinY/globalMaxY (and optionally lava/void levels) for the specified dimension."
         }
      )
      @RequiresMcRestart
      public String[] dimensionOverrides = new String[0];
      @Name("Sea Level")
      @Comment("The sea level Y coordinate.")
      @RangeInt(min = -256, max = 512)
      public int seaLevel = 63;
      @Name("Lava Level")
      @Comment("The Y level at which underground air is replaced with lava.")
      @RangeInt(min = -256, max = 512)
      public int lavaLevel = -54;
      @Name("Void Damage Level")
      @Comment("The Y level at which players start taking void damage.")
      @RangeInt(min = -512, max = 64)
      public int voidDamageLevel = -128;
      @Name("Extend Custom World Types")
      @RequiresMcRestart
      public boolean extendCustomWorldTypes = true;
   }

   public static class LushCaves {
      @Name("Enable Lush Caves")
      @Comment("Allow Lush Caves to generate underground.")
      public boolean enableLushCaves = true;
      @Name("Lush Caves Rarity")
      @Comment("The rarity of Lush Caves. Higher numbers mean rarer caves. (1 in X chance per chunk)")
      @RangeInt(min = 1, max = 1000)
      public int lushCavesRarity = 12;
      @Name("Minimum Height")
      @RangeInt(min = -256, max = 512)
      public int lushCavesMinY = -64;
      @Name("Maximum Height")
      @RangeInt(min = -256, max = 512)
      public int lushCavesMaxY = 63;
      @Name("Radius Base Size")
      @RangeInt(min = 1, max = 100)
      public int lushCavesRadiusBase = 16;
      @Name("Radius Variation")
      @RangeInt(min = 0, max = 100)
      public int lushCavesRadiusVariation = 8;
      @Name("Height Base Size")
      @RangeInt(min = 1, max = 100)
      public int lushCavesHeightBase = 8;
      @Name("Height Variation")
      @RangeInt(min = 0, max = 100)
      public int lushCavesHeightVariation = 6;
   }

   public static class Registry {
      @Name("Enable Deepslate")
      public boolean enableDeepslateFamily = true;
      @Name("Enable Calcite")
      public boolean enableCalcite = true;
      @Name("Enable Tuff")
      public boolean enableTuff = true;
      @Name("Enable Smooth Basalt")
      public boolean enableSmoothBasalt = true;
      @Name("Enable Amethyst")
      public boolean enableAmethystFamily = true;
      @Name("Enable Moss")
      public boolean enableMossFamily = true;
      @Name("Enable Azalea")
      public boolean enableAzaleaFamily = true;
      @Name("Enable Spore Blossom")
      public boolean enableSporeBlossom = true;
      @Name("Enable Dripstone Block")
      public boolean enableDripstoneBlock = true;
      @Name("Enable Dripleaf")
      public boolean enableDripleafFamily = true;
      @Name("Enable Cave Vines and Glow Berries")
      public boolean enableCaveVinesAndBerries = true;
      @Name("Enable Rooted Dirt")
      public boolean enableRootedDirt = true;
      @Name("Enable Raw Ore Blocks")
      public boolean enableRawOreBlocks = true;
      @Name("Enable Spyglass")
      public boolean enableSpyglass = true;
      @Name("Use Custom Creative Tab")
      @RequiresMcRestart
      public boolean useCustomCreativeTab = true;
   }
}
