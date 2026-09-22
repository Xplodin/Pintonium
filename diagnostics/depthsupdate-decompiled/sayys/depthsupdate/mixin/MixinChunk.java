package sayys.depthsupdate.mixin;

import com.google.common.base.Predicate;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.ChunkGeneratorDebug;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent.EnteringChunk;
import org.apache.logging.log4j.LogManager;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;
import sayys.depthsupdate.util.BlockUtils;

@Mixin(Chunk.class)
public abstract class MixinChunk {
   @Shadow
   @Final
   private World field_76637_e;
   @Shadow
   @Final
   private ExtendedBlockStorage[] field_76652_q;
   @Shadow
   @Final
   private int[] field_76638_b;
   @Shadow
   @Final
   public int field_76635_g;
   @Shadow
   @Final
   public int field_76647_h;
   @Shadow
   @Final
   private int[] field_76634_f;
   @Shadow
   private boolean field_76643_l;
   @Shadow
   private int field_82912_p;
   @Shadow
   private boolean field_76644_m;
   @Shadow
   @Final
   private ClassInheritanceMultiMap<Entity>[] field_76645_j;
   @Unique
   private static final ExtendedBlockStorage NULL_BLOCK_STORAGE = null;
   @Unique
   private static final ThreadLocal<HeightContext> depthsupdate$initContext = ThreadLocal.withInitial(() -> HeightContext.VANILLA);
   @Unique
   private static final ThreadLocal<Integer> depthsupdate$setBlockDepth = ThreadLocal.withInitial(() -> 0);

   @Shadow
   public abstract int func_76625_h();

   @Shadow
   protected abstract int func_150808_b(int var1, int var2, int var3);

   @Shadow
   @Nullable
   public abstract TileEntity func_177424_a(BlockPos var1, EnumCreateEntityType var2);

   @Shadow
   private void func_76595_e(int x, int z) {
   }

   @Shadow
   private void func_76615_h(int x, int y, int z) {
   }

   @Shadow
   public abstract void func_76603_b();

   @Shadow
   private void func_76609_d(int p_76609_1_, int p_76609_2_, int p_76609_3_, int p_76609_4_) {
   }

   @Shadow
   public abstract IBlockState func_186032_a(int var1, int var2, int var3);

   @Shadow
   public abstract int func_177413_a(EnumSkyBlock var1, @NonNull BlockPos var2);

   @Unique
   private HeightContext depthsupdate$ctx() {
      return HeightManager.get(this.field_76637_e);
   }

   @Unique
   private boolean depthsupdate$isExtended() {
      return HeightManager.isExtended(this.field_76637_e);
   }

   @Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/chunk/Chunk;<init>(Lnet/minecraft/world/World;II)V")
   private static void depthsupdate$captureWorldForInit(World worldIn, int x, int z, CallbackInfo ci) {
      depthsupdate$initContext.set(HeightManager.get(worldIn));
   }

   @ModifyConstant(constant = @Constant(intValue = 16), method = "Lnet/minecraft/world/chunk/Chunk;<init>(Lnet/minecraft/world/World;II)V")
   private int depthsupdate$modifyStorageArraysSize(int original) {
      HeightContext ctx = depthsupdate$initContext.get();
      return ctx.isExtended() ? ctx.totalStorageSections() : original;
   }

