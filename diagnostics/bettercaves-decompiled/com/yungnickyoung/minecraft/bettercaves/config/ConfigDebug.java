package com.yungnickyoung.minecraft.bettercaves.config;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

public class ConfigDebug {
   @Name("Enable DEBUG Visualizer")
   @Comment(
      "The visualizer creates worlds where there are no blocks except those indicating where caves\n    and caverns would be carved out in a regular world. This is useful for visualizing the kinds of\n    caves and caverns your current config options will create.\n    Type 1 Cave: Wooden Planks\n    Type 2 Cave: Cobblestone\n    Lava Cavern: Redstone Block\n    Floored Cavern: Gold Block\n    Surface Cave: Emerald Block\n    Vanilla Cave: Bricks\nDefault: false"
   )
   public boolean debugVisualizer = false;
}
