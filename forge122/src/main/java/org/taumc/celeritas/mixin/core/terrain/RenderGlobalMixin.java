package org.taumc.celeritas.mixin.core.terrain;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.irisshaders.iris.IrisCommon;
import net.irisshaders.iris.IrisVintage;
import net.irisshaders.iris.pipeline.CommonIrisRenderingPipeline;
import net.irisshaders.iris.pipeline.VintageIrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.render.terrain.SimpleWorldRenderer;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.taumc.celeritas.impl.render.terrain.CeleritasWorldRenderer;
import org.taumc.celeritas.mixin.shaders.accessor.MixinEntityRendererLightmapAccessor;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.*;
import java.util.function.Consumer;

@Mixin(RenderGlobal.class)
public abstract class RenderGlobalMixin implements SimpleWorldRenderer.Provider<CeleritasWorldRenderer> {
    @Unique
    private static final int celeritas$MODERN_ENTITY_MATERIAL_RANGE_START = 50000;

    @Unique
    private static final int celeritas$MODERN_ENTITY_MATERIAL_RANGE_END = 50128;

    @Unique
    private static final NamespacedId celeritas$CHICKEN_ENTITY_ID = new NamespacedId("minecraft", "chicken");

    @Unique
    private static final NamespacedId celeritas$ENDER_DRAGON_ENTITY_ID = new NamespacedId("minecraft", "ender_dragon");

    @Unique
    private static final float celeritas$NO_SKYLIGHT_ENTITY_LIGHT_FLOOR = 160.0F;

    @Unique
    private static final int celeritas$IRIS_ENTITY_ATTRIBUTE_INDEX = 11;

    @Shadow
    @Final
    private Map<Integer, DestroyBlockProgress> damagedBlocks;

    @Shadow @Final private Minecraft mc;
    @Shadow
    @Final
    private RenderManager renderManager;
    @Shadow
    private int countEntitiesRendered;

    @Shadow
    protected abstract boolean isOutlineActive(Entity entityIn, Entity viewer, ICamera camera);

    @Shadow
    private WorldClient world;
    private CeleritasWorldRenderer renderer;

