package com.yungnickyoung.minecraft.bettercaves.world.carver.cavern;

import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.CavernType;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class CavernCarverBuilder {
   private CarverSettings settings;
   private CavernType cavernType;
   private int bottomY;
   private int topY;

   public CavernCarverBuilder(World world) {
      this.settings = new CarverSettings(world);
   }

   public CavernCarver build() {
      return new CavernCarver(this);
   }

   public CavernCarverBuilder ofTypeFromConfig(CavernType cavernType, ConfigHolder config) {
      this.settings.setLiquidAltitude(config.liquidAltitude.get());
      this.settings.setReplaceFloatingGravel(config.replaceFloatingGravel.get());
      this.settings.getNoiseSettings().setFractalType(FastNoise.FractalType.RigidMulti);
      this.settings.setEnableDebugVisualizer(config.debugVisualizer.get());
      this.settings.setFastNoise(true);
      this.cavernType = cavernType;
      switch (cavernType) {
         case LIQUID:
            this.settings.setNoiseThreshold(config.liquidCavernNoiseThreshold.get());
            this.settings.getNoiseSettings().setNoiseType(config.liquidCavernNoiseType.get());
            this.settings.getNoiseSettings().setOctaves(config.liquidCavernFractalOctaves.get());
            this.settings.getNoiseSettings().setGain(config.liquidCavernFractalGain.get());
            this.settings.getNoiseSettings().setFrequency(config.liquidCavernFractalFrequency.get());
            this.settings.setNumGens(config.liquidCavernNumGenerators.get());
            this.settings.setyCompression(config.liquidCavernYCompression.get());
            this.settings.setXzCompression(config.liquidCavernXZCompression.get());
            this.settings.setPriority(config.liquidCavernPriority.get());
            this.bottomY = config.liquidCavernBottom.get();
            this.topY = config.liquidCavernTop.get();
            break;
         case FLOORED:
            this.settings.setNoiseThreshold(config.flooredCavernNoiseThreshold.get());
            this.settings.getNoiseSettings().setNoiseType(config.flooredCavernNoiseType.get());
            this.settings.getNoiseSettings().setOctaves(config.flooredCavernFractalOctaves.get());
            this.settings.getNoiseSettings().setGain(config.flooredCavernFractalGain.get());
            this.settings.getNoiseSettings().setFrequency(config.flooredCavernFractalFrequency.get());
            this.settings.setNumGens(config.flooredCavernNumGenerators.get());
            this.settings.setyCompression(config.flooredCavernYCompression.get());
            this.settings.setXzCompression(config.flooredCavernXZCompression.get());
            this.settings.setPriority(config.flooredCavernPriority.get());
            this.bottomY = config.flooredCavernBottom.get();
            this.topY = config.flooredCavernTop.get();
      }

      return this;
   }

   public CavernCarverBuilder noiseType(FastNoise.NoiseType noiseType) {
      this.settings.getNoiseSettings().setNoiseType(noiseType);
      return this;
   }

   public CavernCarverBuilder fractalOctaves(int fractalOctaves) {
      this.settings.getNoiseSettings().setOctaves(fractalOctaves);
      return this;
   }

   public CavernCarverBuilder fractalGain(float fractalGain) {
      this.settings.getNoiseSettings().setGain(fractalGain);
      return this;
   }

   public CavernCarverBuilder fractalFrequency(float fractalFreq) {
      this.settings.getNoiseSettings().setFrequency(fractalFreq);
      return this;
   }

   public CavernCarverBuilder numberOfGenerators(int numGens) {
      this.settings.setNumGens(numGens);
      return this;
   }

   public CavernCarverBuilder verticalCompression(float yCompression) {
      this.settings.setyCompression(yCompression);
      return this;
   }

   public CavernCarverBuilder horizontalCompression(float xzCompression) {
      this.settings.setXzCompression(xzCompression);
      return this;
   }

   public CavernCarverBuilder noiseThreshold(float noiseThreshold) {
      this.settings.setNoiseThreshold(noiseThreshold);
      return this;
   }

   public CavernCarverBuilder debugVisualizerBlock(IBlockState vBlock) {
      this.settings.setDebugBlock(vBlock);
      return this;
   }

   public CavernCarverBuilder liquidAltitude(int liquidAltitude) {
      this.settings.setLiquidAltitude(liquidAltitude);
      return this;
   }

   public CavernCarverBuilder enableDebugVisualizer(boolean enableDebugVisualizer) {
      this.settings.setEnableDebugVisualizer(enableDebugVisualizer);
      return this;
   }

   public CavernCarverBuilder cavernType(CavernType cavernType) {
      this.cavernType = cavernType;
      return this;
   }

   public CavernCarverBuilder bottomY(int bottomY) {
      this.bottomY = bottomY;
      return this;
   }

   public CavernCarverBuilder topY(int topY) {
      this.topY = topY;
      return this;
   }

   public CarverSettings getSettings() {
      return this.settings;
   }

   public CavernType getCavernType() {
      return this.cavernType;
   }

   public int getBottomY() {
      return this.bottomY;
   }

   public int getTopY() {
      return this.topY;
   }
}
