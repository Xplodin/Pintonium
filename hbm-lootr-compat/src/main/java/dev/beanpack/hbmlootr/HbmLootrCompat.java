package dev.beanpack.hbmlootr;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = HbmLootrCompat.MODID,
        name = HbmLootrCompat.NAME,
        version = HbmLootrCompat.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:hbm;required-after:lootr@[0.6.2,)"
)
public final class HbmLootrCompat {
    public static final String MODID = "hbmlootrcompat";
    public static final String NAME = "HBM Lootr Compatibility";
    public static final String VERSION = "1.0.0";

    private CompatConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Configuration forgeConfig = new Configuration(event.getSuggestedConfigurationFile());
        forgeConfig.load();
        config = CompatConfig.load(forgeConfig);
        if (forgeConfig.hasChanged()) {
            forgeConfig.save();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new HbmLootrHandler(config));
    }
}
