package com.yungnickyoung.minecraft.bettercaves.world.carver.vanilla;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverUtils;
import com.yungnickyoung.minecraft.bettercaves.world.carver.ICarver;
import java.util.Arrays;
import java.util.Random;
import javax.annotation.Nonnull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

public class VanillaCaveCarver extends MapGenCaves implements ICarver {
   private int bottomY;
   private int topY;
   private int density;
   private int priority;
   private int liquidAltitude;
   private IBlockState debugBlock;
   private boolean isDebugVisualizerEnabled;
   private boolean isReplaceGravel;
   private boolean isFloodedUndergroundEnabled;

   public VanillaCaveCarver(VanillaCaveCarverBuilder builder) {
      this.bottomY = builder.getBottomY();
      this.topY = builder.getTopY();
      this.density = builder.getDensity();
      this.priority = builder.getPriority();
      this.liquidAltitude = builder.getLiquidAltitude();
      this.debugBlock = builder.getDebugBlock();
      this.isDebugVisualizerEnabled = builder.isDebugVisualizerEnabled();
      this.isReplaceGravel = builder.isReplaceGravel();
      this.isFloodedUndergroundEnabled = builder.isFloodedUndergroundEnabled();
      if (this.bottomY > this.topY) {
         BetterCaves.LOGGER.warn("Warning: Min altitude for vanilla caves should not be greater than max altitude.");
         BetterCaves.LOGGER.warn("Using default values...");
         this.bottomY = 40;
         this.topY = 128;
      }
   }

   public void generate(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer, boolean addRooms, IBlockState[][] liquidBlocks, boolean[][] carvingMask) {
      int chunkRadius = this.field_75040_a;
      this.field_75039_c = worldIn;
      this.field_75038_b.setSeed(worldIn.func_72905_C());
      long j = this.field_75038_b.nextLong();
      long k = this.field_75038_b.nextLong();

      for (int currChunkX = chunkX - chunkRadius; currChunkX <= chunkX + chunkRadius; currChunkX++) {
         for (int currChunkZ = chunkZ - chunkRadius; currChunkZ <= chunkZ + chunkRadius; currChunkZ++) {
            long j1 = currChunkX * j;
            long k1 = currChunkZ * k;
            this.field_75038_b.setSeed(j1 ^ k1 ^ worldIn.func_72905_C());
            this.recursiveGenerate(currChunkX, currChunkZ, chunkX, chunkZ, primer, addRooms, liquidBlocks, carvingMask);
         }
      }
   }

   public void generate(World worldIn, int x, int z, ChunkPrimer primer, boolean addRooms, IBlockState[][] liquidBlocks) {
      boolean[][] carvingMask = new boolean[16][16];

      for (boolean[] row : carvingMask) {
         Arrays.fill(row, true);
      }

      this.generate(worldIn, x, z, primer, addRooms, liquidBlocks, carvingMask);
   }

