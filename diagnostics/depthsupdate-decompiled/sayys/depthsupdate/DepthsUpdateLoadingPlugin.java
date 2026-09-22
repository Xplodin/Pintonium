package sayys.depthsupdate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.Nullable;
import zone.rong.mixinbooter.IEarlyMixinLoader;

public class DepthsUpdateLoadingPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {
   public List<String> getMixinConfigs() {
      return Arrays.asList("depthsupdate.default.mixin.json", "depthsupdate.mod.mixin.json");
   }

   @Nullable
   public String[] getASMTransformerClass() {
      return null;
   }

   @Nullable
   public String getModContainerClass() {
      return null;
   }

   @Nullable
   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> map) {
   }

   @Nullable
   public String getAccessTransformerClass() {
      return null;
   }
}
