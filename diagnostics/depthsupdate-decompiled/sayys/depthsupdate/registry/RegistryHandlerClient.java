package sayys.depthsupdate.registry;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@EventBusSubscriber(modid = "depthsupdate", value = Side.CLIENT)
public class RegistryHandlerClient {
   @SubscribeEvent
   public static void registerModels(ModelRegistryEvent event) {
      RegistryHandler.registerModelsCommon(event);
   }
}
