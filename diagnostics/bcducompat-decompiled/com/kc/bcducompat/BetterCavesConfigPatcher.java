package com.kc.bcducompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public final class BetterCavesConfigPatcher {
   private static final Set<Integer> LOGGED_DIMENSIONS = new HashSet<>();

   private BetterCavesConfigPatcher() {
   }

   public static synchronized void apply(int var0, Object var1) {
      CompatConfig.reload();
      if (var1 != null && CompatConfig.appliesTo(var0)) {
         try {
            setOption(var1, "flooredCavernBottom", CompatConfig.flooredCavernMinY);
            setOption(var1, "flooredCavernTop", CompatConfig.flooredCavernMaxY);
            setOption(var1, "liquidAltitude", CompatConfig.liquidAltitude);
            setOption(var1, "cavernSpawnChance", CompatConfig.cavernSpawnChance);
            setOption(var1, "flooredCavernXZCompression", CompatConfig.horizontalCompression);
            setOption(var1, "flooredCavernYCompression", CompatConfig.verticalCompression);
            setOption(var1, "flooredCavernPriority", CompatConfig.flooredPriority);
            setOption(var1, "liquidCavernPriority", CompatConfig.liquidPriority);
            if (CompatConfig.forceExtraLargeRegions) {
               setEnumOption(var1, "cavernRegionSize", "ExtraLarge");
            }

            if (CompatConfig.forceOverrideSurfaceDetection) {
               setOption(var1, "overrideSurfaceDetection", Boolean.TRUE);
            }

            if (CompatConfig.disableBedrockFlattening) {
               setOption(var1, "flattenBedrock", Boolean.FALSE);
            }

            if (LOGGED_DIMENSIONS.add(var0)) {
               System.out
                  .println(
                     "[BCDU Compat] Enabled Better Caves floored caverns in dimension "
                        + var0
                        + " from Y "
                        + CompatConfig.flooredCavernMinY
                        + " to "
                        + CompatConfig.flooredCavernMaxY
                        + " (chance "
                        + CompatConfig.cavernSpawnChance
                        + "%)."
                  );
            }
         } catch (ReflectiveOperationException var3) {
            throw new IllegalStateException("BCDU Compat supports Better Caves 1.12.2-2.0.4. Its ConfigHolder layout did not match.", var3);
         }
      }
   }

   private static void setOption(Object var0, String var1, Object var2) throws ReflectiveOperationException {
      Field var3 = var0.getClass().getField(var1);
      Object var4 = var3.get(var0);
      Method var5 = var4.getClass().getMethod("set", Object.class);
      var5.invoke(var4, var2);
   }

   private static void setEnumOption(Object var0, String var1, String var2) throws ReflectiveOperationException {
      Field var3 = var0.getClass().getField(var1);
      Object var4 = var3.get(var0);
      Method var5 = var4.getClass().getMethod("get");
      Object var6 = var5.invoke(var4);
      if (!(var6 instanceof Enum)) {
         throw new IllegalStateException(var1 + " was not an enum option");
      } else {
         Enum var7 = Enum.valueOf((Class<Enum>)var6.getClass(), var2);
         Method var8 = var4.getClass().getMethod("set", Object.class);
         var8.invoke(var4, var7);
      }
   }
}
