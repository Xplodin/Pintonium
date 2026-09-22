package sayys.depthsupdate;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ModMixinConfigPlugin implements IMixinConfigPlugin {
   private static boolean isClassPresent(String className) {
      try {
         Class.forName(className, false, ModMixinConfigPlugin.class.getClassLoader());
         return true;
      } catch (LinkageError | ClassNotFoundException var2) {
         return false;
      }
   }

   public void onLoad(String s) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      if (mixinClassName.endsWith(".MixinRenderGlobalChunkOffset")) {
         return !isClassPresent("optifine.OptiFineForgeTweaker");
      } else if (mixinClassName.contains(".optifine.")) {
         return isClassPresent("optifine.OptiFineForgeTweaker");
      } else if (mixinClassName.contains(".mod.nothirium.")) {
         return isClassPresent("meldexun.nothirium.mc.Nothirium");
      } else if (mixinClassName.contains(".mod.celeritas.")) {
         return isClassPresent("org.taumc.celeritas.CeleritasVintage");
      } else {
         return mixinClassName.contains(".mod.rltweaker.") ? isClassPresent("com.charles445.rltweaker.RLTweaker") : true;
      }
   }

   public void acceptTargets(Set<String> set, Set<String> set1) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
   }

   public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {
   }
}
