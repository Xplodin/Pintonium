package sayys.depthsupdate.mixin;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.DeepFill;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;
import sayys.depthsupdate.world.generation.noise.CaveNoiseGenerator;
import sayys.depthsupdate.world.generation.river.UndergroundRiverGenerator;

@Mixin(ChunkGeneratorOverworld.class)
public abstract class MixinChunkGeneratorOverworld {
   @Shadow
   private World field_185995_n;
   @Shadow
   @Final
   private Random field_185990_i;
   @Unique
   private UndergroundRiverGenerator depthsupdate$riverGenerator;
   @Unique
   private CaveNoiseGenerator depthsupdate$noiseCaveGenerator;

   @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/gen/ChunkGeneratorOverworld;func_185976_a(IILnet/minecraft/world/chunk/ChunkPrimer;)V")
   private void depthsupdate$fillDeepUnderground(int x, int z, ChunkPrimer primer, CallbackInfo ci) {
      if (this.getClass() == ChunkGeneratorOverworld.class) {
         if (HeightManager.isExtended(this.field_185995_n) && HeightManager.get(this.field_185995_n).minY() < 0) {
            HeightContext ctx = HeightManager.get(this.field_185995_n);
            int minY = ctx.minY();
            IBlockState stone = Blocks.field_150348_b.func_176223_P();
            IBlockState deepslate = BlockUtils.getDeepslateBlockState();
            IBlockState bedrock = Blocks.field_150357_h.func_176223_P();
            int fillMaxY = Math.max(0, DepthsUpdateConfig.deepslateMaxY);

            for (int bx = 0; bx < 16; bx++) {
               for (int bz = 0; bz < 16; bz++) {
                  for (int by = minY; by <= fillMaxY; by++) {
                     IBlockState banded = DeepFill.bandAt(by, minY, this.field_185990_i, bedrock, deepslate, stone);
                     if (banded != null) {
                        primer.func_177855_a(bx, by, bz, banded);
                     }
                  }
               }
            }

            if (DepthsUpdateConfig.generateUndergroundRivers) {
               if (this.depthsupdate$riverGenerator == null) {
                  this.depthsupdate$riverGenerator = new UndergroundRiverGenerator(this.field_185995_n);
               }

               this.depthsupdate$riverGenerator.generate(x, z, primer);
            }
         }
      }
   }

   @Inject(
      at = @At("RETURN"),
      method = "Lnet/minecraft/world/gen/ChunkGeneratorOverworld;func_185977_a(IILnet/minecraft/world/chunk/ChunkPrimer;[Lnet/minecraft/world/biome/Biome;)V"
   )
   private void depthsupdate$carveNoiseCaves(int x, int z, ChunkPrimer primer, Biome[] biomesIn, CallbackInfo ci) {
      if (this.getClass() == ChunkGeneratorOverworld.class) {
         if (HeightManager.isExtended(this.field_185995_n) && HeightManager.get(this.field_185995_n).minY() < 0) {
            if (this.depthsupdate$noiseCaveGenerator == null) {
               this.depthsupdate$noiseCaveGenerator = new CaveNoiseGenerator(this.field_185995_n);
            }

            this.depthsupdate$noiseCaveGenerator.generate(x, z, primer, biomesIn);
         }
      }
   }
}
