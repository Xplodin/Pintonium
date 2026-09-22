package com.yungnickyoung.minecraft.bettercaves.event;

import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventConfigReload {
   @SubscribeEvent
   public void onConfigReload(OnConfigChangedEvent event) {
      if ("bettercaves".equals(event.getModID())) {
         ConfigManager.sync("bettercaves", Type.INSTANCE);
      }
   }
}
