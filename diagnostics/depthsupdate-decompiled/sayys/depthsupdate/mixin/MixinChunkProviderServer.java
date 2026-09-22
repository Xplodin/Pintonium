package sayys.depthsupdate.mixin;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.BedrockFilter;
import sayys.depthsupdate.core.DeepFill;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.ChunkPrimerAdapter;
import sayys.depthsupdate.world.generation.noise.CaveNoiseGenerator;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

@Mixin(ChunkProviderServer.class)
public class MixinChunkProviderServer {
   @Shadow
   @Final
   private IChunkGenerator field_186029_c;
   @Shadow
   @Final
   private WorldServer field_73251_h;
   @Unique
   private Random depthsupdate$fillRandom;
   @Unique
   private UndergroundRiverGenerator depthsupdate$riverGenerator;
   @Unique
   private CaveNoiseGenerator depthsupdate$noiseCaveGenerator;

   @Redirect(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/IChunkGenerator;func_185932_a(II)Lnet/minecraft/world/chunk/Chunk;"),
      method = "Lnet/minecraft/world/gen/ChunkProviderServer;func_186025_d(II)Lnet/minecraft/world/chunk/Chunk;"
   )
   private Chunk depthsupdate$onGenerateChunk(IChunkGenerator generator, int x, int z) {
      boolean vanillaOverworld = generator.getClass() == ChunkGeneratorOverworld.class;
      boolean flatOrDebug = generator instanceof ChunkGeneratorFlat || generator instanceof ChunkGeneratorDebug;
      boolean deepWorld = !flatOrDebug && HeightManager.isExtended(this.field_73251_h) && HeightManager.get(this.field_73251_h).minY() < 0;
      boolean fillCustom = deepWorld && !vanillaOverworld && DepthsUpdateConfig.heightExtension.extendCustomWorldTypes;
      boolean filterBedrock = deepWorld && (vanillaOverworld || fillCustom);
      if (filterBedrock) {
         BedrockFilter.begin();
      }

      Chunk chunk;
      try {
         chunk = generator.func_185932_a(x, z);
      } finally {
         if (filterBedrock) {
            BedrockFilter.end();
         }
      }

      if (fillCustom && chunk != null) {
         HeightContext ctx = HeightManager.get(this.field_73251_h);
         int minY = ctx.minY();
         if (this.depthsupdate$fillRandom == null) {
            this.depthsupdate$fillRandom = new Random();
         }

         this.depthsupdate$fillRandom.setSeed(x * 341873128712L + z * 132897987541L);
         IBlockState stone = Blocks.field_150348_b.func_176223_P();
         IBlockState deepslate = BlockUtils.getDeepslateBlockState();
         IBlockState bedrock = Blocks.field_150357_h.func_176223_P();
         int fillMaxY = Math.max(4, DepthsUpdateConfig.deepslateMaxY);
         ExtendedBlockStorage[] storageArrays = chunk.func_76587_i();
         boolean hasSkyLight = this.field_73251_h.field_73011_w.func_191066_m();

         for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
               for (int by = minY; by <= fillMaxY; by++) {
                  if (by >= 0 && by <= 4) {
                     int storageIdx = ctx.toStorageIndex(by);
                     if (storageIdx >= 0 && storageIdx < storageArrays.length) {
                        ExtendedBlockStorage section = storageArrays[storageIdx];
                        if (section != Chunk.field_186036_a && section.func_177485_a(bx, by & 15, bz).func_177230_c() == Blocks.field_150357_h) {
                           section.func_177484_a(bx, by & 15, bz, stone);
                        }
                     }
                  }

                  IBlockState state = DeepFill.bandAt(by, minY, this.depthsupdate$fillRandom, bedrock, deepslate, stone);
                  if (state != null) {
                     int storageIdx = ctx.toStorageIndex(by);
                     if (storageIdx >= 0 && storageIdx < storageArrays.length) {
                        ExtendedBlockStorage section = storageArrays[storageIdx];
                        if (section == Chunk.field_186036_a) {
                           section = new ExtendedBlockStorage(by >> 4 << 4, hasSkyLight);
                           storageArrays[storageIdx] = section;
                        }

                        if (by < 0 || section.func_177485_a(bx, by & 15, bz).func_177230_c() == Blocks.field_150348_b) {
                           section.func_177484_a(bx, by & 15, bz, state);
                        }
                     }
                  }
               }
            }
         }

         ChunkPrimerAdapter adapter = new ChunkPrimerAdapter(chunk, ctx);
         if (DepthsUpdateConfig.generateUndergroundRivers) {
            if (this.depthsupdate$riverGenerator == null) {
               this.depthsupdate$riverGenerator = new UndergroundRiverGenerator(this.field_73251_h);
            }

            this.depthsupdate$riverGenerator.generate(x, z, adapter);
         }

         if (this.depthsupdate$noiseCaveGenerator == null) {
            this.depthsupdate$noiseCaveGenerator = new CaveNoiseGenerator(this.field_73251_h);
         }

         byte[] biomeIds = chunk.func_76605_m();
         Biome[] biomes = new Biome[biomeIds.length];

         for (int i = 0; i < biomeIds.length; i++) {
            biomes[i] = Biome.func_180276_a(biomeIds[i] & 255, Biomes.field_76772_c);
         }

         this.depthsupdate$noiseCaveGenerator.generate(x, z, adapter, biomes);
         chunk.func_76603_b();
         return chunk;
      } else {
         return chunk;
      }
   }
}
