package com.yungnickyoung.minecraft.bettercaves.config.ravine;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

public class ConfigRavine {
   @Name("Enable Ravines")
   @Comment("Set to true to enable ravine generation.\nDefault: true")
   public boolean enableVanillaRavines = true;
   @Name("Enable Flooded Ravines")
   @Comment("Set to true to enable flooded ravines in ocean biomes.\nDefault: true")
   public boolean enableFloodedRavines = true;
}
