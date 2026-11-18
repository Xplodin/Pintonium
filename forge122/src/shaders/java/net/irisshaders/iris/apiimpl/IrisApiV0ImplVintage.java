package net.irisshaders.iris.apiimpl;

import net.irisshaders.iris.IrisCommon;
import net.irisshaders.iris.api.v0.IrisApiConfig;
import net.irisshaders.iris.config.IrisConfig;
import net.irisshaders.iris.gui.VintageShaderPackSelectionScreen;
import net.irisshaders.iris.pipeline.VanillaRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.gui.GuiScreen;
import org.taumc.celeritas.CeleritasShaderVersionService;
import org.taumc.celeritas.api.v0.CeleritasShadersApi;

import java.io.IOException;
import java.util.Optional;
import org.taumc.celeritas.api.v0.postprocess.PostProcessingApi;

import static net.irisshaders.iris.IrisLogging.IRIS_LOGGER;

public class IrisApiV0ImplVintage implements CeleritasShadersApi {
    @Override
    public Optional<PostProcessingApi> getPostProcessing() {
        return Optional.of(VintagePostProcessing.INSTANCE.getApi());
    }

    private final IrisApiConfig config = new IrisApiConfig() {
        @Override
        public boolean areShadersEnabled() {
            IrisConfig irisConfig = IrisCommon.getIrisConfig();
            return irisConfig != null && irisConfig.areShadersEnabled();
        }

        @Override
        public void setShadersEnabledAndApply(boolean enabled) {
            IrisConfig irisConfig = IrisCommon.getIrisConfig();
            if (irisConfig == null) {
                IRIS_LOGGER.warn("Ignoring shader toggle before Iris configuration has initialized");
                return;
            }

            irisConfig.setShadersEnabled(enabled);

            try {
                irisConfig.save();
            } catch (IOException e) {
                IRIS_LOGGER.error("Error saving shader configuration file!", e);
            }

            CeleritasShaderVersionService.INSTANCE.reload();
        }
    };

    @Override
    public boolean isShaderPackInUse() {
        WorldRenderingPipeline pipeline = IrisCommon.getPipelineManager().getPipelineNullable();
        return pipeline != null && !(pipeline instanceof VanillaRenderingPipeline);
    }

    @Override
    public boolean isRenderingShadowPass() {
        return false;
    }

    @Override
    public int getOverriddenShadowDistance(int base) {
        return base;
    }

    @Override
    public boolean isShadowDistanceSliderEnabled() {
        return true;
    }

    @Override
    public boolean areDebugOptionsEnabled() {
        IrisConfig irisConfig = IrisCommon.getIrisConfig();
        return irisConfig != null && irisConfig.areDebugOptionsEnabled();
    }

    @Override
    public Object openMainIrisScreenObj(Object parent) {
        if (parent instanceof GuiScreen) {
            return new VintageShaderPackSelectionScreen((GuiScreen) parent);
        }

        return parent;
    }

    @Override
    public String getMainScreenLanguageKey() {
        return "options.iris.shaderPackSelection";
    }

    @Override
    public IrisApiConfig getConfig() {
        return this.config;
    }

    @Override
    public float getSunPathRotation() {
        WorldRenderingPipeline pipeline = IrisCommon.getPipelineManager().getPipelineNullable();
        return pipeline == null ? 0.0f : pipeline.getSunPathRotation();
    }
}
