package com.yungnickyoung.minecraft.bettercaves.world.carver.vanilla;

import net.minecraft.block.state.IBlockState;

public class VanillaCaveCarverBuilder {
   private int bottomY = 1;
   private int topY = 1;
   private int density = 0;
   private int priority = 0;
   private int liquidAltitude = 10;
   private IBlockState debugBlock;
   private boolean isDebugVisualizerEnabled;
   private boolean isReplaceGravel;
   private boolean isFloodedUndergroundEnabled;

   public VanillaCaveCarver build() {
      return new VanillaCaveCarver(this);
   }

   public VanillaCaveCarverBuilder bottomY(int bottomY) {
      this.bottomY = bottomY;
      return this;
   }

   public VanillaCaveCarverBuilder topY(int topY) {
      this.topY = topY;
      return this;
   }

   public VanillaCaveCarverBuilder density(int density) {
      this.density = density;
      return this;
   }

   public VanillaCaveCarverBuilder priority(int priority) {
      this.priority = priority;
      return this;
   }

   public VanillaCaveCarverBuilder debugVisualizerBlock(IBlockState debugBlock) {
      this.debugBlock = debugBlock;
      return this;
   }

   public VanillaCaveCarverBuilder debugVisualizerEnabled(boolean isDebugVisualizerEnabled) {
      this.isDebugVisualizerEnabled = isDebugVisualizerEnabled;
      return this;
   }

   public VanillaCaveCarverBuilder liquidAltitude(int liquidAltitude) {
      this.liquidAltitude = liquidAltitude;
      return this;
   }

   public VanillaCaveCarverBuilder replaceGravel(boolean replaceGravel) {
      this.isReplaceGravel = replaceGravel;
      return this;
   }

   public VanillaCaveCarverBuilder floodedUnderground(boolean floodedUnderground) {
      this.isFloodedUndergroundEnabled = floodedUnderground;
      return this;
   }

   public int getBottomY() {
      return this.bottomY;
   }

   public int getTopY() {
      return this.topY;
   }

   public int getDensity() {
      return this.density;
   }

   public int getPriority() {
      return this.priority;
   }

   public IBlockState getDebugBlock() {
      return this.debugBlock;
   }

   public boolean isDebugVisualizerEnabled() {
      return this.isDebugVisualizerEnabled;
   }

   public int getLiquidAltitude() {
      return this.liquidAltitude;
   }

   public boolean isReplaceGravel() {
      return this.isReplaceGravel;
   }

   public boolean isFloodedUndergroundEnabled() {
      return this.isFloodedUndergroundEnabled;
   }
}
