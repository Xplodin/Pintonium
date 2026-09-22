package com.yungnickyoung.minecraft.bettercaves.world.ravine;

import com.yungnickyoung.minecraft.bettercaves.config.io.ConfigLoader;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.WaterRegionController;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverUtils;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

public class MapGenBetterRavine extends MapGenRavine {
   private ConfigHolder config;
   private WaterRegionController waterRegionController;
   private MapGenBase defaultRavineGen;
   IBlockState[][] currChunkLiquidBlocks;
   int currChunkX;
   int currChunkZ;

   public MapGenBetterRavine(InitMapGenEvent event) {
      this.defaultRavineGen = event.getOriginalGen();
   }

   public void func_186125_a(World worldIn, int x, int z, @Nonnull ChunkPrimer primer) {
      if (!BetterCavesUtils.isDimensionWhitelisted(worldIn.field_73011_w.getDimension())) {
         this.defaultRavineGen.func_186125_a(worldIn, x, z, primer);
      } else {
         if (this.config == null) {
            this.initialize(worldIn);
         }

         if (this.config.enableVanillaRavines.get()) {
            super.func_186125_a(worldIn, x, z, primer);
         }
      }
   }

   protected void digBlock(ChunkPrimer primer, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop) {
      BlockPos pos = new BlockPos(x + chunkX * 16, y, z + chunkZ * 16);
      IBlockState liquidBlockState;
      if (this.currChunkLiquidBlocks != null && chunkX == this.currChunkX && chunkZ == this.currChunkZ) {
         try {
            liquidBlockState = this.currChunkLiquidBlocks[BetterCavesUtils.getLocal(x)][BetterCavesUtils.getLocal(z)];
         } catch (Exception var12) {
            liquidBlockState = Blocks.field_150353_l.func_176223_P();
         }
      } else {
         try {
            this.currChunkLiquidBlocks = this.waterRegionController.getLiquidBlocksForChunk(chunkX, chunkZ);
            liquidBlockState = this.currChunkLiquidBlocks[BetterCavesUtils.getLocal(x)][BetterCavesUtils.getLocal(z)];
            this.currChunkX = chunkX;
            this.currChunkZ = chunkZ;
         } catch (Exception var13) {
            liquidBlockState = Blocks.field_150353_l.func_176223_P();
         }
      }

      boolean flooded = this.config.enableFloodedRavines.get()
         && BiomeDictionary.hasType(this.field_75039_c.func_180494_b(pos), Type.OCEAN)
         && y < this.field_75039_c.func_181545_F();
      if (flooded) {
         float smoothAmpFactor = BetterCavesUtils.biomeDistanceFactor(this.field_75039_c, pos, 2, b -> !BiomeDictionary.hasType(b, Type.OCEAN));
         if (smoothAmpFactor <= 0.25F) {
            return;
         }
      }

      IBlockState airBlockState = flooded ? Blocks.field_150355_j.func_176223_P() : field_186136_b;
      CarverUtils.digBlock(
         this.field_75039_c, primer, pos, airBlockState, liquidBlockState, this.config.liquidAltitude.get(), this.config.replaceFloatingGravel.get()
      );
   }

   protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
      return false;
   }

   private void initialize(World worldIn) {
      this.field_75039_c = worldIn;
      int dimensionID = worldIn.field_73011_w.getDimension();
      this.config = ConfigLoader.loadConfigFromFileForDimension(dimensionID);
      this.waterRegionController = new WaterRegionController(this.field_75039_c, this.config);
   }
}
