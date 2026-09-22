package com.yungnickyoung.minecraft.bettercaves.config.util;

import com.yungnickyoung.minecraft.bettercaves.config.Configuration;
import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import java.util.HashMap;
import java.util.Map;

public class ConfigHolder {
   public Map<String, ConfigHolder.ConfigOption<?>> properties = new HashMap<>();
   public ConfigHolder.ConfigOption<RegionSize> caveRegionSize = new ConfigHolder.ConfigOption<>(
         "Cave Region Size", Configuration.caveSettings.caves.caveRegionSize
      )
      .setCategory("general.underground generation.caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> caveRegionCustomSize = new ConfigHolder.ConfigOption<>(
         "Cave Region Size Custom Value", Configuration.caveSettings.caves.customRegionSize
      )
      .setCategory("general.underground generation.caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> caveSpawnChance = new ConfigHolder.ConfigOption<>(
         "Cave Spawn Chance", Configuration.caveSettings.caves.caveSpawnChance
      )
      .setCategory("general.underground generation.caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> cubicCaveBottom = new ConfigHolder.ConfigOption<>(
         "Type 1 Cave Minimum Altitude", Configuration.caveSettings.caves.cubicCave.caveBottom
      )
      .setCategory("general.underground generation.caves.type 1 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> cubicCaveTop = new ConfigHolder.ConfigOption<>(
         "Type 1 Cave Maximum Altitude", Configuration.caveSettings.caves.cubicCave.caveTop
      )
      .setCategory("general.underground generation.caves.type 1 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> cubicCaveSurfaceCutoffDepth = new ConfigHolder.ConfigOption<>(
         "Type 1 Cave Surface Cutoff Depth", Configuration.caveSettings.caves.cubicCave.caveSurfaceCutoff
      )
      .setCategory("general.underground generation.caves.type 1 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> cubicCaveYCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Vertical", Configuration.caveSettings.caves.cubicCave.yCompression
      )
      .setCategory("general.underground generation.caves.type 1 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> cubicCaveXZCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Horizontal", Configuration.caveSettings.caves.cubicCave.xzCompression
      )
      .setCategory("general.underground generation.caves.type 1 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> cubicCavePriority = new ConfigHolder.ConfigOption<>(
         "Type 1 Cave Priority", Configuration.caveSettings.caves.cubicCave.cavePriority
      )
      .setCategory("general.underground generation.caves.type 1 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> simplexCaveBottom = new ConfigHolder.ConfigOption<>(
         "Type 2 Cave Minimum Altitude", Configuration.caveSettings.caves.simplexCave.caveBottom
      )
      .setCategory("general.underground generation.caves.type 2 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> simplexCaveTop = new ConfigHolder.ConfigOption<>(
         "Type 2 Cave Maximum Altitude", Configuration.caveSettings.caves.simplexCave.caveTop
      )
      .setCategory("general.underground generation.caves.type 2 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> simplexCaveSurfaceCutoffDepth = new ConfigHolder.ConfigOption<>(
         "Type 2 Cave Surface Cutoff Depth", Configuration.caveSettings.caves.simplexCave.caveSurfaceCutoff
      )
      .setCategory("general.underground generation.caves.type 2 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> simplexCaveYCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Vertical", Configuration.caveSettings.caves.simplexCave.yCompression
      )
      .setCategory("general.underground generation.caves.type 2 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> simplexCaveXZCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Horizontal", Configuration.caveSettings.caves.simplexCave.xzCompression
      )
      .setCategory("general.underground generation.caves.type 2 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> simplexCavePriority = new ConfigHolder.ConfigOption<>(
         "Type 2 Cave Priority", Configuration.caveSettings.caves.simplexCave.cavePriority
      )
      .setCategory("general.underground generation.caves.type 2 caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> isSurfaceCavesEnabled = new ConfigHolder.ConfigOption<>(
         "Enable Surface Caves", Configuration.caveSettings.caves.surfaceCave.enableSurfaceCaves
      )
      .setCategory("general.underground generation.caves.surface caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> surfaceCaveBottom = new ConfigHolder.ConfigOption<>(
         "Surface Cave Minimum Altitude", Configuration.caveSettings.caves.surfaceCave.caveBottom
      )
      .setCategory("general.underground generation.caves.surface caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> surfaceCaveTop = new ConfigHolder.ConfigOption<>(
         "Surface Cave Maximum Altitude", Configuration.caveSettings.caves.surfaceCave.caveTop
      )
      .setCategory("general.underground generation.caves.surface caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> surfaceCaveDensity = new ConfigHolder.ConfigOption<>(
         "Surface Cave Density", Configuration.caveSettings.caves.surfaceCave.caveDensity
      )
      .setCategory("general.underground generation.caves.surface caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> vanillaCaveBottom = new ConfigHolder.ConfigOption<>(
         "Vanilla Cave Minimum Altitude", Configuration.caveSettings.caves.vanillaCave.caveBottom
      )
      .setCategory("general.underground generation.caves.vanilla caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> vanillaCaveTop = new ConfigHolder.ConfigOption<>(
         "Vanilla Cave Maximum Altitude", Configuration.caveSettings.caves.vanillaCave.caveTop
      )
      .setCategory("general.underground generation.caves.vanilla caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> vanillaCaveDensity = new ConfigHolder.ConfigOption<>(
         "Vanilla Cave Density", Configuration.caveSettings.caves.vanillaCave.caveDensity
      )
      .setCategory("general.underground generation.caves.vanilla caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> vanillaCavePriority = new ConfigHolder.ConfigOption<>(
         "Vanilla Cave Priority", Configuration.caveSettings.caves.vanillaCave.cavePriority
      )
      .setCategory("general.underground generation.caves.vanilla caves")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<RegionSize> cavernRegionSize = new ConfigHolder.ConfigOption<>(
         "Cavern Region Size", Configuration.caveSettings.caverns.cavernRegionSize
      )
      .setCategory("general.underground generation.caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> cavernRegionCustomSize = new ConfigHolder.ConfigOption<>(
         "Cavern Region Size Custom Value", Configuration.caveSettings.caverns.customRegionSize
      )
      .setCategory("general.underground generation.caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> cavernSpawnChance = new ConfigHolder.ConfigOption<>(
         "Cavern Spawn Chance", Configuration.caveSettings.caverns.cavernSpawnChance
      )
      .setCategory("general.underground generation.caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> liquidCavernBottom = new ConfigHolder.ConfigOption<>(
         "Liquid Cavern Minimum Altitude", Configuration.caveSettings.caverns.liquidCavern.cavernBottom
      )
      .setCategory("general.underground generation.caverns.liquid caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> liquidCavernTop = new ConfigHolder.ConfigOption<>(
         "Liquid Cavern Maximum Altitude", Configuration.caveSettings.caverns.liquidCavern.cavernTop
      )
      .setCategory("general.underground generation.caverns.liquid caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> liquidCavernYCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Vertical", Configuration.caveSettings.caverns.liquidCavern.yCompression
      )
      .setCategory("general.underground generation.caverns.liquid caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> liquidCavernXZCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Horizontal", Configuration.caveSettings.caverns.liquidCavern.xzCompression
      )
      .setCategory("general.underground generation.caverns.liquid caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> liquidCavernPriority = new ConfigHolder.ConfigOption<>(
         "Liquid Cavern Priority", Configuration.caveSettings.caverns.liquidCavern.cavernPriority
      )
      .setCategory("general.underground generation.caverns.liquid caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> flooredCavernBottom = new ConfigHolder.ConfigOption<>(
         "Floored Cavern Minimum Altitude", Configuration.caveSettings.caverns.flooredCavern.cavernBottom
      )
      .setCategory("general.underground generation.caverns.floored caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> flooredCavernTop = new ConfigHolder.ConfigOption<>(
         "Floored Cavern Maximum Altitude", Configuration.caveSettings.caverns.flooredCavern.cavernTop
      )
      .setCategory("general.underground generation.caverns.floored caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> flooredCavernYCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Vertical", Configuration.caveSettings.caverns.flooredCavern.yCompression
      )
      .setCategory("general.underground generation.caverns.floored caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> flooredCavernXZCompression = new ConfigHolder.ConfigOption<>(
         "Compression - Horizontal", Configuration.caveSettings.caverns.flooredCavern.xzCompression
      )
      .setCategory("general.underground generation.caverns.floored caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> flooredCavernPriority = new ConfigHolder.ConfigOption<>(
         "Floored Cavern Priority", Configuration.caveSettings.caverns.flooredCavern.cavernPriority
      )
      .setCategory("general.underground generation.caverns.floored caverns")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> waterRegionSpawnChance = new ConfigHolder.ConfigOption<>(
         "Water Region Spawn Chance", Configuration.caveSettings.waterRegions.waterRegionSpawnChance
      )
      .setCategory("general.underground generation.water regions")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<RegionSize> waterRegionSize = new ConfigHolder.ConfigOption<>(
         "Water Region Size", Configuration.caveSettings.waterRegions.waterRegionSize
      )
      .setCategory("general.underground generation.water regions")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> waterRegionCustomSize = new ConfigHolder.ConfigOption<>(
         "Water Region Size Custom Value", Configuration.caveSettings.waterRegions.waterRegionCustomSize
      )
      .setCategory("general.underground generation.water regions")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> enableVanillaRavines = new ConfigHolder.ConfigOption<>(
         "Enable Ravines", Configuration.caveSettings.ravines.enableVanillaRavines
      )
      .setCategory("general.underground generation.ravines")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> enableFloodedRavines = new ConfigHolder.ConfigOption<>(
         "Enable Flooded Ravines", Configuration.caveSettings.ravines.enableFloodedRavines
      )
      .setCategory("general.underground generation.ravines")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<String> lavaBlock = new ConfigHolder.ConfigOption<>("Lava Block", Configuration.caveSettings.miscellaneous.lavaBlock)
      .setCategory("general.underground generation.miscellaneous")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<String> waterBlock = new ConfigHolder.ConfigOption<>("Water Block", Configuration.caveSettings.miscellaneous.waterBlock)
      .setCategory("general.underground generation.miscellaneous")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> liquidAltitude = new ConfigHolder.ConfigOption<>(
         "Liquid Altitude", Configuration.caveSettings.miscellaneous.liquidAltitude
      )
      .setCategory("general.underground generation.miscellaneous")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> replaceFloatingGravel = new ConfigHolder.ConfigOption<>(
         "Prevent Cascading Gravel", Configuration.caveSettings.miscellaneous.replaceFloatingGravel
      )
      .setCategory("general.underground generation.miscellaneous")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> overrideSurfaceDetection = new ConfigHolder.ConfigOption<>(
         "Override Surface Detection", Configuration.caveSettings.miscellaneous.overrideSurfaceDetection
      )
      .setCategory("general.underground generation.miscellaneous")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> enableFloodedUnderground = new ConfigHolder.ConfigOption<>(
         "Enable Flooded Underground", Configuration.caveSettings.miscellaneous.enableFloodedUnderground
      )
      .setCategory("general.underground generation.miscellaneous")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> flattenBedrock = new ConfigHolder.ConfigOption<>("Flatten Bedrock", Configuration.bedrockSettings.flattenBedrock)
      .setCategory("general.bedrock generation")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Integer> bedrockWidth = new ConfigHolder.ConfigOption<>("Bedrock Layer Width", Configuration.bedrockSettings.bedrockWidth)
      .setCategory("general.bedrock generation")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Boolean> debugVisualizer = new ConfigHolder.ConfigOption<>(
         "Enable DEBUG Visualizer", Configuration.debugsettings.debugVisualizer
      )
      .setCategory("general.debug settings")
      .addToMap(this.properties);
   public ConfigHolder.ConfigOption<Float> cubicCaveNoiseThreshold = new ConfigHolder.ConfigOption<>(
         "Noise Threshold", Configuration.caveSettings.caves.cubicCave.advancedSettings.noiseThreshold
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> cubicCaveFractalOctaves = new ConfigHolder.ConfigOption<>(
         "Fractal Octaves", Configuration.caveSettings.caves.cubicCave.advancedSettings.fractalOctaves
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> cubicCaveFractalGain = new ConfigHolder.ConfigOption<>(
         "Fractal Gain", Configuration.caveSettings.caves.cubicCave.advancedSettings.fractalGain
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> cubicCaveFractalFrequency = new ConfigHolder.ConfigOption<>(
         "Fractal Frequency", Configuration.caveSettings.caves.cubicCave.advancedSettings.fractalFrequency
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> cubicCaveNumGenerators = new ConfigHolder.ConfigOption<>(
         "Number of Generators", Configuration.caveSettings.caves.cubicCave.advancedSettings.numGenerators
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Boolean> cubicCaveEnableVerticalAdjustment = new ConfigHolder.ConfigOption<>(
         "Enable y-adjustment", Configuration.caveSettings.caves.cubicCave.advancedSettings.yAdjust
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> cubicCaveYAdjustF1 = new ConfigHolder.ConfigOption<>(
         "y-adjustment Variable 1", Configuration.caveSettings.caves.cubicCave.advancedSettings.yAdjustF1
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> cubicCaveYAdjustF2 = new ConfigHolder.ConfigOption<>(
         "y-adjustment Variable 2", Configuration.caveSettings.caves.cubicCave.advancedSettings.yAdjustF2
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<FastNoise.NoiseType> cubicCaveNoiseType = new ConfigHolder.ConfigOption<>(
         "Noise Type", Configuration.caveSettings.caves.cubicCave.advancedSettings.noiseType
      )
      .setCategory("general.underground generation.caves.type 1 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> simplexCaveNoiseThreshold = new ConfigHolder.ConfigOption<>(
         "Noise Threshold", Configuration.caveSettings.caves.simplexCave.advancedSettings.noiseThreshold
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> simplexCaveFractalOctaves = new ConfigHolder.ConfigOption<>(
         "Fractal Octaves", Configuration.caveSettings.caves.simplexCave.advancedSettings.fractalOctaves
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> simplexCaveFractalGain = new ConfigHolder.ConfigOption<>(
         "Fractal Gain", Configuration.caveSettings.caves.simplexCave.advancedSettings.fractalGain
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> simplexCaveFractalFrequency = new ConfigHolder.ConfigOption<>(
         "Fractal Frequency", Configuration.caveSettings.caves.simplexCave.advancedSettings.fractalFrequency
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> simplexCaveNumGenerators = new ConfigHolder.ConfigOption<>(
         "Number of Generators", Configuration.caveSettings.caves.simplexCave.advancedSettings.numGenerators
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Boolean> simplexCaveEnableVerticalAdjustment = new ConfigHolder.ConfigOption<>(
         "Enable y-adjustment", Configuration.caveSettings.caves.simplexCave.advancedSettings.yAdjust
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> simplexCaveYAdjustF1 = new ConfigHolder.ConfigOption<>(
         "y-adjustment Variable 1", Configuration.caveSettings.caves.simplexCave.advancedSettings.yAdjustF1
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> simplexCaveYAdjustF2 = new ConfigHolder.ConfigOption<>(
         "y-adjustment Variable 2", Configuration.caveSettings.caves.simplexCave.advancedSettings.yAdjustF2
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<FastNoise.NoiseType> simplexCaveNoiseType = new ConfigHolder.ConfigOption<>(
         "Noise Type", Configuration.caveSettings.caves.simplexCave.advancedSettings.noiseType
      )
      .setCategory("general.underground generation.caves.type 2 caves.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> liquidCavernNoiseThreshold = new ConfigHolder.ConfigOption<>(
         "Noise Threshold", Configuration.caveSettings.caverns.liquidCavern.advancedSettings.noiseThreshold
      )
      .setCategory("general.underground generation.caverns.liquid caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> liquidCavernFractalOctaves = new ConfigHolder.ConfigOption<>(
         "Fractal Octaves", Configuration.caveSettings.caverns.liquidCavern.advancedSettings.fractalOctaves
      )
      .setCategory("general.underground generation.caverns.liquid caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> liquidCavernFractalGain = new ConfigHolder.ConfigOption<>(
         "Fractal Gain", Configuration.caveSettings.caverns.liquidCavern.advancedSettings.fractalGain
      )
      .setCategory("general.underground generation.caverns.liquid caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> liquidCavernFractalFrequency = new ConfigHolder.ConfigOption<>(
         "Fractal Frequency", Configuration.caveSettings.caverns.liquidCavern.advancedSettings.fractalFrequency
      )
      .setCategory("general.underground generation.caverns.liquid caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> liquidCavernNumGenerators = new ConfigHolder.ConfigOption<>(
         "Number of Generators", Configuration.caveSettings.caverns.liquidCavern.advancedSettings.numGenerators
      )
      .setCategory("general.underground generation.caverns.liquid caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<FastNoise.NoiseType> liquidCavernNoiseType = new ConfigHolder.ConfigOption<>(
         "Noise Type", Configuration.caveSettings.caverns.liquidCavern.advancedSettings.noiseType
      )
      .setCategory("general.underground generation.caverns.liquid caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> flooredCavernNoiseThreshold = new ConfigHolder.ConfigOption<>(
         "Noise Threshold", Configuration.caveSettings.caverns.flooredCavern.advancedSettings.noiseThreshold
      )
      .setCategory("general.underground generation.caverns.floored caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> flooredCavernFractalOctaves = new ConfigHolder.ConfigOption<>(
         "Fractal Octaves", Configuration.caveSettings.caverns.flooredCavern.advancedSettings.fractalOctaves
      )
      .setCategory("general.underground generation.caverns.floored caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> flooredCavernFractalGain = new ConfigHolder.ConfigOption<>(
         "Fractal Gain", Configuration.caveSettings.caverns.flooredCavern.advancedSettings.fractalGain
      )
      .setCategory("general.underground generation.caverns.floored caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Float> flooredCavernFractalFrequency = new ConfigHolder.ConfigOption<>(
         "Fractal Frequency", Configuration.caveSettings.caverns.flooredCavern.advancedSettings.fractalFrequency
      )
      .setCategory("general.underground generation.caverns.floored caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<Integer> flooredCavernNumGenerators = new ConfigHolder.ConfigOption<>(
         "Number of Generators", Configuration.caveSettings.caverns.flooredCavern.advancedSettings.numGenerators
      )
      .setCategory("general.underground generation.caverns.floored caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();
   public ConfigHolder.ConfigOption<FastNoise.NoiseType> flooredCavernNoiseType = new ConfigHolder.ConfigOption<>(
         "Noise Type", Configuration.caveSettings.caverns.flooredCavern.advancedSettings.noiseType
      )
      .setCategory("general.underground generation.caverns.floored caverns.advanced settings")
      .addToMap(this.properties)
      .hidden();

   public static class ConfigOption<T> {
      public String name;
      public String fullName;
      public Class<?> type;
      private T value;
      private boolean hidden = false;
      private String category = "";

      public ConfigOption(String name, T value) {
         this.name = name;
         this.value = value;
         this.type = value.getClass();
      }

      public T get() {
         return this.value;
      }

      public void set(Object value) {
         this.value = (T)value;
      }

      public ConfigHolder.ConfigOption<T> hidden() {
         this.hidden = true;
         return this;
      }

      public ConfigHolder.ConfigOption<T> setCategory(String category) {
         this.category = category;
         this.fullName = category + "." + this.name;
         return this;
      }

      public ConfigHolder.ConfigOption<T> addToMap(Map<String, ConfigHolder.ConfigOption<?>> map) {
         map.put(this.fullName, this);
         return this;
      }
   }
}
