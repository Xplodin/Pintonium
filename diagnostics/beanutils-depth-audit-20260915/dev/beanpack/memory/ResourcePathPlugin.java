package dev.beanpack.memory;

import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@Name("BeanPack Resource Path Memory Fix")
@MCVersion("1.12.2")
@SortingIndex(1001)
@TransformerExclusions("dev.beanpack.memory")
public final class ResourcePathPlugin implements IFMLLoadingPlugin {
   public String[] getASMTransformerClass() {
      return new String[]{"dev.beanpack.memory.ResourcePathTransformer"};
   }

   public String getModContainerClass() {
      return null;
   }

   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> data) {
   }

   public String getAccessTransformerClass() {
      return null;
   }
}
