package dev.beanpack.memory;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.Name("BeanPack Resource Path Memory Fix")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.TransformerExclusions({"dev.beanpack.memory"})
public final class ResourcePathPlugin implements IFMLLoadingPlugin {
    public String[] getASMTransformerClass() { return new String[]{"dev.beanpack.memory.ResourcePathTransformer"}; }
    public String getModContainerClass() { return null; }
    public String getSetupClass() { return null; }
    public void injectData(Map<String, Object> data) {}
    public String getAccessTransformerClass() { return null; }
}
