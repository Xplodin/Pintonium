package sayys.depthsupdate.mixin.mod.celeritas;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.embeddedt.embeddium.impl.gl.device.CommandList;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.taumc.celeritas.impl.render.terrain.VintageRenderPassConfigurationBuilder;
import org.taumc.celeritas.impl.render.terrain.VintageRenderSectionManager;
import sayys.depthsupdate.core.HeightContext;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = VintageRenderSectionManager.class, remap = false)
public class MixinVintageRenderSectionManager {
   @Shadow
   private WorldClient world;

   @Overwrite
   public static VintageRenderSectionManager create(ChunkVertexType vertexType, WorldClient world, int renderDistance, CommandList commandList) {
      HeightContext ctx = HeightManager.get(world);
      int minSection = ctx.isExtended() ? ctx.minSection() : 0;
      return new VintageRenderSectionManager(VintageRenderPassConfigurationBuilder.build(vertexType), world, renderDistance, commandList, minSection, 16);
   }

   @Inject(at = @At("HEAD"), cancellable = true, method = "isSectionVisuallyEmpty")
   private void depthsupdate$fixIsSectionVisuallyEmpty(int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
      Chunk chunk = this.world.func_72964_e(x, z);
      if (chunk.func_76621_g()) {
         cir.setReturnValue(true);
      } else {
         HeightContext ctx = HeightManager.get(this.world);
         ExtendedBlockStorage[] array = chunk.func_76587_i();
         int storageIndex = ctx.toStorageIndex(y << 4);
         if (storageIndex >= 0 && storageIndex < array.length) {
            cir.setReturnValue(array[storageIndex] == Chunk.field_186036_a || array[storageIndex].func_76663_a());
         } else {
            cir.setReturnValue(true);
         }
      }
   }
}
