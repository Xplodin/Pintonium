package com.yungnickyoung.minecraft.bettercaves.config;

public class BCSettings {
   public static final boolean USE_META_DATA = true;
   public static final String MOD_ID = "bettercaves";
   public static final String NAME = "YUNG's Better Caves";
   public static final String VERSION = "1.12.2";
   public static final String SERVER_PROXY = "com.yungnickyoung.minecraft.bettercaves.proxy.ServerProxy";
   public static final String CLIENT_PROXY = "com.yungnickyoung.minecraft.bettercaves.proxy.ClientProxy";
   public static final String CUSTOM_CONFIG_PATH = "bettercaves-1_12_2";
   public static final int SUB_CHUNK_SIZE = 4;
   public static final float[] START_COEFFS = new float[4];
   public static final float[] END_COEFFS = new float[4];

   private BCSettings() {
   }

   static {
      for (int n = 0; n < 4; n++) {
         START_COEFFS[n] = (3 - n) / 3.0F;
         END_COEFFS[n] = n / 3.0F;
      }
   }
}
