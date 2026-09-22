package com.bean.beanutils.compat.bettercaves;

import com.bean.beanutils.config.DepthUpdateCompat;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class BetterCavesDepthPatcher {
   private static final Logger LOG = LogManager.getLogger("beanutils");
   private static final Set<Integer> LOGGED_DIMENSIONS = new HashSet<>();

   private BetterCavesDepthPatcher() {
   }

   public static synchronized void apply(int dimension, Object configHolder) {
      if (configHolder != null && dimension == 0) {
         int caveBottom = Math.min(-1, DepthUpdateCompat.getOverworldMinY() + 8);

         try {
            setOption(configHolder, "cubicCaveBottom", caveBottom);
            setOption(configHolder, "simplexCaveBottom", caveBottom);
            if (LOGGED_DIMENSIONS.add(dimension)) {
               LOG.info("Extended Better Caves Type 1 and Type 2 caves to Y={} in dimension {}", caveBottom, dimension);
            }
         } catch (ReflectiveOperationException var4) {
            throw new IllegalStateException("BeanUtilities supports Better Caves 1.12.2-2.0.4, but its ConfigHolder layout did not match.", var4);
         }
      }
   }

   private static void setOption(Object holder, String fieldName, Object value) throws ReflectiveOperationException {
      Field field = holder.getClass().getField(fieldName);
      Object option = field.get(holder);
      Method setter = option.getClass().getMethod("set", Object.class);
      setter.invoke(option, value);
   }
}
