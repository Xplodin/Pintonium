package com.kc.bcducompat;

import net.minecraftforge.fml.common.Mod;

@Mod(
   modid = "bcducompat",
   name = "Better Caves DepthsUpdate Compat",
   version = "1.0.6",
   acceptableRemoteVersions = "*",
   acceptedMinecraftVersions = "[1.12.2]",
   dependencies = "required-after:mixinbooter;required-after:bettercaves;required-after:depthsupdate"
)
public final class BCDUCompat {
   public static final String MODID = "bcducompat";
   public static final String NAME = "Better Caves DepthsUpdate Compat";
   public static final String VERSION = "1.0.6";

   public BCDUCompat() {
      CompatConfig.reload();
   }
}
