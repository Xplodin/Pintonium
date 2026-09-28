package net.irisshaders.iris;

import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.compat.dh.DHCompatInternal;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.pipeline.CommonIrisRenderingPipeline;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.PipelineManager;
import net.irisshaders.iris.pipeline.VintageIrisRenderingPipeline;
import net.irisshaders.iris.pipeline.VintageVanillaRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.DimensionId;
import net.irisshaders.iris.shaderpack.materialmap.BlockEntry;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import org.embeddedt.embeddium.compat.iris.IBlockEntry;
import org.embeddedt.embeddium.compat.mc.MinecraftVersionShimService;
import org.embeddedt.embeddium.impl.gl.device.GLRenderDevice;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import org.taumc.celeritas.CeleritasShaderVersionService;
import org.taumc.celeritas.mixin.shaders.accessor.MixinEntityRendererLightmapAccessor;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import static com.mitchej123.glsm.GLStateManagerService.GL_STATE_MANAGER;
import static net.irisshaders.iris.IrisLogging.IRIS_LOGGER;

public class IrisVintage implements CeleritasShaderVersionService {
    private static boolean renderSystemInitialized;

    @Override
    public void reloadIris() throws IOException {
        reload();
    }

    @Override
    public boolean irisAllowConcurrentUpdate() {
        return false;
    }

    @Override
    public void handleUnsupportedFeatureFlags(List<FeatureFlags> invalidFlagList, List<String> invalidFeatureFlags) {
        // TODO: show a 1.12-compatible error screen once shader selection UI is ported.
    }

    @Override
    public void processBiomeMap(BiConsumer<String, String> define) {
        Set<String> definedKeys = new HashSet<>();

        for (Biome biome : Biome.REGISTRY) {
            ResourceLocation location = Biome.REGISTRY.getNameForObject(biome);
            if (location != null) {
                String key = "BIOME_" + location.getPath().toUpperCase(Locale.ROOT);
                define.accept(key, String.valueOf(Biome.getIdForBiome(biome)));
                definedKeys.add(key);
            }
        }

        defineModernBiomeAliases(define, definedKeys);
    }

    private static void defineModernBiomeAliases(BiConsumer<String, String> define, Set<String> definedKeys) {
        defineBiomeAlias(define, definedKeys, "JAGGED_PEAKS", "extreme_hills");
        defineBiomeAlias(define, definedKeys, "GROVE", "taiga_cold");
        defineBiomeAlias(define, definedKeys, "SNOWY_SLOPES", "ice_mountains");
        defineBiomeAlias(define, definedKeys, "FROZEN_PEAKS", "ice_mountains");
        defineBiomeAlias(define, definedKeys, "SNOWY_PLAINS", "ice_flats");
        defineBiomeAlias(define, definedKeys, "SNOWY_BEACH", "cold_beach");
        defineBiomeAlias(define, definedKeys, "SNOWY_TAIGA", "taiga_cold");
        defineBiomeAlias(define, definedKeys, "ICE_SPIKES", "mutated_ice_flats");
        defineBiomeAlias(define, definedKeys, "WINDSWEPT_HILLS", "extreme_hills");
        defineBiomeAlias(define, definedKeys, "WINDSWEPT_FOREST", "extreme_hills_with_trees");
        defineBiomeAlias(define, definedKeys, "WINDSWEPT_GRAVELLY_HILLS", "mutated_extreme_hills");
        defineBiomeAlias(define, definedKeys, "OLD_GROWTH_PINE_TAIGA", "redwood_taiga");
        defineBiomeAlias(define, definedKeys, "OLD_GROWTH_SPRUCE_TAIGA", "redwood_taiga_hills");
        defineBiomeAlias(define, definedKeys, "SWAMP", "swampland");
        defineBiomeAlias(define, definedKeys, "MANGROVE_SWAMP", "swampland");
        defineBiomeAlias(define, definedKeys, "MUSHROOM_FIELDS", "mushroom_island");
        defineBiomeAlias(define, definedKeys, "SPARSE_JUNGLE", "jungle_edge");
        defineBiomeAlias(define, definedKeys, "BAMBOO_JUNGLE", "jungle");
        defineBiomeAlias(define, definedKeys, "NETHER_WASTES", "hell");
        defineBiomeAlias(define, definedKeys, "CRIMSON_FOREST", "hell");
        defineBiomeAlias(define, definedKeys, "WARPED_FOREST", "hell");
        defineBiomeAlias(define, definedKeys, "BASALT_DELTAS", "hell");
        defineBiomeAlias(define, definedKeys, "SOUL_SAND_VALLEY", "hell");
        // Minecraft 1.12 only has one End biome, registered as minecraft:sky.
        // Modern shader packs still reference the post-1.13 End biome names in
        // custom uniform expressions, so map every one of them to that biome.
        defineBiomeAlias(define, definedKeys, "THE_END", "sky");
        defineBiomeAlias(define, definedKeys, "SMALL_END_ISLANDS", "sky");
        defineBiomeAlias(define, definedKeys, "END_MIDLANDS", "sky");
        defineBiomeAlias(define, definedKeys, "END_HIGHLANDS", "sky");
        defineBiomeAlias(define, definedKeys, "END_BARRENS", "sky");
        defineBiomeAlias(define, definedKeys, "PALE_GARDEN", "roofed_forest");

        defineMissingBiome(define, definedKeys, "CHERRY_GROVE");
        defineMissingBiome(define, definedKeys, "LUSH_CAVES");
        defineMissingBiome(define, definedKeys, "DEEP_DARK");
    }

