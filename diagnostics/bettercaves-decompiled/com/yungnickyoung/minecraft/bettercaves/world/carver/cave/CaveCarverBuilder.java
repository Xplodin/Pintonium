package com.yungnickyoung.minecraft.bettercaves.world.carver.cave;

import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.CaveType;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class CaveCarverBuilder {
   private CarverSettings settings;
   private int surfaceCutoff;
   private int bottomY;
   private int topY;
   private boolean enableYAdjust;
   private float yAdjustF1;
   private float yAdjustF2;

   public CaveCarverBuilder(World world) {
      this.settings = new CarverSettings(world);
   }

   public CaveCarver build() {
      return new CaveCarver(this);
   }

   public CaveCarverBuilder ofTypeFromConfig(CaveType caveType, ConfigHolder config) {
      this.settings.setLiquidAltitude(config.liquidAltitude.get());
      this.settings.setReplaceFloatingGravel(config.replaceFloatingGravel.get());
      this.settings.setEnableDebugVisualizer(config.debugVisualizer.get());
      this.settings.getNoiseSettings().setFractalType(FastNoise.FractalType.RigidMulti);
      switch (caveType) {
         case CUBIC:
            this.settings.setFastNoise(true);
            this.settings.setNoiseThreshold(config.cubicCaveNoiseThreshold.get());
            this.settings.getNoiseSettings().setNoiseType(config.cubicCaveNoiseType.get());
            this.settings.getNoiseSettings().setOctaves(config.cubicCaveFractalOctaves.get());
            this.settings.getNoiseSettings().setGain(config.cubicCaveFractalGain.get());
            this.settings.getNoiseSettings().setFrequency(config.cubicCaveFractalFrequency.get());
            this.settings.setNumGens(config.cubicCaveNumGenerators.get());
            this.settings.setXzCompression(config.cubicCaveXZCompression.get());
            this.settings.setyCompression(config.cubicCaveYCompression.get());
            this.settings.setPriority(config.cubicCavePriority.get());
            this.surfaceCutoff = config.cubicCaveSurfaceCutoffDepth.get();
            this.bottomY = config.cubicCaveBottom.get();
            this.topY = config.cubicCaveTop.get();
            this.enableYAdjust = config.cubicCaveEnableVerticalAdjustment.get();
            this.yAdjustF1 = config.cubicCaveYAdjustF1.get();
            this.yAdjustF2 = config.cubicCaveYAdjustF2.get();
            break;
         case SIMPLEX:
            this.settings.setFastNoise(false);
            this.settings.setNoiseThreshold(config.simplexCaveNoiseThreshold.get());
            this.settings.getNoiseSettings().setNoiseType(config.simplexCaveNoiseType.get());
            this.settings.getNoiseSettings().setOctaves(config.simplexCaveFractalOctaves.get());
            this.settings.getNoiseSettings().setGain(config.simplexCaveFractalGain.get());
            this.settings.getNoiseSettings().setFrequency(config.simplexCaveFractalFrequency.get());
            this.settings.setNumGens(config.simplexCaveNumGenerators.get());
            this.settings.setXzCompression(config.simplexCaveXZCompression.get());
            this.settings.setyCompression(config.simplexCaveYCompression.get());
            this.settings.setPriority(config.simplexCavePriority.get());
            this.surfaceCutoff = config.simplexCaveSurfaceCutoffDepth.get();
            this.bottomY = config.simplexCaveBottom.get();
            this.topY = config.simplexCaveTop.get();
            this.enableYAdjust = config.simplexCaveEnableVerticalAdjustment.get();
            this.yAdjustF1 = config.simplexCaveYAdjustF1.get();
            this.yAdjustF2 = config.simplexCaveYAdjustF2.get();
      }

      return this;
   }

   public CaveCarverBuilder noiseType(FastNoise.NoiseType noiseType) {
      this.settings.getNoiseSettings().setNoiseType(noiseType);
      return this;
   }

   public CaveCarverBuilder fractalOctaves(int fractalOctaves) {
      this.settings.getNoiseSettings().setOctaves(fractalOctaves);
      return this;
   }

   public CaveCarverBuilder fractalGain(float fractalGain) {
      this.settings.getNoiseSettings().setGain(fractalGain);
      return this;
   }

   public CaveCarverBuilder fractalFrequency(float fractalFreq) {
      this.settings.getNoiseSettings().setFrequency(fractalFreq);
      return this;
   }

   public CaveCarverBuilder numberOfGenerators(int numGens) {
      this.settings.setNumGens(numGens);
      return this;
   }

   public CaveCarverBuilder verticalCompression(float yCompression) {
      this.settings.setyCompression(yCompression);
      return this;
   }

   public CaveCarverBuilder horizontalCompression(float xzCompression) {
      this.settings.setXzCompression(xzCompression);
      return this;
   }

   public CaveCarverBuilder surfaceCutoff(int surfaceCutoff) {
      this.surfaceCutoff = surfaceCutoff;
      return this;
   }

   public CaveCarverBuilder bottomY(int bottomY) {
      this.bottomY = bottomY;
      return this;
   }

   public CaveCarverBuilder topY(int topY) {
      this.topY = topY;
      return this;
   }

   public CaveCarverBuilder verticalAdjuster1(float yAdjustF1) {
      this.yAdjustF1 = yAdjustF1;
      return this;
   }

   public CaveCarverBuilder verticalAdjuster2(float yAdjustF2) {
      this.yAdjustF2 = yAdjustF2;
      return this;
   }

   public CaveCarverBuilder enableVerticalAdjustment(boolean enableYAdjust) {
      this.enableYAdjust = enableYAdjust;
      return this;
   }

   public CaveCarverBuilder noiseThreshold(float noiseThreshold) {
      this.settings.setNoiseThreshold(noiseThreshold);
      return this;
   }

   public CaveCarverBuilder debugVisualizerBlock(IBlockState vBlock) {
      this.settings.setDebugBlock(vBlock);
      return this;
   }

   public CaveCarverBuilder liquidAltitude(int liquidAltitude) {
      this.settings.setLiquidAltitude(liquidAltitude);
      return this;
   }

   public CaveCarverBuilder enableDebugVisualizer(boolean enableDebugVisualizer) {
      this.settings.setEnableDebugVisualizer(enableDebugVisualizer);
      return this;
   }

   public CarverSettings getSettings() {
      return this.settings;
   }

   public int getSurfaceCutoff() {
      return this.surfaceCutoff;
   }

   public int getBottomY() {
      return this.bottomY;
   }

   public int getTopY() {
      return this.topY;
   }

   public boolean isEnableYAdjust() {
      return this.enableYAdjust;
   }

   public float getyAdjustF1() {
      return this.yAdjustF1;
   }

   public float getyAdjustF2() {
      return this.yAdjustF2;
   }
}
