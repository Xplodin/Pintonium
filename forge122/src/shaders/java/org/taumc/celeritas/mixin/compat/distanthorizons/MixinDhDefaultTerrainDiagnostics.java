package org.taumc.celeritas.mixin.compat.distanthorizons;

import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.compat.distanthorizons.DhRenderDiagnostics;

/** Diagnostic-only observers for DH 3.3's default (no shader pack) terrain program. */
@Pseudo
@Mixin(targets = "com.seibel.distanthorizons.common.render.openGl.terrain.GlDhTerrainShaderProgram", remap = false)
public abstract class MixinDhDefaultTerrainDiagnostics {
    @Inject(method = "fillUniformData", at = @At("RETURN"), remap = false)
    private void pintonium$observeDefaultUniforms(DhApiRenderParam params, CallbackInfo ci) {
        DhRenderDiagnostics.afterUniforms(params);
    }

    @Inject(method = "bindVertexBuffer", at = @At("RETURN"), remap = false)
    private void pintonium$observeDefaultVertexBuffer(int vbo, CallbackInfo ci) {
        DhRenderDiagnostics.afterVertexBuffer(vbo);
    }
}
