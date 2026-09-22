package org.taumc.celeritas.mixin.features.options;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.taumc.celeritas.CeleritasVintage;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
    @Redirect(method = "loadRenderers", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;fancyGraphics:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectGetFancyLeaves(GameSettings settings) {
        // Keep vanilla leaf opacity and face culling consistent with the terrain renderer's leaf layer.
        return CeleritasVintage.options().quality.leavesQuality.isFancy(settings.fancyGraphics);
    }
}
