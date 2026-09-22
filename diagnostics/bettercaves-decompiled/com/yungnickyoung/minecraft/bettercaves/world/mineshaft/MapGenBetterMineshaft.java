package com.yungnickyoung.minecraft.bettercaves.world.mineshaft;

import com.yungnickyoung.minecraft.bettercaves.config.io.ConfigLoader;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraft.world.gen.structure.MapGenMineshaft.Type;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

public class MapGenBetterMineshaft extends MapGenMineshaft {
   private MapGenBase defaultMineshaftGen;
   private int liquidAltitude;

   public MapGenBetterMineshaft(InitMapGenEvent event) {
      this.defaultMineshaftGen = event.getOriginalGen();
   }

   protected StructureStart func_75049_b(int chunkX, int chunkZ) {
      MapGenStructureIO.func_143034_b(MapGenBetterMineshaft.StructureBetterMineshaftStart.class, "Mineshaft");
      Biome biome = this.field_75039_c.func_180494_b(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8));
      Type mapgenmineshaft$type = biome instanceof BiomeMesa ? Type.MESA : Type.NORMAL;
      return new MapGenBetterMineshaft.StructureBetterMineshaftStart(this.field_75039_c, this.field_75038_b, chunkX, chunkZ, mapgenmineshaft$type);
   }

   public void func_186125_a(World worldIn, int x, int z, ChunkPrimer primer) {
      if (!BetterCavesUtils.isDimensionWhitelisted(worldIn.field_73011_w.getDimension())) {
         this.defaultMineshaftGen.func_186125_a(worldIn, x, z, primer);
      } else {
         if (this.field_75039_c == null) {
            this.initialize(worldIn);
         }

         super.func_186125_a(worldIn, x, z, primer);
      }
   }

   private void initialize(World worldIn) {
      this.field_75039_c = worldIn;
      ConfigHolder config = ConfigLoader.loadConfigFromFileForDimension(worldIn.field_73011_w.getDimension());
      this.liquidAltitude = config.liquidAltitude.get();
   }

   private class StructureBetterMineshaftStart extends StructureMineshaftStart {
      public StructureBetterMineshaftStart(World worldIn, Random rand, int chunkX, int chunkZ, Type type) {
         super(worldIn, rand, chunkX, chunkZ, type);
      }

      public void func_75068_a(World worldIn, Random rand, StructureBoundingBox structurebb) {
         this.field_75075_a
            .removeIf(
               component -> component.func_74874_b().field_78895_b < MapGenBetterMineshaft.this.liquidAltitude + 5
                  || component.func_74874_b().func_78884_a(structurebb) && !component.func_74875_a(worldIn, rand, structurebb)
            );
      }
   }
}