    @Redirect(method = "loadRenderers", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;renderDistanceChunks:I", ordinal = 1))
    private int nullifyBuiltChunkStorage(GameSettings settings) {
        // Do not allow any resources to be allocated
        return 0;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(Minecraft minecraft, CallbackInfo ci) {
        this.renderer = new CeleritasWorldRenderer();
    }

    @Override
    public CeleritasWorldRenderer celeritas$getWorldRenderer() {
        return this.renderer;
    }

    @Inject(method = "setWorldAndLoadRenderers", at = @At("RETURN"))
    private void onWorldChanged(WorldClient world, CallbackInfo ci) {
        RenderDevice.enterManagedCode();

        try {
            this.renderer.setWorld(world);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    /**
     * @reason Redirect to our renderer
     * @author JellySquid
     */
    @Overwrite
    public int getRenderedChunks() {
        return this.renderer.getVisibleChunkCount();
    }

    /**
     * @reason Redirect the check to our renderer
     * @author JellySquid
     */
    @Overwrite
    public boolean hasNoChunkUpdates() {
        return this.renderer.isTerrainRenderComplete();
    }

    @Unique
    private void celeritas$bindTerrainLightmap() {
        int lightmapTexture = ((MixinEntityRendererLightmapAccessor) this.mc.entityRenderer).celeritas$getLightmapTexture().getGlTextureId();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL13.glActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.bindTexture(lightmapTexture);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightmapTexture);
        GlStateManager.enableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Unique
    private void celeritas$bindMainFramebufferForVanillaTerrain() {
        WorldRenderingPipeline pipeline = IrisCommon.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof CommonIrisRenderingPipeline) {
            ((CommonIrisRenderingPipeline) pipeline).bindDefault();
        } else {
            IrisVintage.resetVanillaGlState();
        }

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Inject(method = "setDisplayListEntitiesDirty", at = @At("RETURN"))
    private void onTerrainUpdateScheduled(CallbackInfo ci) {
        this.renderer.scheduleTerrainUpdate();
    }

    /**
     * @reason Redirect the chunk layer render passes to our renderer
     * @author JellySquid
     */
    @Overwrite
    public int renderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        RenderDevice.enterManagedCode();

        this.celeritas$bindMainFramebufferForVanillaTerrain();

        RenderHelper.disableStandardItemLighting();

        int blockAtlasTexture = this.mc.getTextureMapBlocks().getGlTextureId();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL13.glActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.bindTexture(blockAtlasTexture);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, blockAtlasTexture);
        GlStateManager.enableTexture2D();

        this.mc.entityRenderer.enableLightmap();
        this.celeritas$bindTerrainLightmap();

        double d3 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks;
        double d4 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks;
        double d5 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks;

        try {
            this.renderer.drawChunkLayer(blockLayerIn, d3, d4, d5);
        } finally {
            RenderDevice.exitManagedCode();
        }

        this.mc.entityRenderer.disableLightmap();

        return 1;
    }

    /**
     * @reason Redirect the terrain setup phase to our renderer
     * @author JellySquid
     */
    @Overwrite
    public void setupTerrain(Entity entity, double tick, ICamera camera, int frame, boolean spectator) {
        RenderDevice.enterManagedCode();

        try {
            this.renderer.setupTerrain(((ViewportProvider)camera).sodium$createViewport(), (float)tick, frame, spectator, false);
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    /**
     * @reason Redirect chunk updates to our renderer
     * @author JellySquid
     */
    @Overwrite
    private void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean important) {
        this.renderer.scheduleRebuildForBlockArea(minX, minY, minZ, maxX, maxY, maxZ, important);
    }

    // The following two redirects force light updates to trigger chunk updates and not check vanilla's chunk renderer
    // flags
    @Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher;hasNoFreeRenderBuilders()Z"))
    private boolean alwaysHaveBuilders(ChunkRenderDispatcher instance) {
        return false;
    }

    @Redirect(method = "updateClouds", at = @At(value = "INVOKE", target = "Ljava/util/Set;isEmpty()Z", ordinal = 1))
    private boolean alwaysHaveNoTasks(Set instance) {
        return true;
    }

    @Inject(method = "loadRenderers", at = @At("RETURN"))
    private void onReload(CallbackInfo ci) {
        RenderDevice.enterManagedCode();

        try {
            this.renderer.reload();
        } finally {
            RenderDevice.exitManagedCode();
        }
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderHelper;enableStandardItemLighting()V", shift = At.Shift.AFTER, ordinal = 1), cancellable = true)
    public void sodium$renderTileEntities(Entity entity, ICamera camera, float partialTicks, CallbackInfo ci) {
        VintageIrisRenderingPipeline irisEntityPipeline = this.celeritas$getVintageIrisPipeline();
        boolean irisBlockEntityRendering = irisEntityPipeline != null && irisEntityPipeline.beginVintageBlockEntityRendering();
        Runnable prepareBlockEntityRenderState = () -> {
            this.celeritas$prepareVanillaEntityRenderState(!irisBlockEntityRendering, false, partialTicks);
            if (irisBlockEntityRendering) {
                irisEntityPipeline.updateVintageBlockEntityUniforms();
            }
        };

        try {
            prepareBlockEntityRenderState.run();
            this.renderer.renderBlockEntities(new CeleritasWorldRenderer.TileEntityRenderContext(damagedBlocks, partialTicks, prepareBlockEntityRenderState));
        } finally {
            if (irisBlockEntityRendering) {
                irisEntityPipeline.endVintageBlockEntityRendering();
            }
        }

        this.mc.entityRenderer.disableLightmap();
        this.mc.profiler.endSection();
        ci.cancel();
    }

    /**
     * @reason Replace the debug string
     * @author JellySquid
     */
    @Overwrite
    public String getDebugInfoRenders() {
        return this.renderer.getChunksDebugString();
    }

    private List<Entity>[] getLoadedEntityList() {
        int numPasses = 2;
        List<Entity>[] passesArray = new List[numPasses];
        for (int i = 0; i < numPasses; i++) {
            passesArray[i] = new ArrayList<>();
        }
        Consumer<Entity> addEntity = entity -> {
            for (int i = 0; i < numPasses; i++) {
                if (entity.shouldRenderInPass(i)) {
                    passesArray[i].add(entity);
                }
            }
        };
        // Iterate directly over chunk entity lists where possible - mods may create multipart entities that are not
        // added to the main loadedEntityList.
        if (this.world.getChunkProvider() instanceof ChunkProviderClientAccessor provider) {
            var loadedChunks = provider.celeritas$getLoadedChunks();
            List<Entity> allEntities = new ArrayList<>(this.world.loadedEntityList.size());
            for (Chunk chunk : loadedChunks.values()) {
                if (!((ChunkAccessor)chunk).celeritas$getHasEntities()) {
                    continue;
                }
                ClassInheritanceMultiMap<Entity>[] entityMaps = chunk.getEntityLists();
                for (ClassInheritanceMultiMap<Entity> map : entityMaps) {
                    map.forEach(addEntity);
                }
            }
        } else {
            // Best we can do is the loaded entity list - this will miss some multipart entities
            this.world.loadedEntityList.forEach(addEntity);
        }
        return passesArray;
    }

    private List<Entity>[] celeritas$collectedEntities;

    @Unique
    private VintageIrisRenderingPipeline celeritas$getVintageIrisPipeline() {
        WorldRenderingPipeline pipeline = IrisCommon.getPipelineManager().getPipelineNullable();
        return pipeline instanceof VintageIrisRenderingPipeline vintagePipeline ? vintagePipeline : null;
    }

    @Unique
    private int celeritas$getEntityShaderId(Entity entity) {
        Object2IntFunction<NamespacedId> entityIds = WorldRenderingSettings.INSTANCE.getEntityIds();
        ResourceLocation location = EntityList.getKey(entity);
        if (location == null) {
            return this.celeritas$getUnknownEntityShaderId(entityIds);
        }

        NamespacedId id = new NamespacedId(location.getNamespace(), location.getPath());
        int shaderId = this.celeritas$getMappedEntityShaderId(entityIds, id);
        if (shaderId >= 0) {
            return shaderId;
        }

        NamespacedId modernAlias = this.celeritas$getModernEntityAlias(id);
        if (modernAlias != null) {
            shaderId = this.celeritas$getMappedEntityShaderId(entityIds, modernAlias);
            if (shaderId >= 0) {
                return shaderId;
            }
        }

        return this.celeritas$getUnknownEntityShaderId(entityIds);
    }

    @Unique
    private int celeritas$getMappedEntityShaderId(Object2IntFunction<NamespacedId> entityIds, NamespacedId id) {
        return entityIds != null && entityIds.containsKey(id) ? entityIds.getInt(id) : -1;
    }

    @Unique
    private int celeritas$getUnknownEntityShaderId(Object2IntFunction<NamespacedId> entityIds) {
        return this.celeritas$usesModernEntityMaterialRange(entityIds) ? celeritas$MODERN_ENTITY_MATERIAL_RANGE_END : 0;
    }

    @Unique
    private boolean celeritas$usesModernEntityMaterialRange(Object2IntFunction<NamespacedId> entityIds) {
        int chickenId = this.celeritas$getMappedEntityShaderId(entityIds, celeritas$CHICKEN_ENTITY_ID);
        return chickenId >= celeritas$MODERN_ENTITY_MATERIAL_RANGE_START && chickenId < celeritas$MODERN_ENTITY_MATERIAL_RANGE_END;
    }

    @Unique
    private NamespacedId celeritas$getModernEntityAlias(NamespacedId id) {
        if (!"minecraft".equals(id.getNamespace())) {
            return null;
        }

        return switch (id.getName()) {
            case "ender_crystal" -> new NamespacedId("minecraft", "end_crystal");
            case "villager_golem" -> new NamespacedId("minecraft", "iron_golem");
            case "xp_orb" -> new NamespacedId("minecraft", "experience_orb");
            case "commandblock_minecart" -> new NamespacedId("minecraft", "command_block_minecart");
            case "zombie_pigman" -> new NamespacedId("minecraft", "zombified_piglin");
            case "snowman" -> new NamespacedId("minecraft", "snow_golem");
            case "evocation_illager" -> new NamespacedId("minecraft", "evoker");
            case "vindication_illager" -> new NamespacedId("minecraft", "vindicator");
            case "illusion_illager" -> new NamespacedId("minecraft", "illusioner");
            case "eye_of_ender_signal" -> new NamespacedId("minecraft", "eye_of_ender");
            case "fireworks_rocket" -> new NamespacedId("minecraft", "firework_rocket");
            default -> null;
        };
    }

    @Unique
    private boolean celeritas$isEnderDragon(Entity entity) {
        ResourceLocation location = EntityList.getKey(entity);
        return location != null && celeritas$ENDER_DRAGON_ENTITY_ID.equals(new NamespacedId(location.getNamespace(), location.getPath()));
    }

    @Unique
    private void celeritas$prepareVanillaEntityRenderState(boolean unbindProgram, boolean attenuateSkyLight, float partialTicks) {
        if (unbindProgram) {
            GL20.glUseProgram(0);
        }

        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableBlend();
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
        GlStateManager.disableColorMaterial();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        this.mc.entityRenderer.enableLightmap();
        this.celeritas$prepareVanillaEntityTextureMatrices(attenuateSkyLight, partialTicks);
        this.celeritas$resetVanillaEntityVertexInputs();

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Unique
    private void celeritas$resetVanillaEntityVertexInputs() {
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        for (int attribute = 0; attribute < 16; attribute++) {
            GL20.glDisableVertexAttribArray(attribute);
        }

        GL30.glVertexAttribI3i(celeritas$IRIS_ENTITY_ATTRIBUTE_INDEX, 0, 0, 0);
    }

    @Unique
    private void celeritas$setIrisEntityAttribute(int shaderEntityId) {
        GL30.glVertexAttribI3i(celeritas$IRIS_ENTITY_ATTRIBUTE_INDEX, shaderEntityId, 0, 0);
    }

    @Unique
    private float celeritas$getDaylightFactor(float partialTicks) {
        if (this.world == null || this.world.provider == null || !this.world.provider.hasSkyLight()) {
            return 1.0F;
        }

        float skyAngle = this.world.getCelestialAngle(partialTicks);
        float daylight = 1.0F - (MathHelper.cos(skyAngle * ((float) Math.PI * 2.0F)) * 2.0F + 0.2F);
        daylight = 1.0F - MathHelper.clamp(daylight, 0.0F, 1.0F);
        return MathHelper.clamp(daylight, 0.0F, 1.0F);
    }

    @Unique
    private void celeritas$prepareVanillaEntityTextureMatrices(boolean attenuateSkyLight, float partialTicks) {
        int previousMatrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        float skyScale = attenuateSkyLight ? this.celeritas$getDaylightFactor(partialTicks) : 1.0F;

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();

        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.03125F, 0.03125F, 0.03125F);
        GL11.glScalef(0.00390625F, 0.00390625F * skyScale, 0.00390625F);

        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GL11.glMatrixMode(previousMatrixMode);
    }

    @Unique
    private void celeritas$prepareEntityLightmapCoordinates(Entity entity, boolean attenuateSkyLight, boolean clampLowerBodySkyLight, float partialTicks) {
        int packedLight = entity.getBrightnessForRender();
        float blockLight = packedLight & 0xFFFF;
        float skyLight = packedLight >> 16;
        if (this.celeritas$isNoSkylightDimension()) {
            skyLight = Math.max(skyLight, celeritas$NO_SKYLIGHT_ENTITY_LIGHT_FLOOR);
        } else if (clampLowerBodySkyLight) {
            int lowerBodyPackedLight = this.celeritas$getEntityLowerBodyPackedLight(entity, packedLight);
            blockLight = Math.max(blockLight, lowerBodyPackedLight & 0xFFFF);
            skyLight = Math.min(skyLight, lowerBodyPackedLight >> 16);
        }
        if (attenuateSkyLight) {
            skyLight *= this.celeritas$getDaylightFactor(partialTicks);
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, blockLight, skyLight);
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @Unique
    private int celeritas$getEntityLowerBodyPackedLight(Entity entity, int fallbackPackedLight) {
        if (this.world == null) {
            return fallbackPackedLight;
        }

        BlockPos lowerBodyPos = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY + 0.1D, entity.posZ);
        if (lowerBodyPos.getY() < 0 || lowerBodyPos.getY() >= 256 || !this.world.isBlockLoaded(lowerBodyPos)) {
            return fallbackPackedLight;
        }

        return this.world.getCombinedLight(lowerBodyPos, 0);
    }

    @Unique
    private boolean celeritas$isNoSkylightDimension() {
        return this.world != null && this.world.provider != null && !this.world.provider.hasSkyLight();
    }

    /**
     * @author embeddedt
     * @reason reimplement entity render loop because vanilla's relies on the renderInfos list
     */
    @Inject(method = "renderEntities", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderInfos:Ljava/util/List;", ordinal = 0))
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci,
                                @Local(ordinal = 1) List<Entity> outlineEntityList,
                                @Local(ordinal = 2) List<Entity> multipassEntityList,
                                @Local(ordinal = 0) double renderViewX,
                                @Local(ordinal = 1) double renderViewY,
                                @Local(ordinal = 2) double renderViewZ) {
        int pass = net.minecraftforge.client.MinecraftForgeClient.getRenderPass();
        if (pass == 0 || celeritas$collectedEntities == null) {
            celeritas$collectedEntities = getLoadedEntityList();
        }
        EntityPlayerSP player = this.mc.player;
        BlockPos.MutableBlockPos entityBlockPos = new BlockPos.MutableBlockPos();
        // Apply entity distance scaling
        Entity.setRenderDistanceWeight(MathHelper.clamp((double)this.mc.gameSettings.renderDistanceChunks / 8.0D, 1.0D, 2.5D) * 1);

        VintageIrisRenderingPipeline irisEntityPipeline = this.celeritas$getVintageIrisPipeline();
        boolean irisEntityRendering = irisEntityPipeline != null && irisEntityPipeline.beginVintageEntityRendering();
        boolean irisEntityFallbackRendering = !irisEntityRendering && irisEntityPipeline != null && irisEntityPipeline.beginVintageEntityFallbackRendering();
        boolean attenuateEntitySkyLight = irisEntityRendering && !irisEntityPipeline.isVintageEntityCompatibilityRenderingActive();
        boolean clampEntityLowerBodySkyLight = irisEntityRendering && irisEntityPipeline.shouldClampVintageEntityLowerBodySkyLight();
        this.celeritas$prepareVanillaEntityRenderState(!irisEntityRendering, attenuateEntitySkyLight, partialTicks);

        try {
            for(Entity entity : celeritas$collectedEntities[pass]) {
                // Do regular vanilla checks for visibility
                if(!this.renderManager.shouldRender(entity, camera, renderViewX, renderViewY, renderViewZ) && !entity.isRidingOrBeingRiddenBy(player)) {
                    continue;
                }

                // Check if any corners of the bounding box are in a visible subchunk
                if(!this.celeritas$isEnderDragon(entity) && !CeleritasWorldRenderer.instance().isEntityVisible(entity)) {
                    continue;
                }

                boolean isSleeping = renderViewEntity instanceof EntityLivingBase && ((EntityLivingBase) renderViewEntity).isPlayerSleeping();

                if ((entity != renderViewEntity || this.mc.gameSettings.thirdPersonView != 0 || isSleeping)
                        && (entity.posY < 0.0D || entity.posY >= 256.0D || this.world.isBlockLoaded(entityBlockPos.setPos(entity))))
                {
                    ++this.countEntitiesRendered;

                    int shaderEntityId = 0;
                    if (irisEntityRendering) {
                        shaderEntityId = this.celeritas$getEntityShaderId(entity);
                        CapturedRenderingState.INSTANCE.setCurrentEntity(shaderEntityId);
                        CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
                        irisEntityPipeline.updateVintageEntityUniforms(entity);
                    }

                    this.celeritas$prepareVanillaEntityRenderState(!irisEntityRendering, attenuateEntitySkyLight, partialTicks);
                    if (irisEntityRendering) {
                        this.celeritas$setIrisEntityAttribute(shaderEntityId);
                    }
                    this.celeritas$prepareEntityLightmapCoordinates(entity, attenuateEntitySkyLight, clampEntityLowerBodySkyLight, partialTicks);
                    this.renderManager.renderEntityStatic(entity, partialTicks, false);

                    if (this.isOutlineActive(entity, renderViewEntity, camera))
                    {
                        outlineEntityList.add(entity);
                    }

                    if (this.renderManager.isRenderMultipass(entity)) {
                        multipassEntityList.add(entity);
                    }
                }
            }
        } finally {
            if (irisEntityRendering) {
                CapturedRenderingState.INSTANCE.setCurrentEntity(0);
                CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
                irisEntityPipeline.updateVintageEntityUniforms(null);
                irisEntityPipeline.endVintageEntityRendering();
            } else if (irisEntityFallbackRendering) {
                irisEntityPipeline.endVintageEntityFallbackRendering();
            }
        }
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderMultipass(Lnet/minecraft/entity/Entity;F)V"))
    private void celeritas$renderMultipassEntityWithShaderBridge(RenderManager renderManager, Entity entity, float partialTicks) {
        VintageIrisRenderingPipeline irisEntityPipeline = this.celeritas$getVintageIrisPipeline();
        boolean irisEntityRendering = irisEntityPipeline != null && irisEntityPipeline.beginVintageEntityRendering();
        boolean irisEntityFallbackRendering = !irisEntityRendering && irisEntityPipeline != null && irisEntityPipeline.beginVintageEntityFallbackRendering();
        boolean attenuateEntitySkyLight = irisEntityRendering && !irisEntityPipeline.isVintageEntityCompatibilityRenderingActive();
        boolean clampEntityLowerBodySkyLight = irisEntityRendering && irisEntityPipeline.shouldClampVintageEntityLowerBodySkyLight();

        try {
            if (irisEntityRendering) {
                int shaderEntityId = this.celeritas$getEntityShaderId(entity);
                CapturedRenderingState.INSTANCE.setCurrentEntity(shaderEntityId);
                CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
                irisEntityPipeline.updateVintageEntityUniforms(entity);
                this.celeritas$prepareVanillaEntityRenderState(!irisEntityRendering, attenuateEntitySkyLight, partialTicks);
                this.celeritas$setIrisEntityAttribute(shaderEntityId);
            } else {
                this.celeritas$prepareVanillaEntityRenderState(!irisEntityRendering, attenuateEntitySkyLight, partialTicks);
            }

            this.celeritas$prepareEntityLightmapCoordinates(entity, attenuateEntitySkyLight, clampEntityLowerBodySkyLight, partialTicks);
            renderManager.renderMultipass(entity, partialTicks);
        } finally {
            if (irisEntityRendering) {
                CapturedRenderingState.INSTANCE.setCurrentEntity(0);
                CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
                irisEntityPipeline.updateVintageEntityUniforms(null);
                irisEntityPipeline.endVintageEntityRendering();
            } else if (irisEntityFallbackRendering) {
                irisEntityPipeline.endVintageEntityFallbackRendering();
            }
        }
    }
}

