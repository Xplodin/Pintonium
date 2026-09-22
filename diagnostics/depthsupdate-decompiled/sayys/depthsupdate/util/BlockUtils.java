package sayys.depthsupdate.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.registry.DeepslateRegistry;

public class BlockUtils {
   private static final Logger LOGGER = LogManager.getLogger("DepthsUpdate/BlockUtils");
   private static IBlockState cachedDeepslateBlockState;
   private static IBlockState cachedCheeseDebugBlockState;
   private static IBlockState cachedSpaghettiDebugBlockState;
   private static IBlockState cachedRiverDebugBlockState;
   private static final Map<Block, Block> DEEPSLATE_ORE_MAP = new HashMap<>();
   private static final Map<Block, Boolean> DEEPSLATE_LOOKUP_CACHE = new ConcurrentHashMap<>();
   private static int deepslateOreID = -1;

   private BlockUtils() {
   }

   public static void initializeOreMap() {
      DEEPSLATE_ORE_MAP.clear();
      DEEPSLATE_ORE_MAP.put(Blocks.field_150365_q, DeepslateRegistry.deepslate_coal_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150366_p, DeepslateRegistry.deepslate_iron_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150352_o, DeepslateRegistry.deepslate_gold_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150450_ax, DeepslateRegistry.deepslate_redstone_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150439_ay, DeepslateRegistry.deepslate_redstone_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150369_x, DeepslateRegistry.deepslate_lapis_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150482_ag, DeepslateRegistry.deepslate_diamond_ore);
      DEEPSLATE_ORE_MAP.put(Blocks.field_150412_bA, DeepslateRegistry.deepslate_emerald_ore);
      DEEPSLATE_ORE_MAP.put(DeepslateRegistry.copper_ore, DeepslateRegistry.deepslate_copper_ore);
   }

   public static void clearCaches() {
      cachedDeepslateBlockState = null;
      cachedCheeseDebugBlockState = null;
      cachedSpaghettiDebugBlockState = null;
      cachedRiverDebugBlockState = null;
      DEEPSLATE_LOOKUP_CACHE.clear();
      deepslateOreID = -1;
   }

   public static boolean isRegistered(Block block) {
      return block != null && block.getRegistryName() != null && Block.field_149771_c.func_148741_d(block.getRegistryName());
   }

   public static IBlockState parseBlockState(String spec) {
      BlockSpec parsed = BlockSpec.parse(spec);
      Block block = Block.func_149684_b(parsed.name());
      if (block == null || block == Blocks.field_150350_a) {
         return null;
      } else if (!parsed.hasMeta()) {
         return block.func_176223_P();
      } else {
         IBlockState state;
         try {
            state = block.func_176203_a(parsed.meta());
         } catch (RuntimeException var5) {
            LOGGER.warn("Block {} rejected metadata {}, using its default state", parsed.name(), parsed.meta());
            return block.func_176223_P();
         }

         if (block.func_176201_c(state) != parsed.meta()) {
            LOGGER.warn("Block {} has no metadata {}, using its default state", parsed.name(), parsed.meta());
            return block.func_176223_P();
         } else {
            return state;
         }
      }
   }

   public static IBlockState getDeepslateBlockState() {
      if (cachedDeepslateBlockState != null) {
         return cachedDeepslateBlockState;
      } else {
         IBlockState configured = parseBlockState(DepthsUpdateConfig.deepslateBlock);
         if (configured == null) {
            cachedDeepslateBlockState = DepthsUpdateConfig.REGISTRY.enableDeepslateFamily
               ? DeepslateRegistry.deepslate.func_176223_P()
               : Blocks.field_150348_b.func_176223_P();
         } else {
            cachedDeepslateBlockState = configured;
         }

         return cachedDeepslateBlockState;
      }
   }

   public static IBlockState getDebugBlockState(String blockName, Block fallback, IBlockState currentCache) {
      if (currentCache != null) {
         return currentCache;
      } else {
         IBlockState state = parseBlockState(blockName);
         return state != null ? state : fallback.func_176223_P();
      }
   }

   public static IBlockState getCheeseDebugBlockState() {
      cachedCheeseDebugBlockState = getDebugBlockState(DepthsUpdateConfig.DEBUG.cheeseDebugBlock, Blocks.field_150360_v, cachedCheeseDebugBlockState);
      return cachedCheeseDebugBlockState;
   }

   public static IBlockState getSpaghettiDebugBlockState() {
      cachedSpaghettiDebugBlockState = getDebugBlockState(DepthsUpdateConfig.DEBUG.spaghettiDebugBlock, Blocks.field_150359_w, cachedSpaghettiDebugBlockState);
      return cachedSpaghettiDebugBlockState;
   }

   public static IBlockState getRiverDebugBlockState() {
      cachedRiverDebugBlockState = getDebugBlockState(DepthsUpdateConfig.DEBUG.riverDebugBlock, Blocks.field_150426_aN, cachedRiverDebugBlockState);
      return cachedRiverDebugBlockState;
   }

   public static IBlockState getDeepslateVariant(IBlockState oreState) {
      Block ore = oreState.func_177230_c();
      Block deepVariant = DEEPSLATE_ORE_MAP.get(ore);
      return deepVariant != null ? deepVariant.func_176223_P() : oreState;
   }

   public static boolean isBaseStone(IBlockState state) {
      if (state == null) {
         return false;
      } else {
         Block block = state.func_177230_c();
         return block == Blocks.field_150348_b || block == DeepslateRegistry.tuff || isDeepslate(state);
      }
   }

   public static boolean isDeepslate(IBlockState state) {
      if (state == null) {
         return false;
      } else {
         Block block = state.func_177230_c();
         IBlockState configured = getDeepslateBlockState();
         Block configuredBlock = configured.func_177230_c();
         if (block != configuredBlock) {
            Boolean cached = DEEPSLATE_LOOKUP_CACHE.get(block);
            if (cached != null) {
               return cached;
            } else {
               boolean result = computeIsDeepslate(block);
               DEEPSLATE_LOOKUP_CACHE.put(block, result);
               return result;
            }
         } else {
            return configured == configuredBlock.func_176223_P() || state == configured;
         }
      }
   }

   private static boolean computeIsDeepslate(Block block) {
      if (block == DeepslateRegistry.deepslate) {
         return true;
      } else {
         if (deepslateOreID == -1) {
            deepslateOreID = OreDictionary.getOreID("stoneDeepslate");
         }

         if (deepslateOreID != -1) {
            Item item = Item.func_150898_a(block);
            if (item != Items.field_190931_a) {
               int[] ids = OreDictionary.getOreIDs(new ItemStack(item));

               for (int id : ids) {
                  if (id == deepslateOreID) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }
}
