package com.yungnickyoung.minecraft.bettercaves.world;

import com.yungnickyoung.minecraft.bettercaves.config.io.ConfigLoader;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.bedrock.FlattenBedrock;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

public class MapGenBetterCaves extends MapGenCaves {
   private MapGenBase defaultCaveGen;
   public WaterRegionController waterRegionController;
   private CaveCarverController caveCarverController;
   private CavernCarverController cavernCarverController;
   public ConfigHolder config;

   public MapGenBetterCaves(InitMapGenEvent event) {
      this.defaultCaveGen = event.getOriginalGen();
   }

   public void func_186125_a(World worldIn, int chunkX, int chunkZ, @Nonnull ChunkPrimer primer) {
      if (!BetterCavesUtils.isDimensionWhitelisted(worldIn.field_73011_w.getDimension())) {
         this.defaultCaveGen.func_186125_a(worldIn, chunkX, chunkZ, primer);
      } else {
         if (this.field_75039_c == null) {
            this.initialize(worldIn);
         }

         if (this.config.flattenBedrock.get()) {
            FlattenBedrock.flattenBedrock(primer, this.config.bedrockWidth.get());
         }

         int[][] surfaceAltitudes = new int[16][16];

         for (int subX = 0; subX < 4; subX++) {
            for (int subZ = 0; subZ < 4; subZ++) {
               int startX = subX * 4;
               int startZ = subZ * 4;

               for (int offsetX = 0; offsetX < 4; offsetX++) {
                  for (int offsetZ = 0; offsetZ < 4; offsetZ++) {
                     int surfaceHeight;
                     if (this.config.overrideSurfaceDetection.get()) {
                        surfaceHeight = 1;
                     } else {
                        surfaceHeight = BetterCavesUtils.getSurfaceAltitudeForColumn(primer, startX + offsetX, startZ + offsetZ);
                     }

                     surfaceAltitudes[startX + offsetX][startZ + offsetZ] = surfaceHeight;
                  }
               }
            }
         }

         IBlockState[][] liquidBlocks = this.waterRegionController.getLiquidBlocksForChunk(chunkX, chunkZ);
         this.caveCarverController.carveChunk(primer, chunkX, chunkZ, surfaceAltitudes, liquidBlocks);
         this.cavernCarverController.carveChunk(primer, chunkX, chunkZ, surfaceAltitudes, liquidBlocks);
      }
   }

   private void initialize(World worldIn) {
      this.field_75039_c = worldIn;
      this.config = ConfigLoader.loadConfigFromFileForDimension(this.field_75039_c.field_73011_w.getDimension());
      this.waterRegionController = new WaterRegionController(this.field_75039_c, this.config);
      this.caveCarverController = new CaveCarverController(this.field_75039_c, this.config);
      this.cavernCarverController = new CavernCarverController(worldIn, this.config);
   }
}
