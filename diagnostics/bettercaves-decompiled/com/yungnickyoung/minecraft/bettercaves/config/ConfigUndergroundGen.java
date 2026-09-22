package com.yungnickyoung.minecraft.bettercaves.config;

import com.yungnickyoung.minecraft.bettercaves.config.cave.ConfigCaves;
import com.yungnickyoung.minecraft.bettercaves.config.cavern.ConfigCaverns;
import com.yungnickyoung.minecraft.bettercaves.config.ravine.ConfigRavine;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

public class ConfigUndergroundGen {
   @Name("Caves")
   @Comment("Settings used in the generation of caves.")
   public ConfigCaves caves = new ConfigCaves();
   @Name("Caverns")
   @Comment("Settings used in the generation of caverns. Caverns are spacious caves at low altitudes.")
   public ConfigCaverns caverns = new ConfigCaverns();
   @Name("Water Regions")
   @Comment("Settings used in the generation of water regions.")
   public ConfigWaterRegions waterRegions = new ConfigWaterRegions();
   @Name("Ravines")
   @Comment("Settings used for ravine generation.")
   public ConfigRavine ravines = new ConfigRavine();
   @Name("Miscellaneous")
   @Comment("Miscellaneous settings used in cave and cavern generation.")
   public ConfigMisc miscellaneous = new ConfigMisc();
}
