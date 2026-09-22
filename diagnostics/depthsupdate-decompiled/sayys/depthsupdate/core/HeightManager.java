package sayys.depthsupdate.core;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sayys.depthsupdate.DepthsUpdateConfig;

public final class HeightManager {
   private static final Logger LOGGER = LogManager.getLogger("DepthsUpdate/HeightManager");
   private static volatile Map<Integer, HeightContext> contexts = Map.of();
   private static volatile HeightContext maxContext = HeightContext.VANILLA;
   private static volatile boolean initialized = false;
   private static final Object INIT_LOCK = new Object();

   private HeightManager() {
   }

   public static void initialize() {
      synchronized (INIT_LOCK) {
         DepthsUpdateConfig.HeightExtension cfg = DepthsUpdateConfig.heightExtension;
         int globalMinY = roundToMultipleOf16(cfg.globalMinY, "globalMinY", false);
         int globalMaxY = roundToMultipleOf16(cfg.globalMaxY, "globalMaxY", true);
         if (globalMinY >= globalMaxY) {
            LOGGER.error("globalMinY ({}) must be less than globalMaxY ({}), using defaults", globalMinY, globalMaxY);
            globalMinY = -64;
            globalMaxY = 320;
         }

         int seaLevel = cfg.seaLevel;
         int lavaLevel = cfg.lavaLevel;
         int voidDamageLevel = cfg.voidDamageLevel;

         HeightContext globalContext;
         try {
            globalContext = new HeightContext(globalMinY, globalMaxY, lavaLevel, voidDamageLevel, seaLevel);
         } catch (IllegalArgumentException var15) {
            LOGGER.error("Invalid height configuration ({}), falling back to -64..320", var15.getMessage());
            globalContext = new HeightContext(-64, 320, lavaLevel, voidDamageLevel, seaLevel);
         }

         Map<Integer, HeightContext> newContexts = new HashMap<>();
         HeightContext largest = globalContext;

         for (int dimId : cfg.extendedDimensions) {
            newContexts.put(dimId, globalContext);
         }

         for (String override : cfg.dimensionOverrides) {
            parseOverride(override, lavaLevel, voidDamageLevel, seaLevel, newContexts);
         }

         for (HeightContext ctx : newContexts.values()) {
            if (ctx.primerArraySize() > largest.primerArraySize()) {
               largest = ctx;
            }
         }

         contexts = Map.copyOf(newContexts);
         maxContext = largest;
         initialized = true;
         LOGGER.info("HeightManager initialized: {} extended dimension(s), max context: {}", newContexts.size(), largest);
      }
   }

   public static void ensureInitialized() {
      if (!initialized) {
         initialize();
      }
   }

   public static HeightContext get(World world) {
      return world == null ? HeightContext.VANILLA : get(world.field_73011_w.getDimension());
   }

   public static HeightContext get(int dimensionId) {
      ensureInitialized();
      return contexts.getOrDefault(dimensionId, HeightContext.VANILLA);
   }

   public static boolean isExtended(World world) {
      return world != null && isExtended(world.field_73011_w.getDimension());
   }

   public static boolean isExtended(int dimensionId) {
      ensureInitialized();
      return contexts.containsKey(dimensionId);
   }

   public static HeightContext getMaxContext() {
      ensureInitialized();
      return maxContext;
   }

   public static int getMinY(World world) {
      return get(world).minY();
   }

   public static int getMaxY(World world) {
      return get(world).maxY();
   }

   public static int getLavaLevel(World world) {
      return get(world).lavaLevel();
   }

   public static int getVoidDamageLevel(World world) {
      return get(world).voidDamageLevel();
   }

   private static int roundToMultipleOf16(int value, String name, boolean roundUp) {
      int rounded = roundUp ? -(-value >> 4 << 4) : value >> 4 << 4;
      if (rounded != value) {
         LOGGER.warn("{} ({}) is not a multiple of 16, rounding to {}", name, value, rounded);
      }

      return rounded;
   }

   private static void parseOverride(String override, int defaultLavaLevel, int defaultVoidDamageLevel, int defaultSeaLevel, Map<Integer, HeightContext> map) {
      String[] parts = override.split(":");
      if (parts.length != 3 && parts.length != 5) {
         LOGGER.error("Invalid dimension override format: '{}' (expected 'dimId:minY:maxY' or 'dimId:minY:maxY:lavaLevel:voidDamageLevel')", override);
      } else {
         try {
            int dimId = Integer.parseInt(parts[0].trim());
            int minY = roundToMultipleOf16(Integer.parseInt(parts[1].trim()), "override minY for dim " + dimId, false);
            int maxY = roundToMultipleOf16(Integer.parseInt(parts[2].trim()), "override maxY for dim " + dimId, true);
            int lava = parts.length >= 5 ? Integer.parseInt(parts[3].trim()) : defaultLavaLevel;
            int voidDmg = parts.length >= 5 ? Integer.parseInt(parts[4].trim()) : defaultVoidDamageLevel;
            if (minY >= maxY) {
               LOGGER.error("Override for dim {}: minY ({}) must be less than maxY ({})", dimId, minY, maxY);
               return;
            }

            HeightContext ctx = new HeightContext(minY, maxY, lava, voidDmg, defaultSeaLevel);
            map.put(dimId, ctx);
            LOGGER.info("Registered height override for dimension {}: {}", dimId, ctx);
         } catch (NumberFormatException var12) {
            LOGGER.error("Invalid number in dimension override: '{}'", override, var12);
         } catch (IllegalArgumentException var13) {
            LOGGER.error("Invalid height context for override '{}': {}", override, var13.getMessage());
         }
      }
   }
}
