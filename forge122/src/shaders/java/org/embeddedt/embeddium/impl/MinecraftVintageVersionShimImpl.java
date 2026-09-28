package org.embeddedt.embeddium.impl;

import net.irisshaders.iris.IrisCommon;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.materialmap.VintageBlockMaterialMapping;
import net.irisshaders.iris.shaderpack.materialmap.VintageWorldRenderingSettings;
import net.irisshaders.iris.shadows.VintageShadowMatrices;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import org.embeddedt.embeddium.compat.mc.MCDynamicTexture;
import org.embeddedt.embeddium.compat.mc.MCNativeImage;
import org.embeddedt.embeddium.compat.mc.MCResourceLocation;
import org.embeddedt.embeddium.compat.mc.MCResourceManager;
import org.embeddedt.embeddium.compat.mc.MCTextureManager;
import org.embeddedt.embeddium.compat.mc.MinecraftVersionShimService;
import org.embeddedt.embeddium.compat.mc.NativeImage;
import org.embeddedt.embeddium.compat.mc.PlatformUtilService;
import org.embeddedt.embeddium.impl.resource.VintageResourceLocation;
import org.embeddedt.embeddium.impl.resource.VintageResourceManager;
import org.embeddedt.embeddium.impl.resource.VintageTextureManager;
import org.embeddedt.embeddium.impl.texture.VintageDynamicTexture;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector4f;
import org.taumc.celeritas.interfaces.IRenderTargetExt;
import org.taumc.celeritas.mixin.shaders.accessor.MixinEntityRendererLightmapAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Locale;

import static net.irisshaders.iris.IrisLogging.IRIS_LOGGER;

public class MinecraftVintageVersionShimImpl implements MinecraftVersionShimService, PlatformUtilService {
    private static final Minecraft CLIENT = Minecraft.getMinecraft();
    private static final boolean DEVELOPMENT_ENVIRONMENT = Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"));
    private static boolean rendererReloadDeferred;

    @Override
    public boolean isOnOSX() {
        return Minecraft.IS_RUNNING_ON_MAC;
    }

    @Override
    public int getMipmapLevels() {
        return CLIENT.gameSettings.mipmapLevels;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return isModLoadedSafe(modId);
    }

    @Override
    public String translate(String key, Object... args) {
        return I18n.format(key, args);
    }

    @Override
    public boolean isLevelLoaded() {
        return CLIENT.world != null;
    }

    @Override
    public int getRenderDistanceInBlocks() {
        return getEffectiveRenderDistance() * 16;
    }

    @Override
    public int getEffectiveRenderDistance() {
        return CLIENT.gameSettings.renderDistanceChunks;
    }