    private static void defineBiomeAlias(BiConsumer<String, String> define, Set<String> definedKeys, String alias, String legacyName) {
        String key = "BIOME_" + alias;
        if (definedKeys.contains(key)) {
            return;
        }

        Biome biome = Biome.REGISTRY.getObject(new ResourceLocation("minecraft", legacyName));
        define.accept(key, biome == null ? "-1" : String.valueOf(Biome.getIdForBiome(biome)));
        definedKeys.add(key);
    }

    private static void defineMissingBiome(BiConsumer<String, String> define, Set<String> definedKeys, String alias) {
        String key = "BIOME_" + alias;
        if (!definedKeys.contains(key)) {
            define.accept(key, "-1");
            definedKeys.add(key);
        }
    }

    @Override
    public IBlockEntry parseBlockEntry(@NotNull String entry) {
        if (entry.isEmpty()) {
            throw new IllegalArgumentException("Called BlockEntry::parse with an empty string");
        }

        String[] parts = entry.split(":");
        if (parts.length == 1) {
            return new BlockEntry(new NamespacedId("minecraft", parts[0]), Collections.emptySet());
        }

        if (parts.length == 2 && !startsWithDigit(parts[1]) && !isStatePredicate(parts[1])) {
            return new BlockEntry(new NamespacedId(parts[0], parts[1]), Collections.emptySet());
        }

        int statesStart;
        NamespacedId id;
        if (parts.length >= 2 && (startsWithDigit(parts[1]) || isStatePredicate(parts[1]))) {
            statesStart = 1;
            id = new NamespacedId("minecraft", parts[0]);
        } else {
            statesStart = 2;
            id = new NamespacedId(parts[0], parts[1]);
        }

        Set<Integer> metadataIds = new HashSet<>();
        Map<String, String> propertyPredicates = new HashMap<>();
        for (int i = statesStart; i < parts.length; i++) {
            for (String metaPart : parts[i].split(",")) {
                String trimmed = metaPart.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                int equalsIndex = trimmed.indexOf('=');
                if (equalsIndex > 0 && equalsIndex + 1 < trimmed.length()) {
                    propertyPredicates.put(trimmed.substring(0, equalsIndex), trimmed.substring(equalsIndex + 1));
                } else if (isMetadataId(trimmed)) {
                    metadataIds.add(Integer.parseInt(trimmed));
                }
            }
        }

        return new BlockEntry(id, metadataIds, Collections.unmodifiableMap(propertyPredicates));
    }

    private static boolean startsWithDigit(String value) {
        return !value.isEmpty() && Character.isDigit(value.charAt(0));
    }

    private static boolean isStatePredicate(String value) {
        return value.indexOf('=') >= 0;
    }

