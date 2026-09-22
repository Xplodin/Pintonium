package dev.beanpack.nullsafe;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("BeanPack Null-Safe Crash Reporter")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"dev.beanpack.nullsafe"})
public final class NullSafeCrashPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {NullSafeCrashTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // No runtime configuration is needed.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