   @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/chunk/Chunk;<init>(Lnet/minecraft/world/World;II)V")
   private void depthsupdate$onChunkInitDefault(World worldIn, int x, int z, CallbackInfo ci) {
      depthsupdate$initContext.remove();
      HeightContext ctx = HeightManager.get(worldIn);
      if (ctx.isExtended()) {
         for (int i = 0; i < this.field_76634_f.length; i++) {
            this.field_76634_f[i] = ctx.minY();
         }
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_76606_c(II)Z")
   public void depthsupdate$isEmptyBetween(int startY, int endY, CallbackInfoReturnable<Boolean> cir) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         if (startY < ctx.minY()) {
            startY = ctx.minY();
         }

         if (endY >= ctx.maxY()) {
            endY = ctx.maxY() - 1;
         }

         for (int i = startY; i <= endY; i += 16) {
            int chunkY = ctx.toStorageIndex(i);
            if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
               ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
               if (extendedblockstorage != NULL_BLOCK_STORAGE && !extendedblockstorage.func_76663_a()) {
                  cir.setReturnValue(false);
                  return;
               }
            }
         }

         cir.setReturnValue(true);
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_150811_f(II)Z")
   private void depthsupdate$checkLight(int p_150811_1_, int p_150811_2_, CallbackInfoReturnable<Boolean> cir) {
      if (this.depthsupdate$isExtended()) {
         int i = this.func_76625_h();
         boolean flag = false;
         boolean flag1 = false;
         int minY = this.depthsupdate$ctx().minY();
         MutableBlockPos blockpos$mutableblockpos = new MutableBlockPos((this.field_76635_g << 4) + p_150811_1_, 0, (this.field_76647_h << 4) + p_150811_2_);

         for (int j = i + 16 - 1; j > this.field_76637_e.func_181545_F() || j > minY && !flag1; j--) {
            blockpos$mutableblockpos.func_181079_c(blockpos$mutableblockpos.func_177958_n(), j, blockpos$mutableblockpos.func_177952_p());
            int k = this.func_150808_b(
               blockpos$mutableblockpos.func_177958_n(), blockpos$mutableblockpos.func_177956_o(), blockpos$mutableblockpos.func_177952_p()
            );
            if (k == 255 && blockpos$mutableblockpos.func_177956_o() < this.field_76637_e.func_181545_F()) {
               flag1 = true;
            }

            if (!flag && k > 0) {
               flag = true;
            } else if (flag && k == 0 && !this.field_76637_e.func_175664_x(blockpos$mutableblockpos)) {
               cir.setReturnValue(false);
               return;
            }
         }

         for (int l = blockpos$mutableblockpos.func_177956_o(); l > minY; l--) {
            blockpos$mutableblockpos.func_181079_c(blockpos$mutableblockpos.func_177958_n(), l, blockpos$mutableblockpos.func_177952_p());
            if (this.func_186032_a(blockpos$mutableblockpos.func_177958_n(), blockpos$mutableblockpos.func_177956_o(), blockpos$mutableblockpos.func_177952_p())
                  .getLightValue(this.field_76637_e, blockpos$mutableblockpos)
               > 0) {
               this.field_76637_e.func_175664_x(blockpos$mutableblockpos);
            }
         }

         cir.setReturnValue(true);
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_186032_a(III)Lnet/minecraft/block/state/IBlockState;")
   public void depthsupdate$getBlockState(int x, int y, int z, CallbackInfoReturnable<IBlockState> cir) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         if (this.field_76637_e.func_175624_G() == WorldType.field_180272_g) {
            IBlockState iblockstate = null;
            if (y == 60) {
               iblockstate = Blocks.field_180401_cv.func_176223_P();
            }

            if (y == 70) {
               iblockstate = ChunkGeneratorDebug.func_177461_b(x, z);
            }

            cir.setReturnValue(iblockstate == null ? Blocks.field_150350_a.func_176223_P() : iblockstate);
         } else {
            int chunkY = ctx.toStorageIndex(y);
            if (y >= ctx.minY() && chunkY >= 0 && chunkY < this.field_76652_q.length) {
               ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
               if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                  cir.setReturnValue(extendedblockstorage.func_177485_a(x & 15, y & 15, z & 15));
                  return;
               }
            }

            cir.setReturnValue(Blocks.field_150350_a.func_176223_P());
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/chunk/Chunk;func_177436_a(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;"
   )
   public void depthsupdate$setBlockState(@NonNull BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         int i = pos.func_177958_n() & 15;
         int j = pos.func_177956_o();
         int k = pos.func_177952_p() & 15;
         int l = k << 4 | i;
         if (j >= this.field_76638_b[l] - 1) {
            this.field_76638_b[l] = -999;
         }

         int i1 = this.field_76634_f[l];
         IBlockState iblockstate = this.func_186032_a(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p());
         if (j < DepthsUpdateConfig.deepslateMaxY) {
            state = BlockUtils.getDeepslateVariant(state);
         }

         if (iblockstate == state) {
            cir.setReturnValue(null);
         } else {
            Block block = state.func_177230_c();
            Block block1 = iblockstate.func_177230_c();
            int k1 = iblockstate.getLightOpacity(this.field_76637_e, pos);
            int chunkY = ctx.toStorageIndex(j);
            if (chunkY < 0 || chunkY >= this.field_76652_q.length) {
               cir.setReturnValue(null);
               return;
            }

            ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
            boolean flag = false;
            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
               if (block == Blocks.field_150350_a) {
                  cir.setReturnValue(null);
                  return;
               }

               extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.field_76637_e.field_73011_w.func_191066_m());
               this.field_76652_q[chunkY] = extendedblockstorage;
               flag = j >= i1;
            }

            extendedblockstorage.func_177484_a(i, j & 15, k, state);
            if (!this.field_76637_e.field_72995_K) {
               if (block1 != block) {
                  block1.func_180663_b(this.field_76637_e, pos, iblockstate);
               }

               TileEntity te = this.func_177424_a(pos, EnumCreateEntityType.CHECK);
               if (te != null && te.shouldRefresh(this.field_76637_e, pos, iblockstate, state)) {
                  this.field_76637_e.func_175713_t(pos);
               }
            } else if (block1.hasTileEntity(iblockstate)) {
               TileEntity te = this.func_177424_a(pos, EnumCreateEntityType.CHECK);
               if (te != null && te.shouldRefresh(this.field_76637_e, pos, iblockstate, state)) {
                  this.field_76637_e.func_175713_t(pos);
               }
            }

            if (extendedblockstorage.func_177485_a(i, j & 15, k).func_177230_c() != block) {
               cir.setReturnValue(null);
            } else {
               if (flag) {
                  this.func_76603_b();
               } else {
                  int j1 = state.getLightOpacity(this.field_76637_e, pos);
                  if (j1 > 0) {
                     if (j >= i1) {
                        this.func_76615_h(i, j + 1, k);
                     }
                  } else if (j == i1 - 1) {
                     this.func_76615_h(i, j, k);
                  }

                  if (j1 != k1 && (j1 < k1 || this.func_177413_a(EnumSkyBlock.SKY, pos) > 0 || this.func_177413_a(EnumSkyBlock.BLOCK, pos) > 0)) {
                     this.func_76595_e(i, k);
                  }
               }

               if (!this.field_76637_e.field_72995_K && block1 != block && (!this.field_76637_e.captureBlockSnapshots || block.hasTileEntity(state))) {
                  int depth = depthsupdate$setBlockDepth.get();
                  if (depth < 16) {
                     depthsupdate$setBlockDepth.set(depth + 1);

                     try {
                        block.func_176213_c(this.field_76637_e, pos, state);
                     } finally {
                        depthsupdate$setBlockDepth.set(depth);
                     }
                  }
               }

               if (block.hasTileEntity(state)) {
                  TileEntity tileentity1 = this.func_177424_a(pos, EnumCreateEntityType.CHECK);
                  if (tileentity1 == null) {
                     tileentity1 = block.createTileEntity(this.field_76637_e, state);
                     this.field_76637_e.func_175690_a(pos, tileentity1);
                  }

                  if (tileentity1 != null) {
                     tileentity1.func_145836_u();
                  }
               }

               this.field_76643_l = true;
               cir.setReturnValue(iblockstate);
            }
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/chunk/Chunk;func_177413_a(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;)I"
   )
   public void depthsupdate$getLightFor(EnumSkyBlock type, @NonNull BlockPos pos, CallbackInfoReturnable<Integer> cir) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         int i = pos.func_177958_n() & 15;
         int j = pos.func_177956_o();
         int k = pos.func_177952_p() & 15;
         int chunkY = ctx.toStorageIndex(j);
         if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
            ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
               int height = this.field_76634_f[k << 4 | i];
               cir.setReturnValue(j >= height ? type.field_77198_c : 0);
            } else if (type == EnumSkyBlock.SKY) {
               cir.setReturnValue(!this.field_76637_e.field_73011_w.func_191066_m() ? 0 : extendedblockstorage.func_76670_c(i, j & 15, k));
            } else {
               cir.setReturnValue(type == EnumSkyBlock.BLOCK ? extendedblockstorage.func_76674_d(i, j & 15, k) : type.field_77198_c);
            }
         } else {
            cir.setReturnValue(type.field_77198_c);
         }
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/chunk/Chunk;func_177431_a(Lnet/minecraft/world/EnumSkyBlock;Lnet/minecraft/util/math/BlockPos;I)V"
   )
   public void depthsupdate$setLightFor(EnumSkyBlock type, @NonNull BlockPos pos, int lightValue, CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         int i = pos.func_177958_n() & 15;
         int j = pos.func_177956_o();
         int k = pos.func_177952_p() & 15;
         int chunkY = ctx.toStorageIndex(j);
         if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
            ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
               extendedblockstorage = new ExtendedBlockStorage(j >> 4 << 4, this.field_76637_e.field_73011_w.func_191066_m());
               this.field_76652_q[chunkY] = extendedblockstorage;
               this.func_76603_b();
            }

            this.field_76643_l = true;
            if (type == EnumSkyBlock.SKY) {
               if (this.field_76637_e.field_73011_w.func_191066_m()) {
                  extendedblockstorage.func_76657_c(i, j & 15, k, lightValue);
               }
            } else if (type == EnumSkyBlock.BLOCK) {
               extendedblockstorage.func_76677_d(i, j & 15, k, lightValue);
            }

            ci.cancel();
         } else {
            ci.cancel();
         }
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_177443_a(Lnet/minecraft/util/math/BlockPos;I)I")
   public void depthsupdate$getLightSubtracted(@NonNull BlockPos pos, int amount, CallbackInfoReturnable<Integer> cir) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         int i = pos.func_177958_n() & 15;
         int j = pos.func_177956_o();
         int k = pos.func_177952_p() & 15;
         int chunkY = ctx.toStorageIndex(j);
         if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
            ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
            if (extendedblockstorage == NULL_BLOCK_STORAGE) {
               cir.setReturnValue(
                  this.field_76637_e.field_73011_w.func_191066_m() && amount < EnumSkyBlock.SKY.field_77198_c ? EnumSkyBlock.SKY.field_77198_c - amount : 0
               );
            } else {
               int l = !this.field_76637_e.field_73011_w.func_191066_m() ? 0 : extendedblockstorage.func_76670_c(i, j & 15, k);
               l -= amount;
               int i1 = extendedblockstorage.func_76674_d(i, j & 15, k);
               if (i1 > l) {
                  l = i1;
               }

               cir.setReturnValue(l);
            }
         } else {
            cir.setReturnValue(
               this.field_76637_e.field_73011_w.func_191066_m() && amount < EnumSkyBlock.SKY.field_77198_c ? EnumSkyBlock.SKY.field_77198_c - amount : 0
            );
         }
      }
   }

   @Inject(at = @At("RETURN"), method = "Lnet/minecraft/world/chunk/Chunk;<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ChunkPrimer;II)V")
   private void depthsupdate$onChunkPrimerInit(@NonNull World worldIn, ChunkPrimer primer, int x, int z, CallbackInfo ci) {
      if (HeightManager.isExtended(worldIn)) {
         HeightContext ctx = HeightManager.get(worldIn);
         boolean flag = worldIn.field_73011_w.func_191066_m();

         for (int i = 0; i < this.field_76652_q.length; i++) {
            this.field_76652_q[i] = null;
         }

         for (int i = 0; i < this.field_76634_f.length; i++) {
            this.field_76634_f[i] = ctx.minY();
         }

         for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
               for (int l = ctx.minY(); l < ctx.maxY(); l++) {
                  IBlockState iblockstate = primer.func_177856_a(j, l, k);
                  if (iblockstate.func_185904_a() != Material.field_151579_a) {
                     int chunkY = ctx.toStorageIndex(l);
                     if (this.field_76652_q[chunkY] == null) {
                        this.field_76652_q[chunkY] = new ExtendedBlockStorage(l >> 4 << 4, flag);
                     }

                     this.field_76652_q[chunkY].func_177484_a(j, l & 15, k, iblockstate);
                  }
               }
            }
         }

         this.func_76603_b();
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_76625_h()I")
   private void depthsupdate$getTopFilledSegment(CallbackInfoReturnable<Integer> cir) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();

         for (int i = 16 + ctx.upperSections() - 1; i >= 16; i--) {
            if (i < this.field_76652_q.length && this.field_76652_q[i] != NULL_BLOCK_STORAGE) {
               cir.setReturnValue(this.field_76652_q[i].func_76662_d());
               return;
            }
         }

         for (int ix = 15; ix >= 0; ix--) {
            if (this.field_76652_q[ix] != NULL_BLOCK_STORAGE) {
               cir.setReturnValue(this.field_76652_q[ix].func_76662_d());
               return;
            }
         }

         int negStart = 16 + ctx.upperSections();
         int negEnd = negStart + ctx.negativeSections();

         for (int ixx = negStart; ixx < negEnd; ixx++) {
            if (ixx < this.field_76652_q.length && this.field_76652_q[ixx] != NULL_BLOCK_STORAGE) {
               cir.setReturnValue(this.field_76652_q[ixx].func_76662_d());
               return;
            }
         }

         cir.setReturnValue(ctx.minY());
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, require = 0, method = "Lnet/minecraft/world/chunk/Chunk;func_76590_a()V")
   private void depthsupdate$generateHeightMap(@NonNull CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         ci.cancel();
         HeightContext ctx = this.depthsupdate$ctx();
         int i = this.func_76625_h();
         this.field_82912_p = Integer.MAX_VALUE;
         int minY = ctx.minY();

         for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
               this.field_76638_b[j + (k << 4)] = -999;
               this.field_76634_f[k << 4 | j] = minY;

               for (int l = i + 16; l > minY; l--) {
                  if (this.func_150808_b(j, l - 1, k) != 0) {
                     this.field_76634_f[k << 4 | j] = l;
                     if (l < this.field_82912_p) {
                        this.field_82912_p = l;
                     }
                     break;
                  }
               }
            }
         }

         this.field_76643_l = true;
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_76603_b()V")
   private void depthsupdate$generateSkylightMap(@NonNull CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         ci.cancel();
         HeightContext ctx = this.depthsupdate$ctx();
         int i = this.func_76625_h();
         this.field_82912_p = Integer.MAX_VALUE;
         int minY = ctx.minY();

         for (int j = 0; j < 16; j++) {
            for (int k = 0; k < 16; k++) {
               this.field_76638_b[j + (k << 4)] = -999;
               this.field_76634_f[k << 4 | j] = minY;

               for (int l = i + 16; l > minY; l--) {
                  if (this.func_150808_b(j, l - 1, k) != 0) {
                     this.field_76634_f[k << 4 | j] = l;
                     if (l < this.field_82912_p) {
                        this.field_82912_p = l;
                     }
                     break;
                  }
               }

               if (this.field_76637_e.field_73011_w.func_191066_m()) {
                  int k1 = 15;
                  int i1 = i + 16 - 1;

                  while (true) {
                     int j1 = this.func_150808_b(j, i1, k);
                     if (j1 == 0 && k1 != 15) {
                        j1 = 1;
                     }

                     k1 -= j1;
                     if (k1 <= 0) {
                        break;
                     }

                     int chunkY = ctx.toStorageIndex(i1);
                     if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
                        ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
                        if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                           extendedblockstorage.func_76657_c(j, i1 & 15, k, k1);
                           this.field_76637_e.func_175679_n(new BlockPos((this.field_76635_g << 4) + j, i1, (this.field_76647_h << 4) + k));
                        }
                     }

                     if (--i1 < minY || k1 <= 0) {
                        break;
                     }
                  }
               }
            }
         }

         this.field_76643_l = true;
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_76615_h(III)V")
   private void depthsupdate$relightBlock(int x, int y, int z, @NonNull CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         ci.cancel();
         HeightContext ctx = this.depthsupdate$ctx();
         int i = this.field_76634_f[z << 4 | x];
         int j = i;
         int minY = ctx.minY();
         if (y > i) {
            j = y;
         }

         while (j > minY && this.func_150808_b(x, j - 1, z) == 0) {
            j--;
         }

         if (j != i) {
            this.field_76637_e.func_72975_g(x + this.field_76635_g * 16, z + this.field_76647_h * 16, j, i);
            this.field_76634_f[z << 4 | x] = j;
            int k = this.field_76635_g * 16 + x;
            int l = this.field_76647_h * 16 + z;
            if (this.field_76637_e.field_73011_w.func_191066_m()) {
               if (j < i) {
                  for (int j1 = j; j1 < i; j1++) {
                     int chunkY = ctx.toStorageIndex(j1);
                     if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
                        ExtendedBlockStorage extendedblockstorage2 = this.field_76652_q[chunkY];
                        if (extendedblockstorage2 != NULL_BLOCK_STORAGE) {
                           extendedblockstorage2.func_76657_c(x, j1 & 15, z, 15);
                           this.field_76637_e.func_175679_n(new BlockPos((this.field_76635_g << 4) + x, j1, (this.field_76647_h << 4) + z));
                        }
                     }
                  }
               } else {
                  for (int i1 = i; i1 < j; i1++) {
                     int chunkY = ctx.toStorageIndex(i1);
                     if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
                        ExtendedBlockStorage extendedblockstorage = this.field_76652_q[chunkY];
                        if (extendedblockstorage != NULL_BLOCK_STORAGE) {
                           extendedblockstorage.func_76657_c(x, i1 & 15, z, 0);
                           this.field_76637_e.func_175679_n(new BlockPos((this.field_76635_g << 4) + x, i1, (this.field_76647_h << 4) + z));
                        }
                     }
                  }
               }

               int k1 = 15;

               while (j > minY && k1 > 0) {
                  int i2 = this.func_150808_b(x, --j, z);
                  if (i2 == 0) {
                     i2 = 1;
                  }

                  k1 -= i2;
                  if (k1 < 0) {
                     k1 = 0;
                  }

                  int chunkY = ctx.toStorageIndex(j);
                  if (chunkY >= 0 && chunkY < this.field_76652_q.length) {
                     ExtendedBlockStorage extendedblockstorage1 = this.field_76652_q[chunkY];
                     if (extendedblockstorage1 != NULL_BLOCK_STORAGE) {
                        extendedblockstorage1.func_76657_c(x, j & 15, z, k1);
                     }
                  }
               }
            }

            int l1 = this.field_76634_f[z << 4 | x];
            int j2 = i;
            int k2 = l1;
            if (l1 < i) {
               j2 = l1;
               k2 = i;
            }

            if (l1 < this.field_82912_p) {
               this.field_82912_p = l1;
            }

            if (this.field_76637_e.field_73011_w.func_191066_m()) {
               for (EnumFacing enumfacing : Plane.HORIZONTAL) {
                  this.func_76609_d(k + enumfacing.func_82601_c(), l + enumfacing.func_82599_e(), j2, k2);
               }

               this.func_76609_d(k, l, j2, k2);
            }

            this.field_76643_l = true;
         }
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/world/chunk/Chunk;func_76612_a(Lnet/minecraft/entity/Entity;)V")
   public void depthsupdate$addEntity(@NonNull Entity entityIn, CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         this.field_76644_m = true;
         int i = MathHelper.func_76128_c(entityIn.field_70165_t / 16.0);
         int j = MathHelper.func_76128_c(entityIn.field_70161_v / 16.0);
         if (i != this.field_76635_g || j != this.field_76647_h) {
            LogManager.getLogger().warn("Wrong location! ({}, {}) should be ({}, {}), {}", i, j, this.field_76635_g, this.field_76647_h, entityIn);
            entityIn.func_70106_y();
         }

         int k = ctx.toStorageIndex(MathHelper.func_76128_c(entityIn.field_70163_u));
         if (k < 0) {
            k = 0;
         }

         if (k >= this.field_76645_j.length) {
            k = this.field_76645_j.length - 1;
         }

         MinecraftForge.EVENT_BUS.post(new EnteringChunk(entityIn, this.field_76635_g, this.field_76647_h, entityIn.field_70176_ah, entityIn.field_70164_aj));
         entityIn.field_70175_ag = true;
         entityIn.field_70176_ah = this.field_76635_g;
         entityIn.field_70162_ai = k;
         entityIn.field_70164_aj = this.field_76647_h;
         this.field_76645_j[k].add(entityIn);
         this.field_76643_l = true;
         ci.cancel();
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/chunk/Chunk;func_177414_a(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"
   )
   public void depthsupdate$getEntitiesWithinAABBForEntity(
      @Nullable Entity entityIn, @NonNull AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> filter, CallbackInfo ci
   ) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         int startY = MathHelper.func_76128_c((aabb.field_72338_b - World.MAX_ENTITY_RADIUS) / 16.0);
         int endY = MathHelper.func_76128_c((aabb.field_72337_e + World.MAX_ENTITY_RADIUS) / 16.0);
         startY = MathHelper.func_76125_a(startY, ctx.minSection(), ctx.maxSection());
         endY = MathHelper.func_76125_a(endY, ctx.minSection(), ctx.maxSection());

         for (int y = startY; y <= endY; y++) {
            int k = ctx.toStorageIndex(y << 4);
            if (k >= 0 && k < this.field_76645_j.length && !this.field_76645_j[k].isEmpty()) {
               for (Entity entity : this.field_76645_j[k]) {
                  if (entity.func_174813_aQ().func_72326_a(aabb) && entity != entityIn) {
                     if (filter == null || filter.apply(entity)) {
                        listToFill.add(entity);
                     }

                     Entity[] aentity = entity.func_70021_al();
                     if (aentity != null) {
                        for (Entity entity1 : aentity) {
                           if (entity1 != entityIn && entity1.func_174813_aQ().func_72326_a(aabb) && (filter == null || filter.apply(entity1))) {
                              listToFill.add(entity1);
                           }
                        }
                     }
                  }
               }
            }
         }

         ci.cancel();
      }
   }

   @Inject(
      at = @At("HEAD"),
      cancellable = true,
      method = "Lnet/minecraft/world/chunk/Chunk;func_177430_a(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"
   )
   public <T extends Entity> void depthsupdate$getEntitiesOfTypeWithinAABB(
      Class<? extends T> entityClass, @NonNull AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter, CallbackInfo ci
   ) {
      if (this.depthsupdate$isExtended()) {
         HeightContext ctx = this.depthsupdate$ctx();
         int startY = MathHelper.func_76128_c((aabb.field_72338_b - World.MAX_ENTITY_RADIUS) / 16.0);
         int endY = MathHelper.func_76128_c((aabb.field_72337_e + World.MAX_ENTITY_RADIUS) / 16.0);
         startY = MathHelper.func_76125_a(startY, ctx.minSection(), ctx.maxSection());
         endY = MathHelper.func_76125_a(endY, ctx.minSection(), ctx.maxSection());

         for (int y = startY; y <= endY; y++) {
            int k = ctx.toStorageIndex(y << 4);
            if (k >= 0 && k < this.field_76645_j.length) {
               for (T t : this.field_76645_j[k].func_180215_b(entityClass)) {
                  if (t.func_174813_aQ().func_72326_a(aabb) && (filter == null || filter.apply(t))) {
                     listToFill.add(t);
                  }
               }
            }
         }

         ci.cancel();
      }
   }
}