    @Override
    public Vector3d getUnshiftedCameraPosition() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera == null) {
            return new Vector3d();
        }

        float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();
        double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * tickDelta;
        double y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * tickDelta;
        double z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * tickDelta;
        return new Vector3d(x, y, z);
    }

    @Override
    public float getSkyAngle() {
        WorldClient world = CLIENT.world;
        return world == null ? 0.0f : world.getCelestialAngle(CapturedRenderingState.INSTANCE.getTickDelta());
    }

    @Override
    public void applyRotationYP(Matrix4f preCelestial, float degrees) {
        preCelestial.rotateY(degrees * IrisCommon.DEGREES_TO_RADIANS);
    }

    @Override
    public void applyRotationXP(Matrix4f preCelestial, float degrees) {
        preCelestial.rotateX(degrees * IrisCommon.DEGREES_TO_RADIANS);
    }

    @Override
    public void applyRotationZP(Matrix4f preCelestial, float degrees) {
        preCelestial.rotateZ(degrees * IrisCommon.DEGREES_TO_RADIANS);
    }

    @Override
    public int getMoonPhase() {
        if (this.isNoSkylightDimension()) {
            return 0;
        }

        WorldClient world = CLIENT.world;
        return world == null ? 0 : world.getMoonPhase();
    }

    @Override
    public long getDayTime() {
        WorldClient world = CLIENT.world;
        return world == null ? 0L : world.getWorldTime();
    }

    @Override
    public long getDimensionTime(long orElse) {
        return orElse;
    }

    @Override
    public boolean isCurrentDimensionNether() {
        WorldClient world = CLIENT.world;
        return world != null && world.provider.getDimension() == -1;
    }

    @Override
    public boolean isCurrentDimensionEnd() {
        WorldClient world = CLIENT.world;
        return world != null && world.provider.getDimension() == 1;
    }

    @Override
    public int getMinecraftRenderHeight() {
        return CLIENT.getFramebuffer().framebufferHeight;
    }

    @Override
    public int getMinecraftRenderWidth() {
        return CLIENT.getFramebuffer().framebufferWidth;
    }

    @Override
    public int getBedrockLevel() {
        return 0;
    }

    @Override
    public float getCloudHeight() {
        WorldClient world = CLIENT.world;
        return world == null ? 128.0f : world.provider.getCloudHeight();
    }

    @Override
    public int getHeightLimit() {
        WorldClient world = CLIENT.world;
        return world == null ? 256 : world.provider.getHeight();
    }

    @Override
    public int getLogicalHeightLimit() {
        WorldClient world = CLIENT.world;
        return world == null ? 256 : world.provider.getActualHeight();
    }

    @Override
    public boolean hasCeiling() {
        return isCurrentDimensionNether();
    }

    @Override
    public boolean hasSkyLight() {
        WorldClient world = CLIENT.world;
        return world != null && world.provider != null && world.provider.hasSkyLight();
    }

    @Override
    public float getAmbientLight() {
        return 0.0f;
    }

    @Override
    public Vector3d getPlayerLookVector() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera == null) {
            return ZERO3D;
        }

        Vec3d look = camera.getLook(CapturedRenderingState.INSTANCE.getTickDelta());
        return new Vector3d(look.x, look.y, look.z);
    }

    @Override
    public Vector3d getPlayerBodyVector() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera == null) {
            return ZERO3D;
        }

        double yaw = Math.toRadians(camera.rotationYaw);
        return new Vector3d(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    @Override
    public Vector4f getLightningBoltPosition() {
        return ZERO4F;
    }

    @Override
    public float getThunderStrength() {
        WorldClient world = CLIENT.world;
        return world == null ? 0.0f : clamp01(world.getThunderStrength(CapturedRenderingState.INSTANCE.getTickDelta()));
    }

    @Override
    public float getCurrentHealth() {
        EntityPlayer player = CLIENT.player;
        return player == null ? -1.0f : player.getHealth() / player.getMaxHealth();
    }

    @Override
    public float getCurrentHunger() {
        EntityPlayer player = CLIENT.player;
        return player == null ? -1.0f : player.getFoodStats().getFoodLevel() / 20.0f;
    }

    @Override
    public float getCurrentAir() {
        EntityPlayer player = CLIENT.player;
        return player == null ? -1.0f : player.getAir() / 300.0f;
    }

    @Override
    public float getCurrentArmor() {
        EntityPlayer player = CLIENT.player;
        return player == null ? -1.0f : player.getTotalArmorValue() / 20.0f;
    }

    @Override
    public float getMaxAir() {
        return 300.0f;
    }

    @Override
    public float getMaxHealth() {
        EntityPlayer player = CLIENT.player;
        return player == null ? -1.0f : player.getMaxHealth();
    }

    @Override
    public boolean isFirstPersonCamera() {
        return CLIENT.gameSettings.thirdPersonView == 0;
    }

    @Override
    public boolean isSpectator() {
        return CLIENT.player != null && CLIENT.player.isSpectator();
    }

    @Override
    public Vector3d getEyePosition() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera == null) {
            return new Vector3d();
        }

        return new Vector3d(camera.posX, camera.posY + camera.getEyeHeight(), camera.posZ);
    }

    @Override
    public boolean isOnGround() {
        return CLIENT.player != null && CLIENT.player.onGround;
    }

    @Override
    public boolean isHurt() {
        return CLIENT.player != null && CLIENT.player.hurtTime > 0;
    }

    @Override
    public boolean isInvisible() {
        return CLIENT.player != null && CLIENT.player.isInvisible();
    }

    @Override
    public boolean isBurning() {
        return CLIENT.player != null && CLIENT.player.isBurning();
    }

    @Override
    public boolean isSneaking() {
        return CLIENT.player != null && CLIENT.player.isSneaking();
    }

    @Override
    public boolean isSprinting() {
        return CLIENT.player != null && CLIENT.player.isSprinting();
    }

    @Override
    public Vector3d getSkyColor() {
        WorldClient world = CLIENT.world;
        Entity camera = CLIENT.getRenderViewEntity();
        if (world == null || camera == null || this.isNoSkylightDimension()) {
            return ZERO3D;
        }

        Vec3d sky = world.getSkyColor(camera, CapturedRenderingState.INSTANCE.getTickDelta());
        return new Vector3d(sky.x, sky.y, sky.z);
    }

    private boolean isNoSkylightDimension() {
        WorldClient world = CLIENT.world;
        return world != null && world.provider != null && !world.provider.hasSkyLight();
    }

    @Override
    public float getBlindness() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera instanceof EntityLivingBase living && living.isPotionActive(MobEffects.BLINDNESS)) {
            PotionEffect effect = living.getActivePotionEffect(MobEffects.BLINDNESS);
            return effect == null ? 0.0f : clamp01(effect.getDuration() / 20.0f);
        }

        return 0.0f;
    }

    @Override
    public float getDarknessFactor() {
        return 0.0f;
    }

    @Override
    public float getPlayerMood() {
        return 0.0f;
    }

    @Override
    public float getRainStrength() {
        WorldClient world = CLIENT.world;
        return world == null ? 0.0f : clamp01(world.getRainStrength(CapturedRenderingState.INSTANCE.getTickDelta()));
    }

    @Override
    public Vector2i getEyeBrightness() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera == null) {
            return ZERO2I;
        }

        int brightness = camera.getBrightnessForRender();
        return new Vector2i(brightness & 0xffff, brightness >> 16);
    }

    @Override
    public float getNightVision() {
        Entity camera = CLIENT.getRenderViewEntity();
        return camera instanceof EntityLivingBase living && living.isPotionActive(MobEffects.NIGHT_VISION) ? 1.0f : 0.0f;
    }

    @Override
    public int isEyeInWater() {
        Entity camera = CLIENT.getRenderViewEntity();
        if (camera == null) {
            return 0;
        }

        if (camera.isInsideOfMaterial(Material.WATER)) {
            return 1;
        }

        return camera.isInsideOfMaterial(Material.LAVA) ? 2 : 0;
    }

    @Override
    public boolean hideGui() {
        return CLIENT.gameSettings.hideGUI;
    }

    @Override
    public boolean isRightHanded() {
        return CLIENT.player == null || CLIENT.player.getPrimaryHand() == EnumHandSide.RIGHT;
    }

    @Override
    public float getScreenBrightness() {
        return CLIENT.gameSettings.gammaSetting;
    }

    @Override
    public Vector2i getAtlasSize() {
        return ZERO2I;
    }

    @Override
    public Vector2i getTextureSize() {
        return ZERO2I;
    }

    @Override
    public MCNativeImage createNativeImage(int width, int height, boolean useCalloc) {
        return new NativeImage(width, height, useCalloc);
    }

    @Override
    public MCNativeImage readNativeImage(InputStream textureStream) throws IOException {
        return NativeImage.read(textureStream);
    }

    @Override
    public MCNativeImage readNativeImage(ByteBuffer textureData) throws IOException {
        return NativeImage.read(textureData);
    }

    @Override
    public MCNativeImage[] createNativeImageArray(int size) {
        return new MCNativeImage[size];
    }

    @Override
    public MCResourceLocation makeResourceLocation(String namespace, String path) {
        return new VintageResourceLocation(new ResourceLocation(namespace, path));
    }

    @Override
    public MCResourceLocation makeResourceLocation(String str) {
        return new VintageResourceLocation(new ResourceLocation(str));
    }

    @Override
    public String getOsString() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            return "MC_OS_MAC";
        }
        if (os.contains("win")) {
            return "MC_OS_WINDOWS";
        }
        return "MC_OS_LINUX";
    }

    @Override
    public String getMcVersion() {
        return Loader.MC_VERSION;
    }

    @Override
    public String getBackupVersionNumber() {
        return "1.12.2";
    }

    @Override
    public void markRendererReloadRequired() {
        if (CLIENT.renderGlobal == null) {
            return;
        }

        if (!rendererReloadDeferred && shouldDeferRendererReload()) {
            IRIS_LOGGER.info("Deferring 1.12 renderer reload until the client has finished entering the world");
        }

        rendererReloadDeferred = true;
    }

    public static void processDeferredRendererReload() {
        if (!rendererReloadDeferred || CLIENT.renderGlobal == null || shouldDeferRendererReload()) {
            return;
        }

        rendererReloadDeferred = false;
        reloadRendererNow("deferred shader pipeline change");
    }

    private static boolean shouldDeferRendererReload() {
        if (CLIENT.world == null) {
            return true;
        }

        if (CLIENT.player == null || CLIENT.getConnection() == null) {
            return true;
        }

        GuiScreen currentScreen = CLIENT.currentScreen;
        if (currentScreen == null) {
            return false;
        }

        String screenName = currentScreen.getClass().getSimpleName();
        return "GuiScreenWorking".equals(screenName) || "GuiDownloadTerrain".equals(screenName);
    }

    private static void reloadRendererNow(String reason) {
        long start = System.nanoTime();
        CLIENT.renderGlobal.loadRenderers();

        long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
        if (elapsedMillis > 500L) {
            IRIS_LOGGER.info("Completed 1.12 renderer reload for {} in {} ms", reason, elapsedMillis);
        }
    }

    @Override
    public boolean isDHPresent() {
        // Shader initialization runs before Forge has populated Loader.namedMods.
        return MinecraftVintageVersionShimImpl.class.getResource(
                "/com/seibel/distanthorizons/api/DhApi.class") != null;
    }

    @Override
    public Matrix4f getShadowModelView(float sunPathRotation, float intervalSize) {
        return VintageShadowMatrices.createModelViewMatrix(sunPathRotation, intervalSize);
    }

    @Override
    public Matrix4f getShadowProjection(float shadowDistance, float nearPlane, float farPlane) {
        return VintageShadowMatrices.createOrthoMatrix(shadowDistance, nearPlane, farPlane);
    }

    @Override
    public void bindMainFramebuffer() {
        CLIENT.getFramebuffer().bindFramebuffer(true);
    }

    @Override
    public void unbindMainFramebuffer() {
        CLIENT.getFramebuffer().unbindFramebuffer();
    }

    @Override
    public int getMainFramebufferWidth() {
        return CLIENT.getFramebuffer().framebufferWidth;
    }

    @Override
    public int getMainFramebufferHeight() {
        return CLIENT.getFramebuffer().framebufferHeight;
    }

    @Override
    public int getColorTextureId() {
        Framebuffer framebuffer = CLIENT.getFramebuffer();
        return framebuffer.framebufferTexture;
    }

    @Override
    public int getColorBufferVersion() {
        Framebuffer framebuffer = CLIENT.getFramebuffer();
        return ((IRenderTargetExt) framebuffer).iris$getColorBufferVersion();
    }

    @Override
    public int getLightTextureId() {
        return ((MixinEntityRendererLightmapAccessor) CLIENT.entityRenderer).celeritas$getLightmapTexture().getGlTextureId();
    }

    @Override
    public int getMissingTextureId() {
        return TextureUtil.MISSING_TEXTURE.getGlTextureId();
    }

    @Override
    public MCDynamicTexture createDynamicTexture(MCNativeImage image) {
        return new VintageDynamicTexture((NativeImage) image);
    }

    @Override
    public MCTextureManager getTextureManager() {
        return new VintageTextureManager(CLIENT.getTextureManager());
    }

    @Override
    public MCResourceManager getResourceManager() {
        return new VintageResourceManager(CLIENT.getResourceManager());
    }

    @Override
    public void populateBlockIds(ShaderPack pack) {
        VintageWorldRenderingSettings.INSTANCE.setBlockStateIds(
                VintageBlockMaterialMapping.createBlockStateIdMap(pack.getIdMap().getBlockProperties()));
    }

    @Override
    public boolean isLoadValid() {
        return true;
    }

    @Override
    public boolean modPresent(String modid) {
        return isModLoadedSafe(modid);
    }

    @Override
    public String getModName(String modId) {
        var container = Loader.instance().getIndexedModList().get(modId);
        return container == null ? modId : container.getName();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return DEVELOPMENT_ENVIRONMENT;
    }

    @Override
    public Path getConfigDir() {
        return getGameDirPath().resolve("config");
    }

    @Override
    public Path getGameDir() {
        return getGameDirPath();
    }

    private static boolean isModLoadedSafe(String modId) {
        try {
            return Loader.isModLoaded(modId);
        } catch (RuntimeException e) {
            return Loader.instance().getIndexedModList().containsKey(modId);
        }
    }

    private static Path getGameDirPath() {
        if (CLIENT != null && CLIENT.gameDir != null) {
            return CLIENT.gameDir.toPath();
        }

        Object[] injectionData = FMLInjectionData.data();
        if (injectionData.length > 6 && injectionData[6] instanceof File) {
            return ((File) injectionData[6]).toPath();
        }

        return new File(".").toPath();
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
