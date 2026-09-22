package com.yungnickyoung.minecraft.bettercaves;

import com.yungnickyoung.minecraft.bettercaves.event.EventBetterCaveGen;
import com.yungnickyoung.minecraft.bettercaves.proxy.IProxy;
import java.io.File;
import java.io.IOException;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
   modid = "bettercaves",
   name = "YUNG's Better Caves",
   version = "1.12.2",
   useMetadata = true,
   acceptableRemoteVersions = "*",
   acceptedMinecraftVersions = "[1.12.2]"
)
public class BetterCaves {
   public static final Logger LOGGER = LogManager.getLogger("bettercaves");
   public static File customConfigDir;
   @SidedProxy(
      clientSide = "com.yungnickyoung.minecraft.bettercaves.proxy.ClientProxy",
      serverSide = "com.yungnickyoung.minecraft.bettercaves.proxy.ServerProxy"
   )
   public static IProxy proxy;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      proxy.preInit();
      customConfigDir = new File(Loader.instance().getConfigDir(), "bettercaves-1_12_2");

      try {
         String filePath = customConfigDir.getCanonicalPath();
         if (customConfigDir.mkdir()) {
            LOGGER.info("Creating directory for dimension-specific Better Caves configs at " + filePath);
         }
      } catch (IOException var3) {
         LOGGER.warn("ERROR creating Better Caves config directory.");
      }
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      MinecraftForge.TERRAIN_GEN_BUS.register(new EventBetterCaveGen());
   }
}
