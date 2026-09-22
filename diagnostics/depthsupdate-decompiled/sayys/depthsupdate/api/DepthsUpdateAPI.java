package sayys.depthsupdate.api;

import net.minecraft.world.World;
import sayys.depthsupdate.core.HeightManager;

public final class DepthsUpdateAPI {
   private DepthsUpdateAPI() {
   }

   public static HeightInfo getHeightInfo(World world) {
      return HeightManager.get(world);
   }

   public static HeightInfo getHeightInfo(int dimensionId) {
      return HeightManager.get(dimensionId);
   }

   public static boolean isHeightExtended(World world) {
      return HeightManager.isExtended(world);
   }

   public static boolean isHeightExtended(int dimensionId) {
      return HeightManager.isExtended(dimensionId);
   }
}
