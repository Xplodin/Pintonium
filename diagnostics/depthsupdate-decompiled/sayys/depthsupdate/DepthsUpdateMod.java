package sayys.depthsupdate;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import sayys.depthsupdate.client.AssetHandler;
import sayys.depthsupdate.command.ScanCommand;
import sayys.depthsupdate.proxy.IProxy;
import sayys.depthsupdate.registry.RegistryHandler;
import sayys.depthsupdate.world.generation.AmethystGeodeGenerator;
import sayys.depthsupdate.world.generation.DripstoneCavesGenerator;
import sayys.depthsupdate.world.generation.LushCavesGenerator;

@Mod(modid = "depthsupdate", name = "Depths Update", version = "1.12.2-1.0.0-a12", dependencies = "after:fluidlogged_api")
public class DepthsUpdateMod {
   public static final Logger LOGGER = LogManager.getLogger("Depths Update");
   @SidedProxy(modId = "depthsupdate", clientSide = "sayys.depthsupdate.proxy.ClientProxy", serverSide = "sayys.depthsupdate.proxy.CommonProxy")
   public static IProxy proxy;

   @EventHandler
   @SideOnly(Side.CLIENT)
   public void construct(@NonNull FMLConstructionEvent event) {
      AssetHandler.setup();
   }

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      LOGGER.info("Hello From {}!", "Depths Update");
      LOGGER.info("Proxy is {}", proxy);
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      RegistryHandler.init();
      LushCavesGenerator.register();
      DripstoneCavesGenerator.register();
      AmethystGeodeGenerator.register();
   }

   @EventHandler
   public void serverStarting(FMLServerStartingEvent event) {
      event.registerServerCommand(new ScanCommand());
   }
}
