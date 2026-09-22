package sayys.depthsupdate.mixin.mod.celeritas;

import org.embeddedt.embeddium.impl.util.position.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.taumc.celeritas.impl.world.WorldSlice;
import sayys.depthsupdate.core.HeightManager;

@Mixin(value = WorldSlice.class, remap = false)
public class MixinCeleritasWorldSlice {
   @Redirect(at = @At(value = "INVOKE", ordinal = 0, target = "Lorg/embeddedt/embeddium/impl/util/position/SectionPos;y()I"), method = "prepare")
   private static int depthsupdate$remapSectionYForStorageAccess(SectionPos origin) {
      int sectionY = origin.y();
      return HeightManager.getMaxContext().toStorageIndex(sectionY << 4);
   }
}
