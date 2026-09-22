package sayys.depthsupdate.world.generation;

import java.util.concurrent.atomic.LongAdder;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.world.generation.noise.CaveType;

public final class GenerationLog {
   private static final Logger LOGGER = LogManager.getLogger("DepthsUpdate/Worldgen");
   private static final LongAdder[] OPENED_BY = newCounters(CaveType.values().length);
   private static final LongAdder CARVED = new LongAdder();
   private static final LongAdder PILLAR_BLOCKS = new LongAdder();
   private static final LongAdder AQUIFER_WATER = new LongAdder();
   private static final LongAdder AQUIFER_LAVA = new LongAdder();
   private static final LongAdder GROUND_PLUGS = new LongAdder();
   private static final LongAdder CHUNKS = new LongAdder();

   private GenerationLog() {
   }

   private static LongAdder[] newCounters(int length) {
      LongAdder[] counters = new LongAdder[length];

      for (int i = 0; i < length; i++) {
         counters[i] = new LongAdder();
      }

      return counters;
   }

   public static boolean enabled() {
      return DepthsUpdateConfig.DEBUG.enableGenerationLog;
   }

   public static void carved(int openMask) {
      CARVED.increment();

      for (int i = 0; i < OPENED_BY.length; i++) {
         if ((openMask & 1 << i) != 0) {
            OPENED_BY[i].increment();
         }
      }
   }

   public static void pillarBlocked() {
      PILLAR_BLOCKS.increment();
   }

   public static void aquiferWater() {
      AQUIFER_WATER.increment();
   }

   public static void aquiferLava() {
      AQUIFER_LAVA.increment();
   }

   public static void groundPlug() {
      GROUND_PLUGS.increment();
   }

   public static void featurePlaced(String name, BlockPos pos) {
      if (enabled() && DepthsUpdateConfig.DEBUG.logFeaturePlacements) {
         LOGGER.info("{} at {}, {}, {}", name, pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
      }
   }

   public static void chunkGenerated() {
      if (enabled()) {
         CHUNKS.increment();
         int interval = Math.max(1, DepthsUpdateConfig.DEBUG.generationLogInterval);
         if (CHUNKS.sum() % interval == 0L) {
            report();
         }
      }
   }

   public static synchronized void report() {
      long chunks = CHUNKS.sumThenReset();
      if (chunks > 0L) {
         long carvedTotal = CARVED.sumThenReset();
         StringBuilder shares = new StringBuilder();

         for (CaveType type : CaveType.values()) {
            if (shares.length() > 0) {
               shares.append(", ");
            }

            long count = OPENED_BY[type.ordinal()].sumThenReset();
            shares.append(type.label()).append(' ').append(carvedTotal == 0L ? 0L : Math.round(100.0 * count / carvedTotal)).append('%');
         }

         long pillars = PILLAR_BLOCKS.sumThenReset();
         long water = AQUIFER_WATER.sumThenReset();
         long lava = AQUIFER_LAVA.sumThenReset();
         long plugs = GROUND_PLUGS.sumThenReset();
         LOGGER.info(
            "{} chunks | carved {}/chunk ({}) | pillar blocks {}/chunk | aquifer water {} lava {} per chunk | ground plugs {}",
            chunks,
            carvedTotal / chunks,
            shares,
            pillars / chunks,
            water / chunks,
            lava / chunks,
            plugs
         );
      }
   }
}
