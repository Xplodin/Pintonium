package com.yungnickyoung.minecraft.bettercaves.noise;

import com.yungnickyoung.minecraft.bettercaves.config.BCSettings;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NoiseGen {
   private long seed;
   private int numGenerators;
   private NoiseSettings noiseSettings;
   private float yCompression;
   private float xzCompression;
   private List<INoiseLibrary> listNoiseGens = new ArrayList<>();

   public NoiseGen(World world, boolean isFastNoise, NoiseSettings noiseSettings, int numGenerators, float yComp, float xzComp) {
      this.seed = world.func_72905_C();
      this.noiseSettings = noiseSettings;
      this.numGenerators = numGenerators;
      this.yCompression = yComp;
      this.xzCompression = xzComp;
      this.initializeNoiseGens(isFastNoise);
   }

   public NoiseColumn generateNoiseColumn(BlockPos blockPos, int minHeight, int maxHeight) {
      int x = blockPos.func_177958_n();
      int z = blockPos.func_177952_p();
      NoiseColumn noiseColumn = new NoiseColumn();

      for (int y = minHeight; y <= maxHeight; y++) {
         Vector3f f = new Vector3f(x * this.xzCompression, y * this.yCompression, z * this.xzCompression);
         NoiseTuple newTuple = new NoiseTuple();

         for (int i = 0; i < this.numGenerators; i++) {
            newTuple.put(this.listNoiseGens.get(i).GetNoise(f.x, f.y, f.z));
         }

         noiseColumn.put(y, newTuple);
      }

      return noiseColumn;
   }

   public NoiseColumn interpolateNoiseColumn(BlockPos blockPos, int minHeight, int maxHeight, int subChunkSize) {
      int x = blockPos.func_177958_n();
      int z = blockPos.func_177952_p();
      NoiseColumn noiseColumn = new NoiseColumn();
      int startY = minHeight;

      while (startY <= maxHeight) {
         int endY = Math.min(startY + subChunkSize - 1, maxHeight);
         Vector3f subChunkStart = new Vector3f(x * this.xzCompression, startY * this.yCompression, z * this.xzCompression);
         Vector3f subChunkEnd = new Vector3f(x * this.xzCompression, endY * this.yCompression, z * this.xzCompression);
         NoiseTuple startTuple = new NoiseTuple();
         NoiseTuple endTuple = new NoiseTuple();

         for (int i = 0; i < this.numGenerators; i++) {
            startTuple.put(this.listNoiseGens.get(i).GetNoise(subChunkStart.x, subChunkStart.y, subChunkStart.z));
            endTuple.put(this.listNoiseGens.get(i).GetNoise(subChunkEnd.x, subChunkEnd.y, subChunkEnd.z));
         }

         noiseColumn.put(startY, startTuple);
         noiseColumn.put(endY, endTuple);

         for (int y = startY + 1; y < endY; y++) {
            float startCoeff;
            float endCoeff;
            if (endY == maxHeight) {
               startCoeff = (float)(endY - startY - y - startY) / (endY - startY);
               endCoeff = (float)(y - startY) / (endY - startY);
            } else {
               startCoeff = BCSettings.START_COEFFS[y - startY];
               endCoeff = BCSettings.END_COEFFS[y - startY];
            }

            NoiseTuple newTuple = startTuple.times(startCoeff).plus(endTuple.times(endCoeff));
            noiseColumn.put(y, newTuple);
         }

         startY += subChunkSize;
      }

      return noiseColumn;
   }

   public NoiseCube interpolateNoiseCube(BlockPos startPos, BlockPos endPos, int minHeight, int maxHeight) {
      int startX = startPos.func_177958_n();
      int endX = endPos.func_177958_n();
      int startZ = startPos.func_177952_p();
      int endZ = endPos.func_177952_p();
      int subChunkSize = endX - startX + 1;
      NoiseColumn noisesX0Z0 = this.generateNoiseColumn(new BlockPos(startX, 1, startZ), minHeight, maxHeight);
      NoiseColumn noisesX0Z1 = this.generateNoiseColumn(new BlockPos(startX, 1, endZ), minHeight, maxHeight);
      NoiseColumn noisesX1Z0 = this.generateNoiseColumn(new BlockPos(endX, 1, startZ), minHeight, maxHeight);
      NoiseColumn noisesX1Z1 = this.generateNoiseColumn(new BlockPos(endX, 1, endZ), minHeight, maxHeight);
      NoiseCube cube = new NoiseCube(subChunkSize);
      cube.get(0).set(0, noisesX0Z0);
      cube.get(0).set(subChunkSize - 1, noisesX0Z1);
      cube.get(subChunkSize - 1).set(0, noisesX1Z0);
      cube.get(subChunkSize - 1).set(subChunkSize - 1, noisesX1Z1);

      for (int x = 1; x < subChunkSize - 1; x++) {
         float startCoeff = BCSettings.START_COEFFS[x];
         float endCoeff = BCSettings.END_COEFFS[x];
         NoiseColumn xz0 = cube.get(x).get(0);

         for (int y = minHeight; y <= maxHeight; y++) {
            NoiseTuple startTuple = cube.get(0).get(0).get(y);
            NoiseTuple endTuple = cube.get(subChunkSize - 1).get(0).get(y);
            NoiseTuple newTuple = startTuple.times(startCoeff).plus(endTuple.times(endCoeff));
            xz0.put(y, newTuple);
         }

         NoiseColumn xz1 = cube.get(x).get(subChunkSize - 1);

         for (int y = minHeight; y <= maxHeight; y++) {
            NoiseTuple startTuple = cube.get(0).get(subChunkSize - 1).get(y);
            NoiseTuple endTuple = cube.get(subChunkSize - 1).get(subChunkSize - 1).get(y);
            NoiseTuple newTuple = startTuple.times(startCoeff).plus(endTuple.times(endCoeff));
            xz1.put(y, newTuple);
         }
      }

      for (int x = 0; x < subChunkSize; x++) {
         for (int z = 1; z < subChunkSize - 1; z++) {
            float startCoeff = BCSettings.START_COEFFS[z];
            float endCoeff = BCSettings.END_COEFFS[z];
            NoiseColumn xz = cube.get(x).get(z);

            for (int y = minHeight; y <= maxHeight; y++) {
               NoiseTuple startTuple = cube.get(x).get(0).get(y);
               NoiseTuple endTuple = cube.get(x).get(subChunkSize - 1).get(y);
               NoiseTuple newTuple = startTuple.times(startCoeff).plus(endTuple.times(endCoeff));
               xz.put(y, newTuple);
            }
         }
      }

      return cube;
   }

   public long getSeed() {
      return this.seed;
   }

   private void initializeNoiseGens(boolean isFastNoise) {
      if (isFastNoise) {
         for (int i = 0; i < this.numGenerators; i++) {
            FastNoise noiseGen = new FastNoise();
            noiseGen.SetSeed((int)this.seed + 1111 * (i + 1));
            noiseGen.SetFractalType(this.noiseSettings.getFractalType());
            noiseGen.SetNoiseType(this.noiseSettings.getNoiseType());
            noiseGen.SetFractalOctaves(this.noiseSettings.getOctaves());
            noiseGen.SetFractalGain(this.noiseSettings.getGain());
            noiseGen.SetFrequency(this.noiseSettings.getFrequency());
            this.listNoiseGens.add(noiseGen);
         }
      } else {
         for (int i = 0; i < this.numGenerators; i++) {
            OpenSimplex2S noiseGen = new OpenSimplex2S(this.seed + 1111 * (i + 1));
            noiseGen.setGain(this.noiseSettings.getGain());
            noiseGen.setOctaves(this.noiseSettings.getOctaves());
            noiseGen.setFrequency(this.noiseSettings.getFrequency());
            noiseGen.setLacunarity(2.0);
            this.listNoiseGens.add(noiseGen);
         }
      }
   }
}
