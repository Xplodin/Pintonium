package dev.beanpack.memory;

import net.minecraft.launchwrapper.IClassTransformer;

public final class ResourcePathTransformer implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if (bytes != null
         && (
            "net.minecraft.client.resources.AbstractResourcePack".equals(name) || "net.minecraft.client.resources.AbstractResourcePack".equals(transformedName)
         )) {
         try {
            byte[] result = ResourcePathPatcher.patch(bytes);
            System.out
               .println(
                  result == bytes
                     ? "[BeanPack Memory] Asset-path formatter already optimized or not recognized; unchanged."
                     : "[BeanPack Memory] Replaced asset-path String.format with direct string construction."
               );
            return result;
         } catch (RuntimeException var5) {
            System.err.println("[BeanPack Memory] Could not patch asset path; leaving original class: " + var5);
            return bytes;
         }
      } else {
         return bytes;
      }
   }
}
