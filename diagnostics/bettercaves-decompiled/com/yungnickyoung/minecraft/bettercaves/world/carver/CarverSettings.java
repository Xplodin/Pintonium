package com.yungnickyoung.minecraft.bettercaves.world.carver;

import com.yungnickyoung.minecraft.bettercaves.noise.NoiseSettings;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;

public class CarverSettings {
   private World world;
   private long seed;
   private int priority;
   private NoiseSettings noiseSettings = new NoiseSettings();
   private boolean isFastNoise;
   private int numGens;
   private float yCompression;
   private float xzCompression;
   private float noiseThreshold;
   private int liquidAltitude;
   private boolean replaceFloatingGravel;
   private IBlockState debugBlock;
   private boolean enableDebugVisualizer;

   public CarverSettings(World world) {
      this.world = world;
      this.seed = world.func_72905_C();
   }

   public World getWorld() {
      return this.world;
   }

   public void setWorld(World world) {
      this.world = world;
   }

   public long getSeed() {
      return this.seed;
   }

   public void setSeed(long seed) {
      this.seed = seed;
   }

   public int getPriority() {
      return this.priority;
   }

   public void setPriority(int priority) {
      this.priority = priority;
   }

   public NoiseSettings getNoiseSettings() {
      return this.noiseSettings;
   }

   public void setNoiseSettings(NoiseSettings noiseSettings) {
      this.noiseSettings = noiseSettings;
   }

   public boolean isFastNoise() {
      return this.isFastNoise;
   }

   public void setFastNoise(boolean fastNoise) {
      this.isFastNoise = fastNoise;
   }

   public int getNumGens() {
      return this.numGens;
   }

   public void setNumGens(int numGens) {
      this.numGens = numGens;
   }

   public float getyCompression() {
      return this.yCompression;
   }

   public void setyCompression(float yCompression) {
      this.yCompression = yCompression;
   }

   public float getXzCompression() {
      return this.xzCompression;
   }

   public void setXzCompression(float xzCompression) {
      this.xzCompression = xzCompression;
   }

   public float getNoiseThreshold() {
      return this.noiseThreshold;
   }

   public void setNoiseThreshold(float noiseThreshold) {
      this.noiseThreshold = noiseThreshold;
   }

   public int getLiquidAltitude() {
      return this.liquidAltitude;
   }

   public void setLiquidAltitude(int liquidAltitude) {
      this.liquidAltitude = liquidAltitude;
   }

   public boolean isReplaceFloatingGravel() {
      return this.replaceFloatingGravel;
   }

   public void setReplaceFloatingGravel(boolean replaceFloatingGravel) {
      this.replaceFloatingGravel = replaceFloatingGravel;
   }

   public IBlockState getDebugBlock() {
      return this.debugBlock;
   }

   public void setDebugBlock(IBlockState debugBlock) {
      this.debugBlock = debugBlock;
   }

   public boolean isEnableDebugVisualizer() {
      return this.enableDebugVisualizer;
   }

   public void setEnableDebugVisualizer(boolean enableDebugVisualizer) {
      this.enableDebugVisualizer = enableDebugVisualizer;
   }
}
