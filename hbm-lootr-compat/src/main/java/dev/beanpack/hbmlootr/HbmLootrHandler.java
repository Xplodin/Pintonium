package dev.beanpack.hbmlootr;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

final class HbmLootrHandler {
    private static final Logger LOG = LogManager.getLogger("HBM Lootr Compatibility");

    private final CompatConfig config;
    private final ReflectionBridge bridge = new ReflectionBridge();
    private boolean suppressDrops;

    HbmLootrHandler(CompatConfig config) {
        this.config = config;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkPopulate(PopulateChunkEvent.Post event) {
        if (!config.enabled || event.getWorld().isRemote) {
            return;
        }
        Chunk chunk = event.getWorld().getChunkProvider().getLoadedChunk(event.getChunkX(), event.getChunkZ());
        if (chunk != null) {
            processChunk(chunk, config.convertFilledWorldgenCrates, "world generation");
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!config.enabled || event.getWorld().isRemote) {
            return;
        }
        processChunk(event.getChunk(), false, "chunk load");
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (!config.enabled || event.phase != TickEvent.Phase.END || !(event.world instanceof WorldServer)) {
            return;
        }
        WorldServer world = (WorldServer) event.world;
        if (world.getTotalWorldTime() % config.periodicScanTicks != 0) {
            return;
        }

        // HBM can place phased structures after normal chunk-population events. A low-frequency scan
        // catches those crates, but only if they still carry an unopened loot table.
        List<TileEntity> snapshot = new ArrayList<>(world.loadedTileEntityList);
        for (TileEntity tile : snapshot) {
            if (bridge.isHbmCrate(tile) && bridge.getLootTable(tile) != null) {
                convert(tile, false, "delayed structure scan");
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (suppressDrops && event.getEntity() instanceof EntityItem) {
            event.setCanceled(true);
        }
    }

    private void processChunk(Chunk chunk, boolean allowFilledInventory, String reason) {
        Collection<TileEntity> values = new ArrayList<>(chunk.getTileEntityMap().values());
        for (TileEntity tile : values) {
            if (bridge.isHbmCrate(tile)) {
                convert(tile, allowFilledInventory, reason);
            }
        }
    }

    private void convert(TileEntity original, boolean allowFilledInventory, String reason) {
        World world = original.getWorld();
        if (world == null || world.isRemote || !bridge.isDimensionAllowed(world.provider.getDimension())) {
            return;
        }
        if (config.skipLockedCrates && bridge.isLocked(original)) {
            return;
        }

        ResourceLocation lootTable = bridge.getLootTable(original);
        if (lootTable != null && bridge.isLootTableBlocked(lootTable)) {
            return;
        }

        NonNullList<ItemStack> inventoryTemplate = null;
        long seed = 0L;
        if (lootTable != null) {
            seed = bridge.getLootTableSeed(original);
        } else if (allowFilledInventory) {
            inventoryTemplate = bridge.copyInventory(original);
            if (inventoryTemplate == null) {
                return;
            }
        } else {
            return;
        }

        BlockPos pos = original.getPos().toImmutable();
        String detail = lootTable == null ? "filled inventory" : lootTable.toString();
        if (config.dryRun) {
            LOG.info("[dry-run] Eligible HBM crate at dim {} {} via {} ({})",
                    world.provider.getDimension(), pos, reason, detail);
            return;
        }

        IBlockState oldState = world.getBlockState(pos);
        NBTTagCompound originalTag = original.writeToNBT(new NBTTagCompound());
        IBlockState replacement = bridge.getLootrInventoryState(oldState);
        if (replacement == null) {
            LOG.error("Could not resolve Lootr inventory block; leaving HBM crate at dim {} {} untouched",
                    world.provider.getDimension(), pos);
            return;
        }

        TileEntity newTile;
        suppressDrops = true;
        try {
            world.setBlockState(pos, replacement, 2);
            world.removeTileEntity(pos);
            Chunk replacementChunk = world.getChunkProvider()
                    .getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
            if (replacementChunk == null) {
                throw new IllegalStateException("Replacement chunk unexpectedly unloaded");
            }
            newTile = replacementChunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
        } catch (RuntimeException ex) {
            LOG.error("Failed to replace HBM crate at dim " + world.provider.getDimension() + " " + pos, ex);
            return;
        } finally {
            suppressDrops = false;
        }

        if (newTile == null || !bridge.isLootrInventory(newTile)) {
            LOG.error("Replacement at dim {} {} was not a Lootr inventory: {}",
                    world.provider.getDimension(), pos, newTile);
            rollback(world, pos, oldState, originalTag);
            return;
        }

        try {
            if (lootTable != null) {
                bridge.setLootTable(newTile, lootTable, seed);
            } else {
                bridge.setCustomInventory(newTile, inventoryTemplate);
            }
            bridge.copyCustomName(original, newTile);
            newTile.markDirty();
        } catch (RuntimeException ex) {
            LOG.error("Failed to transfer HBM loot data at dim "
                    + world.provider.getDimension() + " " + pos + "; rolling back", ex);
            rollback(world, pos, oldState, originalTag);
            return;
        }
        LOG.info("Converted HBM structure crate at dim {} {} via {} ({})",
                world.provider.getDimension(), pos, reason, detail);
    }

    private void rollback(World world, BlockPos pos, IBlockState oldState, NBTTagCompound originalTag) {
        suppressDrops = true;
        try {
            world.setBlockState(pos, oldState, 2);
            world.removeTileEntity(pos);
            Chunk chunk = world.getChunkProvider().getLoadedChunk(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk == null) {
                throw new IllegalStateException("Rollback chunk unexpectedly unloaded");
            }
            TileEntity restored = chunk.getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
            if (restored == null) {
                throw new IllegalStateException("Rollback did not recreate the HBM tile entity");
            }
            restored.readFromNBT(originalTag);
            restored.markDirty();
            LOG.info("Restored original HBM crate at dim {} {}", world.provider.getDimension(), pos);
        } catch (RuntimeException rollbackFailure) {
            LOG.error("Could not restore original HBM crate at dim "
                    + world.provider.getDimension() + " " + pos, rollbackFailure);
        } finally {
            suppressDrops = false;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static IBlockState copyFacing(IBlockState source, IBlockState target) {
        if (source.getPropertyKeys().contains(BlockChest.FACING)
                && target.getPropertyKeys().contains(BlockChest.FACING)) {
            return target.withProperty((IProperty) BlockChest.FACING, source.getValue(BlockChest.FACING));
        }
        return target;
    }

    private static final class ReflectionBridge {
        private Class<?> hbmCrateClass;
        private Class<?> lootrInventoryClass;
        private Method hbmGetLootTable;
        private Method hbmIsLocked;
        private Field hbmInventory;
        private Method lootrSetLootTable;
        private Method lootrSetCustomInventory;
        private Method lootrDimensionBlocked;
        private Method lootrTableBlocked;
        private Block lootrInventoryBlock;
        private boolean initialized;
        private boolean initializationFailed;

        private boolean initialize() {
            if (initialized) {
                return !initializationFailed;
            }
            initialized = true;
            try {
                hbmCrateClass = Class.forName("com.hbm.tileentity.machine.storage.TileEntityCrateBase");
                lootrInventoryClass = Class.forName("noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity");
                hbmGetLootTable = findMethod(hbmCrateClass,
                        new String[]{"getLootTable", "func_184276_b"});
                hbmIsLocked = findMethod(hbmCrateClass, new String[]{"isLocked"});
                hbmInventory = hbmCrateClass.getField("inventory");
                lootrSetLootTable = findMethod(lootrInventoryClass,
                        new String[]{"setLootTable", "func_189404_a"}, ResourceLocation.class, long.class);
                lootrSetCustomInventory = findMethod(lootrInventoryClass,
                        new String[]{"setCustomInventory"}, NonNullList.class);

                Class<?> modBlocks = Class.forName("noobanidus.mods.lootr.init.ModBlocks");
                lootrInventoryBlock = (Block) modBlocks.getField("INVENTORY").get(null);
                Class<?> configManager = Class.forName("noobanidus.mods.lootr.config.ConfigManager");
                lootrDimensionBlocked = findMethod(configManager,
                        new String[]{"isDimensionBlocked"}, int.class);
                lootrTableBlocked = findMethod(configManager,
                        new String[]{"isBlacklisted"}, ResourceLocation.class);
                LOG.info("HBM/Lootr reflection bridge initialized");
            } catch (ReflectiveOperationException | LinkageError ex) {
                initializationFailed = true;
                LOG.error("Could not initialize HBM/Lootr compatibility bridge", ex);
            }
            return !initializationFailed;
        }

        boolean isHbmCrate(TileEntity tile) {
            return initialize() && hbmCrateClass.isInstance(tile);
        }

        boolean isLootrInventory(TileEntity tile) {
            return initialize() && lootrInventoryClass.isInstance(tile);
        }

        ResourceLocation getLootTable(TileEntity tile) {
            if (!initialize()) {
                return null;
            }
            try {
                return (ResourceLocation) hbmGetLootTable.invoke(tile);
            } catch (ReflectiveOperationException ex) {
                LOG.error("Failed to read HBM loot table at " + tile.getPos(), ex);
                return null;
            }
        }

        long getLootTableSeed(TileEntity tile) {
            return tile.writeToNBT(new net.minecraft.nbt.NBTTagCompound()).getLong("LootTableSeed");
        }

        boolean isLocked(TileEntity tile) {
            if (!initialize()) {
                return true;
            }
            try {
                return (Boolean) hbmIsLocked.invoke(tile);
            } catch (ReflectiveOperationException ex) {
                LOG.error("Failed to inspect HBM crate lock at " + tile.getPos(), ex);
                return true;
            }
        }

        boolean isDimensionAllowed(int dimension) {
            if (!initialize()) {
                return false;
            }
            try {
                return !(Boolean) lootrDimensionBlocked.invoke(null, dimension);
            } catch (ReflectiveOperationException ex) {
                LOG.error("Failed to inspect Lootr dimension rules", ex);
                return false;
            }
        }

        boolean isLootTableBlocked(ResourceLocation table) {
            if (!initialize()) {
                return true;
            }
            try {
                return (Boolean) lootrTableBlocked.invoke(null, table);
            } catch (ReflectiveOperationException ex) {
                LOG.error("Failed to inspect Lootr table blacklist for " + table, ex);
                return true;
            }
        }

        NonNullList<ItemStack> copyInventory(TileEntity tile) {
            if (!initialize()) {
                return null;
            }
            try {
                Object value = hbmInventory.get(tile);
                if (!(value instanceof IItemHandler)) {
                    return null;
                }
                IItemHandler handler = (IItemHandler) value;
                NonNullList<ItemStack> result = NonNullList.create();
                boolean hasItems = false;
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    if (!stack.isEmpty()) {
                        hasItems = true;
                    }
                    if (!stack.isEmpty() || handler.getSlots() <= 27) {
                        result.add(stack.copy());
                    }
                }
                return hasItems ? result : null;
            } catch (IllegalAccessException ex) {
                LOG.error("Failed to snapshot HBM crate inventory at " + tile.getPos(), ex);
                return null;
            }
        }

        IBlockState getLootrInventoryState(IBlockState original) {
            if (!initialize()) {
                return null;
            }
            return copyFacing(original, lootrInventoryBlock.getDefaultState());
        }

        void setLootTable(TileEntity tile, ResourceLocation table, long seed) {
            try {
                lootrSetLootTable.invoke(tile, table, seed);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to transfer loot table " + table, ex);
            }
        }

        void setCustomInventory(TileEntity tile, NonNullList<ItemStack> inventory) {
            try {
                lootrSetCustomInventory.invoke(tile, inventory);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException("Failed to transfer custom inventory", ex);
            }
        }

        void copyCustomName(TileEntity source, TileEntity target) {
            if (!(target instanceof TileEntityChest)) {
                return;
            }
            try {
                Method hasName = findMethod(source.getClass(),
                        new String[]{"hasCustomName", "func_145818_k_"});
                if (!(Boolean) hasName.invoke(source)) {
                    return;
                }
                Method getName = findMethod(source.getClass(),
                        new String[]{"getName", "func_70005_c_"});
                ((TileEntityChest) target).setCustomName((String) getName.invoke(source));
            } catch (ReflectiveOperationException ignored) {
                // Names are cosmetic; conversion remains valid if an unusual crate omits these methods.
            }
        }

        private static Method findMethod(Class<?> owner, String[] names, Class<?>... parameters)
                throws NoSuchMethodException {
            Class<?> current = owner;
            while (current != null) {
                for (String name : names) {
                    try {
                        Method result = current.getDeclaredMethod(name, parameters);
                        result.setAccessible(true);
                        return result;
                    } catch (NoSuchMethodException ignored) {
                        // Try the next runtime/development name or superclass.
                    }
                }
                current = current.getSuperclass();
            }
            throw new NoSuchMethodException(owner.getName());
        }
    }
}
