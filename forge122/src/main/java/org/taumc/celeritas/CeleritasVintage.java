package org.taumc.celeritas;

import com.mojang.realmsclient.gui.ChatFormatting;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.embeddedt.embeddium.impl.common.util.MathUtil;
import org.embeddedt.embeddium.impl.common.util.NativeBuffer;
import org.embeddedt.embeddium.impl.gl.device.GLRenderDevice;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.taumc.celeritas.impl.compat.betterfoliage.BetterFoliageCompat;
import org.taumc.celeritas.impl.compat.nothirium.NothiriumCompatibility;
import org.taumc.celeritas.impl.command.TogglePassCommand;
import org.taumc.celeritas.impl.gui.SodiumGameOptions;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;

@Mod(modid = CeleritasVintage.MODID, useMetadata = true)
public class CeleritasVintage {
    public static final String MODID = "celeritas";
    private static final Logger LOGGER = LogManager.getLogger("Pintonium");
    public static String VERSION;
    private static final SodiumGameOptions CONFIG = loadConfig();

    @EventHandler
    public void onConstruct(FMLConstructionEvent event) {
        if (!NothiriumCompatibility.isPresent()) {
            GLRenderDevice.VANILLA_STATE_RESETTER = () -> {
                GL30.glBindVertexArray(0);
                OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
            };
        } else {
            LOGGER.info("Nothirium coexistence preview: Pintonium terrain, video options and shaders are inactive.");
        }
        VERSION = Loader.instance().getIndexedModList().get(MODID).getVersion();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event) {
        if (!NothiriumCompatibility.isPresent() && Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"))) {
            ClientCommandHandler.instance.registerCommand(new TogglePassCommand());
        }

    }

    @SubscribeEvent
    public void onF3Text(RenderGameOverlayEvent.Text event) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        var strings = event.getRight();
        strings.add("");
        if (NothiriumCompatibility.isPresent()) {
            strings.add("%sPintonium (%s): Nothirium coexistence".formatted(ChatFormatting.GREEN, VERSION));
            strings.add("Shaderpack bridge: not yet available");
            return;
        }
        strings.add("%s%s Renderer (%s)".formatted(ChatFormatting.GREEN, "Pintonium", VERSION));

        // Embeddium: Show a lot less with reduced debug info
        if (Minecraft.getMinecraft().isReducedDebug()) {
            return;
        }

        if (Loader.isModLoaded("betterfoliage")) {
            strings.add("Foliage compatibility: " + BetterFoliageCompat.getDebugStatus());
        }

        var renderer = CeleritasWorldRenderer.instanceNullable();

        if (renderer != null) {
            strings.addAll(renderer.getDebugStrings());
        }

        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);

            if (str.startsWith("Allocated:")) {
                strings.add(i + 1, getNativeMemoryString());

                break;
            }
        }
    }

    private static String getNativeMemoryString() {
        return "Off-Heap: +" + MathUtil.toMib(getNativeMemoryUsage()) + "MB";
    }

    private static long getNativeMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed() + NativeBuffer.getTotalAllocated();
    }

    public static Logger logger() {
        return LOGGER;
    }

    private static SodiumGameOptions loadConfig() {
        try {
            return SodiumGameOptions.load();
        } catch (Exception e) {
            LOGGER.error("Failed to load configuration file", e);
            LOGGER.error("Using default configuration file in read-only mode");
            SodiumGameOptions config = new SodiumGameOptions();
            config.setReadOnly();
            return config;
        }
    }

    public static SodiumGameOptions options() {
        if (CONFIG == null) {
            throw new IllegalStateException("Config not yet available");
        } else {
            return CONFIG;
        }
    }
}
