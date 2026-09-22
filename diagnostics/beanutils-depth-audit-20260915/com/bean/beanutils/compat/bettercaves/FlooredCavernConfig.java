package com.bean.beanutils.compat.bettercaves;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public final class FlooredCavernConfig {
   private static final File CONFIG_FILE = new File("config", "bettercaves-depthsupdate-compat.cfg");
   public static boolean enabled = true;
   public static boolean allDimensions = false;
   public static Set<Integer> targetDimensions = Collections.singleton(0);
   public static int flooredCavernMinY = -120;
   public static int flooredCavernMaxY = -8;
   public static int liquidAltitude = -128;
   public static float cavernSpawnChance = 65.0F;
   public static float horizontalCompression = 0.35F;
   public static float verticalCompression = 0.55F;
   public static int flooredPriority = 10;
   public static int liquidPriority = 0;
   public static boolean forceExtraLargeRegions = true;
   public static boolean forceOverrideSurfaceDetection = true;
   public static boolean disableBedrockFlattening = true;

   private FlooredCavernConfig() {
   }

   public static synchronized void reload() {
      ensureExists();
      Properties var0 = new Properties();

      try {
         BufferedInputStream var1 = new BufferedInputStream(new FileInputStream(CONFIG_FILE));

         try {
            var0.load(var1);
         } catch (Throwable var5) {
            try {
               var1.close();
            } catch (Throwable var4) {
               var5.addSuppressed(var4);
            }

            throw var5;
         }

         var1.close();
      } catch (IOException var61) {
         System.err.println("[BeanUtilities] Failed to read " + CONFIG_FILE + ": " + var61.getMessage());
         return;
      }

      enabled = getBoolean(var0, "enabled", enabled);
      parseDimensions(var0.getProperty("targetDimensions", "0"));
      flooredCavernMinY = clamp(getInt(var0, "flooredCavernMinY", flooredCavernMinY), -2048, 2047);
      flooredCavernMaxY = clamp(getInt(var0, "flooredCavernMaxY", flooredCavernMaxY), -2048, 2047);
      if (flooredCavernMaxY <= flooredCavernMinY) {
         flooredCavernMaxY = Math.min(2047, flooredCavernMinY + 16);
      }

      liquidAltitude = clamp(getInt(var0, "liquidAltitude", liquidAltitude), -2048, 2047);
      if (liquidAltitude > flooredCavernMinY) {
         liquidAltitude = flooredCavernMinY;
      }

      cavernSpawnChance = clamp(getFloat(var0, "cavernSpawnChance", cavernSpawnChance), 0.0F, 100.0F);
      horizontalCompression = clamp(getFloat(var0, "horizontalCompression", horizontalCompression), 0.01F, 10.0F);
      verticalCompression = clamp(getFloat(var0, "verticalCompression", verticalCompression), 0.01F, 10.0F);
      flooredPriority = clamp(getInt(var0, "flooredPriority", flooredPriority), 0, 100);
      liquidPriority = clamp(getInt(var0, "liquidPriority", liquidPriority), 0, 100);
      forceExtraLargeRegions = getBoolean(var0, "forceExtraLargeRegions", forceExtraLargeRegions);
      forceOverrideSurfaceDetection = getBoolean(var0, "forceOverrideSurfaceDetection", forceOverrideSurfaceDetection);
      disableBedrockFlattening = getBoolean(var0, "disableBedrockFlattening", disableBedrockFlattening);
   }

   public static boolean appliesTo(int var0) {
      return enabled && (allDimensions || targetDimensions.contains(var0));
   }

   private static void ensureExists() {
      if (!CONFIG_FILE.isFile()) {
         File var0 = CONFIG_FILE.getParentFile();
         if (var0 != null && !var0.isDirectory() && !var0.mkdirs()) {
            System.err.println("[BeanUtilities] Could not create config directory: " + var0);
         } else {
            try {
               BufferedWriter var1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8));

               try {
                  var1.write("# Better Caves 2.0.4 x DepthsUpdate negative-Y cavern compatibility\n");
                  var1.write("# Changes apply when Better Caves creates its per-dimension ConfigHolder.\n\n");
                  var1.write("enabled=true\n");
                  var1.write("# Comma-separated dimension IDs, or 'all'.\n");
                  var1.write("targetDimensions=0\n\n");
                  var1.write("flooredCavernMinY=-120\n");
                  var1.write("flooredCavernMaxY=-8\n");
                  var1.write("liquidAltitude=-128\n\n");
                  var1.write("# 0-100. Higher values make cavern regions more common.\n");
                  var1.write("cavernSpawnChance=65.0\n");
                  var1.write("# Lower compression values make cavern shapes broader/larger.\n");
                  var1.write("horizontalCompression=0.35\n");
                  var1.write("verticalCompression=0.55\n\n");
                  var1.write("flooredPriority=10\n");
                  var1.write("# 0 disables liquid caverns while keeping floored caverns.\n");
                  var1.write("liquidPriority=0\n");
                  var1.write("forceExtraLargeRegions=true\n");
                  var1.write("forceOverrideSurfaceDetection=true\n");
                  var1.write("disableBedrockFlattening=true\n");
               } catch (Throwable var5) {
                  try {
                     var1.close();
                  } catch (Throwable var4) {
                     var5.addSuppressed(var4);
                  }

                  throw var5;
               }

               var1.close();
            } catch (IOException var61) {
               System.err.println("[BeanUtilities] Failed to create default config: " + var61.getMessage());
            }
         }
      }
   }

   private static void parseDimensions(String var0) {
      String var1 = var0 == null ? "0" : var0.trim();
      if (!var1.equalsIgnoreCase("all") && !var1.equals("*")) {
         allDimensions = false;
         HashSet<Integer> var2 = new HashSet<>();

         for (String var6 : var1.split(",")) {
            try {
               var2.add(Integer.parseInt(var6.trim()));
            } catch (NumberFormatException var8) {
               System.err.println("[BeanUtilities] Ignoring invalid dimension ID: " + var6);
            }
         }

         if (var2.isEmpty()) {
            var2.add(0);
         }

         targetDimensions = Collections.unmodifiableSet(var2);
      } else {
         allDimensions = true;
         targetDimensions = Collections.emptySet();
      }
   }

   private static boolean getBoolean(Properties var0, String var1, boolean var2) {
      String var3 = var0.getProperty(var1);
      return var3 == null ? var2 : Boolean.parseBoolean(var3.trim());
   }

   private static int getInt(Properties var0, String var1, int var2) {
      String var3 = var0.getProperty(var1);
      if (var3 == null) {
         return var2;
      } else {
         try {
            return Integer.parseInt(var3.trim());
         } catch (NumberFormatException var5) {
            return var2;
         }
      }
   }

   private static float getFloat(Properties var0, String var1, float var2) {
      String var3 = var0.getProperty(var1);
      if (var3 == null) {
         return var2;
      } else {
         try {
            return Float.parseFloat(var3.trim());
         } catch (NumberFormatException var5) {
            return var2;
         }
      }
   }

   private static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   private static float clamp(float var0, float var1, float var2) {
      return Math.max(var1, Math.min(var2, var0));
   }
}