    private static boolean isMetadataId(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private static void ensureRenderSystemInitialized() {
        if (!renderSystemInitialized) {
            IrisRenderSystem.initRenderer();
            renderSystemInitialized = true;
        }
    }

    @Override
    public IBlockEntry createBlockEntry(NamespacedId id) {
        return new BlockEntry(id, Collections.emptySet());
    }

    @Override
    public void onEarlyInitialize() {
        installVanillaStateResetter();
        DHCompat.run();
    }

    @Override
    public void onRenderSystemInit() {
        installVanillaStateResetter();
        ensureRenderSystemInitialized();
        if (!MinecraftVersionShimService.MINECRAFT_SHIM.isDHPresent()) {
            IrisCommon.loadShaderpack();
        }
    }

    @Override
    public void onLoadingComplete() {
        installVanillaStateResetter();

        // DH can defer loading the selected shader pack until after its API is ready.
        // Resolve that deferred load before deciding whether the menu needs a pipeline;
        // otherwise the manager call below can activate shaders after the condition was
        // evaluated and accidentally construct a world pipeline inside the main menu.
        PipelineManager pipelineManager = IrisCommon.getPipelineManager();

        if (MinecraftVersionShimService.MINECRAFT_SHIM.isLevelLoaded()) {
            pipelineManager.preparePipeline(getCurrentDimension());
            resetVanillaGlState();
        } else {
            if (!isShaderPackActive()) {
                pipelineManager.preparePipeline(DimensionId.OVERWORLD);
            }
            resetMenuGlState();
        }
    }

    @Override
    public void destroyEverything() {
        IrisCommon.getPipelineManager().destroyPipeline();
        if (MinecraftVersionShimService.MINECRAFT_SHIM.isLevelLoaded()) {
            resetVanillaGlState();
        } else {
            resetMenuGlState();
        }
    }

    @Override
    public WorldRenderingPipeline createPipeline(NamespacedId dimensionId) {
        if (IrisCommon.getCurrentPack().isEmpty() || !IrisCommon.getIrisConfig().areShadersEnabled()) {
            IrisCommon.setFallback(false);
            return createVanillaRenderingPipeline();
        }

        try {
            ensureRenderSystemInitialized();
            ProgramSet programs = IrisCommon.getCurrentPack().get().getProgramSet(dimensionId);
            IrisCommon.setFallback(false);
            return new VintageIrisRenderingPipeline(programs);
        } catch (Exception e) {
            IRIS_LOGGER.error("Failed to create the 1.12 shader rendering pipeline, falling back to vanilla rendering!", e);
            IrisCommon.setFallback(true);
            return createVanillaRenderingPipeline();
        }
    }

    @Override
    public WorldRenderingPipeline createVanillaRenderingPipeline() {
        return new VintageVanillaRenderingPipeline();
    }

    @Override
    public void reload() {
        try {
            IrisCommon.getIrisConfig().initialize();
            IrisCommon.destroyEverything();
            IrisCommon.loadShaderpack();

            if (MinecraftVersionShimService.MINECRAFT_SHIM.isLevelLoaded()) {
                IrisCommon.getPipelineManager().preparePipeline(getCurrentDimension());
                resetVanillaGlState();
                // A shader toggle changes the terrain programs even when the
                // shader pack's material/AO settings happen to be identical.
                // WorldRenderingSettings only requests a rebuild when one of
                // those settings changes, which leaves existing Celeritas
                // meshes bound to the programs from the previous pipeline.
                // Queue the same full renderer reload as F3+A after every
                // in-world pipeline reload so old worker results are discarded
                // and all sections are rebuilt against the new pipeline.
                MinecraftVersionShimService.MINECRAFT_SHIM.markRendererReloadRequired();
            } else if (!isShaderPackActive()) {
                IrisCommon.getPipelineManager().preparePipeline(DimensionId.OVERWORLD);
                resetMenuGlState();
            }
        } catch (IOException e) {
            IRIS_LOGGER.error("Error reloading shader pack while applying changes!", e);
        }
    }

    public static void resetVanillaGlState() {
        resetGlState(true);
    }

    public static void resetMenuGlState() {
        resetGlState(false);
    }

    private static void resetGlState(boolean bindWorldTextures) {
        installVanillaStateResetter();
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.getFramebuffer() == null) {
            return;
        }

        MinecraftVersionShimService.MINECRAFT_SHIM.bindMainFramebuffer();
        GL_STATE_MANAGER.glUseProgram(0);
        resetVanillaRenderDeviceState();

        GL11.glViewport(0, 0, minecraft.getFramebuffer().framebufferWidth, minecraft.getFramebuffer().framebufferHeight);

        int drawBuffer = OpenGlHelper.isFramebufferEnabled() ? GL30.GL_COLOR_ATTACHMENT0 : GL11.GL_BACK;
        GL11.glDrawBuffer(drawBuffer);
        resetDrawBufferArray(drawBuffer);
        GL11.glReadBuffer(drawBuffer);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (bindWorldTextures && minecraft.entityRenderer != null) {
            int lightmapTexture = ((MixinEntityRendererLightmapAccessor) minecraft.entityRenderer).celeritas$getLightmapTexture().getGlTextureId();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL13.glActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.bindTexture(lightmapTexture);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightmapTexture);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
        }

        if (bindWorldTextures && minecraft.getTextureMapBlocks() != null) {
            int blockAtlasTexture = minecraft.getTextureMapBlocks().getGlTextureId();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.bindTexture(blockAtlasTexture);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockAtlasTexture);
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D();
        }
    }

    private static void installVanillaStateResetter() {
        GLRenderDevice.VANILLA_STATE_RESETTER = IrisVintage::resetVanillaRenderDeviceState;
    }

    private static void resetVanillaRenderDeviceState() {
        GL30.glBindVertexArray(0);
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private static void resetDrawBufferArray(int drawBuffer) {
        IntBuffer drawBuffers = BufferUtils.createIntBuffer(1);
        drawBuffers.put(drawBuffer);
        drawBuffers.flip();
        GL20.glDrawBuffers(drawBuffers);
    }

    private static boolean isShaderPackActive() {
        return IrisCommon.getIrisConfig().areShadersEnabled() && IrisCommon.getCurrentPack().isPresent();
    }

    public static NamespacedId getCurrentDimension() {
        WorldClient world = Minecraft.getMinecraft().world;
        if (world == null) {
            return DimensionId.OVERWORLD;
        }

        int dimension = world.provider.getDimension();
        if (dimension == -1) {
            return DimensionId.NETHER;
        }
        if (dimension == 1) {
            return DimensionId.END;
        }
        return DimensionId.OVERWORLD;
    }

    @Override
    public Object getDHCompatInstance(IrisRenderingPipeline pipeline, boolean renderDHShadow)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return new DHCompatInternal((CommonIrisRenderingPipeline) pipeline, renderDHShadow);
    }
}
