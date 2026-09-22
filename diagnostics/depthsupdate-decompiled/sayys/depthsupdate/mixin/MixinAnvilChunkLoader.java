package sayys.depthsupdate.mixin;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(AnvilChunkLoader.class)
public abstract class MixinAnvilChunkLoader {
   @Unique
   private static final ThreadLocal<HeightContext> depthsupdate$ctx = ThreadLocal.withInitial(() -> HeightContext.VANILLA);
   @Unique
   private static final ThreadLocal<Integer> depthsupdate$nestingLevel = ThreadLocal.withInitial(() -> 0);

   @Inject(
      at = @At("HEAD"),
      method = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;func_75823_a(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;"
   )
   private void depthsupdate$startRead(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
      depthsupdate$ctx.set(HeightManager.get(worldIn));
      depthsupdate$nestingLevel.set(Math.max(0, depthsupdate$nestingLevel.get()) + 1);
   }

   @Inject(
      at = @At("RETURN"),
      method = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;func_75823_a(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;"
   )
   private void depthsupdate$endRead(World worldIn, NBTTagCompound compound, CallbackInfoReturnable<Chunk> cir) {
      int depth = depthsupdate$nestingLevel.get() - 1;
      if (depth <= 0) {
         depthsupdate$ctx.remove();
         depthsupdate$nestingLevel.remove();
      } else {
         depthsupdate$nestingLevel.set(depth);
      }
   }

   @ModifyConstant(
      constant = @Constant(intValue = 16),
      method = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;func_75823_a(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;"
   )
   private int depthsupdate$modifyStorageArraysSize(int original) {
      HeightContext ctx = depthsupdate$ctx.get();
      return ctx.isExtended() ? ctx.totalStorageSections() : original;
   }

   @Redirect(
      at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NBTTagCompound;func_74771_c(Ljava/lang/String;)B"),
      method = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;func_75823_a(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;"
   )
   private byte depthsupdate$offsetY(@NonNull NBTTagCompound compound, String key) {
      byte b = compound.func_74771_c(key);
      HeightContext ctx = depthsupdate$ctx.get();
      return !"Y".equals(key) || !ctx.isExtended() || !compound.func_74764_b("Blocks") && !compound.func_74764_b("Palette")
         ? b
         : (byte)ctx.toStorageIndex(b << 4);
   }

   @Redirect(
      at = @At(value = "NEW", target = "Lnet/minecraft/world/chunk/storage/ExtendedBlockStorage;"),
      method = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;func_75823_a(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/world/chunk/Chunk;"
   )
   @Contract("_, _ -> new")
   private @NonNull ExtendedBlockStorage depthsupdate$fixConstructorY(int y, boolean storeSkylight) {
      HeightContext ctx = depthsupdate$ctx.get();
      return ctx.isExtended() ? new ExtendedBlockStorage(ctx.fromStorageIndex(y >> 4) << 4, storeSkylight) : new ExtendedBlockStorage(y, storeSkylight);
   }

   @Inject(
      at = @At("HEAD"),
      method = "Lnet/minecraft/world/chunk/storage/AnvilChunkLoader;func_75820_a(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)V"
   )
   private void depthsupdate$markExtendedChunk(Chunk chunkIn, World worldIn, NBTTagCompound compound, CallbackInfo ci) {
      if (HeightManager.isExtended(worldIn)) {
         compound.func_74757_a("DepthsUpdateExtended", true);
      }
   }
}
