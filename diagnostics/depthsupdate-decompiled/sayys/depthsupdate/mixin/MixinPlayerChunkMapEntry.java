package sayys.depthsupdate.mixin;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeModContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(PlayerChunkMapEntry.class)
public abstract class MixinPlayerChunkMapEntry {
   @Shadow
   private boolean field_187290_j;
   @Shadow
   private int field_187287_g;
   @Shadow
   private int field_187288_h;
   @Shadow
   @Final
   private PlayerChunkMap field_187282_b;
   @Shadow
   @Final
   private ChunkPos field_187284_d;
   @Shadow
   @Nullable
   private Chunk field_187286_f;
   @Shadow
   @Final
   private List<EntityPlayerMP> field_187283_c;
   @Unique
   private int[] depthsupdate$changedBlocks = new int[64];

   @Shadow
   public abstract void func_187267_a(Packet<?> var1);

   @Shadow
   protected abstract void func_187273_a(@Nullable TileEntity var1);

   @Unique
   private HeightContext depthsupdate$ctx() {
      return this.field_187286_f != null ? HeightManager.get(this.field_187286_f.func_177412_p()) : HeightContext.VANILLA;
   }

   @Unique
   private boolean depthsupdate$isExtended() {
      return this.field_187286_f != null && HeightManager.isExtended(this.field_187286_f.func_177412_p());
   }

   @ModifyConstant(constant = @Constant(intValue = 65535), method = "Lnet/minecraft/server/management/PlayerChunkMapEntry;func_187272_b()Z")
   private int depthsupdate$modifySendToPlayersMask(int original) {
      return this.depthsupdate$isExtended() ? this.depthsupdate$ctx().fullChunkSectionMask() : original;
   }

   @ModifyConstant(
      constant = @Constant(intValue = 65535),
      method = "Lnet/minecraft/server/management/PlayerChunkMapEntry;func_187278_c(Lnet/minecraft/entity/player/EntityPlayerMP;)V"
   )
   private int depthsupdate$modifySendToPlayerMask(int original) {
      return this.depthsupdate$isExtended() ? this.depthsupdate$ctx().fullChunkSectionMask() : original;
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/server/management/PlayerChunkMapEntry;func_187265_a(III)V")
   private void depthsupdate$blockChanged(int x, int y, int z, CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         ci.cancel();
         if (this.field_187290_j) {
            if (this.field_187287_g == 0) {
               this.field_187282_b.func_187304_a((PlayerChunkMapEntry)this);
            }

            HeightContext ctx = this.depthsupdate$ctx();
            int sectionY = ctx.toStorageIndex(y);
            if (sectionY < 0) {
               sectionY = 0;
            }

            if (sectionY > ctx.totalStorageSections() - 1) {
               sectionY = ctx.totalStorageSections() - 1;
            }

            this.field_187288_h |= 1 << sectionY;
            int packed = x << 28 | z << 24 | y & 65535;

            for (int i = 0; i < this.field_187287_g; i++) {
               if (this.depthsupdate$changedBlocks[i] == packed) {
                  return;
               }
            }

            if (this.field_187287_g == this.depthsupdate$changedBlocks.length) {
               this.depthsupdate$changedBlocks = Arrays.copyOf(this.depthsupdate$changedBlocks, this.depthsupdate$changedBlocks.length << 1);
            }

            this.depthsupdate$changedBlocks[this.field_187287_g++] = packed;
         }
      }
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "Lnet/minecraft/server/management/PlayerChunkMapEntry;func_187280_d()V")
   private void depthsupdate$update(CallbackInfo ci) {
      if (this.depthsupdate$isExtended()) {
         ci.cancel();
         if (this.field_187290_j && this.field_187286_f != null && this.field_187287_g != 0) {
            if (this.field_187287_g == 1) {
               int i = (this.depthsupdate$changedBlocks[0] >> 28 & 15) + this.field_187284_d.field_77276_a * 16;
               int k = (this.depthsupdate$changedBlocks[0] >> 24 & 15) + this.field_187284_d.field_77275_b * 16;
               int j = (short)(this.depthsupdate$changedBlocks[0] & 65535);
               BlockPos blockpos = new BlockPos(i, j, k);
               this.func_187267_a(new SPacketBlockChange(this.field_187282_b.func_72688_a(), blockpos));
               IBlockState state = this.field_187282_b.func_72688_a().func_180495_p(blockpos);
               if (state.func_177230_c().hasTileEntity(state)) {
                  this.func_187273_a(this.field_187282_b.func_72688_a().func_175625_s(blockpos));
               }
            } else if (this.field_187287_g >= ForgeModContainer.clumpingThreshold) {
               this.func_187267_a(new SPacketChunkData(this.field_187286_f, this.field_187288_h));
            } else {
               for (int l = 0; l < this.field_187287_g; l++) {
                  int i1 = (this.depthsupdate$changedBlocks[l] >> 28 & 15) + this.field_187284_d.field_77276_a * 16;
                  int k1 = (this.depthsupdate$changedBlocks[l] >> 24 & 15) + this.field_187284_d.field_77275_b * 16;
                  int j1 = (short)(this.depthsupdate$changedBlocks[l] & 65535);
                  BlockPos blockpos1 = new BlockPos(i1, j1, k1);
                  this.func_187267_a(new SPacketBlockChange(this.field_187282_b.func_72688_a(), blockpos1));
                  IBlockState state = this.field_187282_b.func_72688_a().func_180495_p(blockpos1);
                  if (state.func_177230_c().hasTileEntity(state)) {
                     this.func_187273_a(this.field_187282_b.func_72688_a().func_175625_s(blockpos1));
                  }
               }
            }

            this.field_187287_g = 0;
            this.field_187288_h = 0;
         }
      }
   }
}