   protected void recursiveGenerate(
      int chunkX,
      int chunkZ,
      int originalChunkX,
      int originalChunkZ,
      @Nonnull ChunkPrimer primer,
      boolean addRooms,
      IBlockState[][] liquidBlocks,
      boolean[][] carvingMask
   ) {
      int numAttempts = this.field_75038_b.nextInt(this.field_75038_b.nextInt(this.field_75038_b.nextInt(15) + 1) + 1);
      if (this.field_75038_b.nextInt(100) > this.density) {
         numAttempts = 0;
      }

      for (int i = 0; i < numAttempts; i++) {
         double caveStartX = chunkX * 16 + this.field_75038_b.nextInt(16);
         double caveStartY = this.field_75038_b.nextInt(this.topY - this.bottomY) + this.bottomY;
         double caveStartZ = chunkZ * 16 + this.field_75038_b.nextInt(16);
         int numAddTunnelCalls = 1;
         if (addRooms && this.field_75038_b.nextInt(4) == 0) {
            this.addRoom(this.field_75038_b.nextLong(), originalChunkX, originalChunkZ, primer, caveStartX, caveStartY, caveStartZ, liquidBlocks, carvingMask);
            numAddTunnelCalls += this.field_75038_b.nextInt(4);
         }

         for (int j = 0; j < numAddTunnelCalls; j++) {
            float yaw = this.field_75038_b.nextFloat() * (float) (Math.PI * 2);
            float pitch = (this.field_75038_b.nextFloat() - 0.5F) * 2.0F / 8.0F;
            float width = this.field_75038_b.nextFloat() * 2.0F + this.field_75038_b.nextFloat();
            if (addRooms && this.field_75038_b.nextInt(10) == 0) {
               width *= this.field_75038_b.nextFloat() * this.field_75038_b.nextFloat() * 3.0F + 1.0F;
            }

            this.addTunnel(
               this.field_75038_b.nextLong(),
               originalChunkX,
               originalChunkZ,
               primer,
               caveStartX,
               caveStartY,
               caveStartZ,
               width,
               yaw,
               pitch,
               0,
               0,
               1.0,
               liquidBlocks,
               carvingMask
            );
         }
      }
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public int getTopY() {
      return this.topY;
   }

   protected void addRoom(
      long seed,
      int originChunkX,
      int originChunkZ,
      ChunkPrimer primer,
      double caveStartX,
      double caveStartY,
      double caveStartZ,
      IBlockState[][] liquidBlocks,
      boolean[][] carvingMask
   ) {
      this.addTunnel(
         seed,
         originChunkX,
         originChunkZ,
         primer,
         caveStartX,
         caveStartY,
         caveStartZ,
         1.0F + this.field_75038_b.nextFloat() * 6.0F,
         0.0F,
         0.0F,
         -1,
         -1,
         0.5,
         liquidBlocks,
         carvingMask
      );
   }

   protected void addTunnel(
      long seed,
      int originChunkX,
      int originChunkZ,
      ChunkPrimer primer,
      double caveStartX,
      double caveStartY,
      double caveStartZ,
      float width,
      float yaw,
      float pitch,
      int startCounter,
      int endCounter,
      double heightModifier,
      IBlockState[][] liquidBlocks,
      boolean[][] carvingMask
   ) {
      Random random = new Random(seed);
      double originBlockX = originChunkX * 16 + 8;
      double originBlockZ = originChunkZ * 16 + 8;
      float yawModifier = 0.0F;
      float pitchModifier = 0.0F;
      if (endCounter <= 0) {
         int i = this.field_75040_a * 16 - 16;
         endCounter = i - random.nextInt(i / 4);
      }

      boolean comesFromRoom = false;
      if (startCounter == -1) {
         startCounter = endCounter / 2;
         comesFromRoom = true;
      }

      for (int randomCounterValue = random.nextInt(endCounter / 2) + endCounter / 4; startCounter < endCounter; startCounter++) {
         double xzOffset = 1.5 + MathHelper.func_76126_a(startCounter * (float) Math.PI / endCounter) * width;
         double yOffset = xzOffset * heightModifier;
         float pitchXZ = MathHelper.func_76134_b(pitch);
         float pitchY = MathHelper.func_76126_a(pitch);
         caveStartX += MathHelper.func_76134_b(yaw) * pitchXZ;
         caveStartY += pitchY;
         caveStartZ += MathHelper.func_76126_a(yaw) * pitchXZ;
         boolean flag = random.nextInt(6) == 0;
         if (flag) {
            pitch *= 0.92F;
         } else {
            pitch *= 0.7F;
         }

         pitch += pitchModifier * 0.1F;
         yaw += yawModifier * 0.1F;
         pitchModifier *= 0.9F;
         yawModifier *= 0.75F;
         pitchModifier += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
         yawModifier += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;
         if (!comesFromRoom && startCounter == randomCounterValue && width > 1.0F && endCounter > 0) {
            this.addTunnel(
               random.nextLong(),
               originChunkX,
               originChunkZ,
               primer,
               caveStartX,
               caveStartY,
               caveStartZ,
               random.nextFloat() * 0.5F + 0.5F,
               yaw - (float) (Math.PI / 2),
               pitch / 3.0F,
               startCounter,
               endCounter,
               1.0,
               liquidBlocks,
               carvingMask
            );
            this.addTunnel(
               random.nextLong(),
               originChunkX,
               originChunkZ,
               primer,
               caveStartX,
               caveStartY,
               caveStartZ,
               random.nextFloat() * 0.5F + 0.5F,
               yaw + (float) (Math.PI / 2),
               pitch / 3.0F,
               startCounter,
               endCounter,
               1.0,
               liquidBlocks,
               carvingMask
            );
            return;
         }

         if (comesFromRoom || random.nextInt(4) != 0) {
            double caveStartXOffsetFromCenter = caveStartX - originBlockX;
            double caveStartZOffsetFromCenter = caveStartZ - originBlockZ;
            double distanceToEnd = endCounter - startCounter;
            double d7 = width + 2.0F + 16.0F;
            if (caveStartXOffsetFromCenter * caveStartXOffsetFromCenter
                  + caveStartZOffsetFromCenter * caveStartZOffsetFromCenter
                  - distanceToEnd * distanceToEnd
               > d7 * d7) {
               return;
            }

            if (caveStartX >= originBlockX - 16.0 - xzOffset * 2.0
               && caveStartZ >= originBlockZ - 16.0 - xzOffset * 2.0
               && caveStartX <= originBlockX + 16.0 + xzOffset * 2.0
               && caveStartZ <= originBlockZ + 16.0 + xzOffset * 2.0) {
               int minX = MathHelper.func_76128_c(caveStartX - xzOffset) - originChunkX * 16 - 1;
               int minY = MathHelper.func_76128_c(caveStartY - yOffset) - 1;
               int minZ = MathHelper.func_76128_c(caveStartZ - xzOffset) - originChunkZ * 16 - 1;
               int maxX = MathHelper.func_76128_c(caveStartX + xzOffset) - originChunkX * 16 + 1;
               int maxY = MathHelper.func_76128_c(caveStartY + yOffset) + 1;
               int maxZ = MathHelper.func_76128_c(caveStartZ + xzOffset) - originChunkZ * 16 + 1;
               if (minX < 0) {
                  minX = 0;
               }

               if (maxX > 16) {
                  maxX = 16;
               }

               if (minY < 1) {
                  minY = 1;
               }

               if (maxY > 248) {
                  maxY = 248;
               }

               if (minZ < 0) {
                  minZ = 0;
               }

               if (maxZ > 16) {
                  maxZ = 16;
               }

               for (int currX = minX; currX < maxX; currX++) {
                  double xAxisDist = (currX + originChunkX * 16 + 0.5 - caveStartX) / xzOffset;

                  for (int currZ = minZ; currZ < maxZ; currZ++) {
                     double zAxisDist = (currZ + originChunkZ * 16 + 0.5 - caveStartZ) / xzOffset;
                     if (carvingMask[currX][currZ] && xAxisDist * xAxisDist + zAxisDist * zAxisDist < 1.0) {
                        for (int currY = maxY; currY > minY; currY--) {
                           double yAxisDist = (currY - 1 + 0.5 - caveStartY) / yOffset;
                           if (yAxisDist > -0.7 && xAxisDist * xAxisDist + yAxisDist * yAxisDist + zAxisDist * zAxisDist < 1.0) {
                              IBlockState liquidBlock = liquidBlocks[BetterCavesUtils.getLocal(currX)][BetterCavesUtils.getLocal(currZ)];
                              if (this.isDebugVisualizerEnabled) {
                                 CarverUtils.debugDigBlock(primer, currX, currY, currZ, this.debugBlock, true);
                              } else {
                                 this.digBlock(
                                    this.field_75039_c,
                                    primer,
                                    originChunkX,
                                    originChunkZ,
                                    currX,
                                    currY,
                                    currZ,
                                    liquidBlock,
                                    this.liquidAltitude,
                                    this.isReplaceGravel
                                 );
                              }
                           } else if (this.isDebugVisualizerEnabled) {
                              CarverUtils.debugDigBlock(primer, currX, currY, currZ, this.debugBlock, false);
                           }
                        }
                     }
                  }
               }

               if (comesFromRoom) {
                  break;
               }
            }
         }
      }
   }

   private void digBlock(
      World world,
      ChunkPrimer primer,
      int chunkX,
      int chunkZ,
      int localX,
      int y,
      int localZ,
      IBlockState liquidBlockState,
      int liquidAltitude,
      boolean replaceGravel
   ) {
      BlockPos pos = new BlockPos(chunkX * 16 + localX, y, chunkZ * 16 + localZ);
      boolean flooded = this.isFloodedUndergroundEnabled
         && !this.isDebugVisualizerEnabled
         && BiomeDictionary.hasType(world.func_180494_b(pos), Type.OCEAN)
         && y < world.func_181545_F();
      if (!flooded
         || BiomeDictionary.hasType(world.func_180494_b(pos.func_177974_f()), Type.OCEAN)
            && BiomeDictionary.hasType(world.func_180494_b(pos.func_177978_c()), Type.OCEAN)
            && BiomeDictionary.hasType(world.func_180494_b(pos.func_177976_e()), Type.OCEAN)
            && BiomeDictionary.hasType(world.func_180494_b(pos.func_177968_d()), Type.OCEAN)) {
         IBlockState airBlockState = flooded ? Blocks.field_150355_j.func_176223_P() : Blocks.field_150350_a.func_176223_P();
         CarverUtils.digBlock(world, primer, pos, airBlockState, liquidBlockState, liquidAltitude, replaceGravel);
      }
   }

   protected boolean isOceanBlock(ChunkPrimer data, int x, int y, int z, int chunkX, int chunkZ) {
      return false;
   }
}
