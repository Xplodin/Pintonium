package com.bean.beanutils.compat.hbmlootr;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Post;
import net.minecraftforge.event.world.ChunkEvent.Load;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.items.IItemHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class HbmLootrHandler {
   private static final Logger LOG = LogManager.getLogger("HBM Lootr Compatibility");
   private final CompatConfig config;
   private final HbmLootrHandler.ReflectionBridge bridge = new HbmLootrHandler.ReflectionBridge();
   private boolean suppressDrops;

   public HbmLootrHandler(CompatConfig config) {
      this.config = config;
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onChunkPopulate(Post event) {
      if (this.config.enabled && !event.getWorld().field_72995_K) {
         Chunk chunk = event.getWorld().func_72863_F().func_186026_b(event.getChunkX(), event.getChunkZ());
         if (chunk != null) {
            this.processChunk(chunk, this.config.convertFilledWorldgenCrates, "world generation");
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onChunkLoad(Load event) {
      if (this.config.enabled && !event.getWorld().field_72995_K) {
         this.processChunk(event.getChunk(), false, "chunk load");
      }
   }

   @SubscribeEvent
   public void onWorldTick(WorldTickEvent event) {
      if (this.config.enabled && event.phase == Phase.END && event.world instanceof WorldServer) {
         WorldServer world = (WorldServer)event.world;
         if (world.func_82737_E() % this.config.periodicScanTicks == 0L) {
            for (TileEntity tile : new ArrayList(world.field_147482_g)) {
               if (this.bridge.isHbmCrate(tile) && this.bridge.getLootTable(tile) != null) {
                  this.convert(tile, false, "delayed structure scan");
               }
            }
         }
      }
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   public void onEntityJoin(EntityJoinWorldEvent event) {
      if (this.suppressDrops && event.getEntity() instanceof EntityItem) {
         event.setCanceled(true);
      }
   }

   private void processChunk(Chunk chunk, boolean allowFilledInventory, String reason) {
      for (TileEntity tile : new ArrayList(chunk.func_177434_r().values())) {
         if (this.bridge.isHbmCrate(tile)) {
            this.convert(tile, allowFilledInventory, reason);
         }
      }
   }

   private void convert(TileEntity original, boolean allowFilledInventory, String reason) {
      World world = original.func_145831_w();
      if (world != null && !world.field_72995_K && this.bridge.isDimensionAllowed(world.field_73011_w.getDimension())) {
         if (!this.config.skipLockedCrates || !this.bridge.isLocked(original)) {
            ResourceLocation lootTable = this.bridge.getLootTable(original);
            if (lootTable == null || !this.bridge.isLootTableBlocked(lootTable)) {
               NonNullList<ItemStack> inventoryTemplate = null;
               long seed = 0L;
               if (lootTable != null) {
                  seed = this.bridge.getLootTableSeed(original);
               } else {
                  if (!allowFilledInventory) {
                     return;
                  }

                  inventoryTemplate = this.bridge.copyInventory(original);
                  if (inventoryTemplate == null) {
                     return;
                  }
               }

               BlockPos pos = original.func_174877_v().func_185334_h();
               String detail = lootTable == null ? "filled inventory" : lootTable.toString();
               if (this.config.dryRun) {
                  LOG.info("[dry-run] Eligible HBM crate at dim {} {} via {} ({})", world.field_73011_w.getDimension(), pos, reason, detail);
               } else {
                  IBlockState oldState = world.func_180495_p(pos);
                  NBTTagCompound originalTag = original.func_189515_b(new NBTTagCompound());
                  IBlockState replacement = this.bridge.getLootrInventoryState(oldState);
                  if (replacement == null) {
                     LOG.error("Could not resolve Lootr inventory block; leaving HBM crate at dim {} {} untouched", world.field_73011_w.getDimension(), pos);
                  } else {
                     this.suppressDrops = true;

                     TileEntity newTile;
                     try {
                        world.func_180501_a(pos, replacement, 2);
                        world.func_175713_t(pos);
                        Chunk replacementChunk = world.func_72863_F().func_186026_b(pos.func_177958_n() >> 4, pos.func_177952_p() >> 4);
                        if (replacementChunk == null) {
                           throw new IllegalStateException("Replacement chunk unexpectedly unloaded");
                        }

                        newTile = replacementChunk.func_177424_a(pos, EnumCreateEntityType.IMMEDIATE);
                     } catch (RuntimeException var21) {
                        LOG.error("Failed to replace HBM crate at dim " + world.field_73011_w.getDimension() + " " + pos, var21);
                        return;
                     } finally {
                        this.suppressDrops = false;
                     }

                     if (newTile != null && this.bridge.isLootrInventory(newTile)) {
                        try {
                           if (lootTable != null) {
                              this.bridge.setLootTable(newTile, lootTable, seed);
                           } else {
                              this.bridge.setCustomInventory(newTile, inventoryTemplate);
                           }

                           this.bridge.copyCustomName(original, newTile);
                           newTile.func_70296_d();
                        } catch (RuntimeException var20) {
                           LOG.error("Failed to transfer HBM loot data at dim " + world.field_73011_w.getDimension() + " " + pos + "; rolling back", var20);
                           this.rollback(world, pos, oldState, originalTag);
                           return;
                        }

                        LOG.info("Converted HBM structure crate at dim {} {} via {} ({})", world.field_73011_w.getDimension(), pos, reason, detail);
                     } else {
                        LOG.error("Replacement at dim {} {} was not a Lootr inventory: {}", world.field_73011_w.getDimension(), pos, newTile);
                        this.rollback(world, pos, oldState, originalTag);
                     }
                  }
               }
            }
         }
      }
   }

   private void rollback(World world, BlockPos pos, IBlockState oldState, NBTTagCompound originalTag) {
      this.suppressDrops = true;

      try {
         world.func_180501_a(pos, oldState, 2);
         world.func_175713_t(pos);
         Chunk chunk = world.func_72863_F().func_186026_b(pos.func_177958_n() >> 4, pos.func_177952_p() >> 4);
         if (chunk == null) {
            throw new IllegalStateException("Rollback chunk unexpectedly unloaded");
         }

         TileEntity restored = chunk.func_177424_a(pos, EnumCreateEntityType.IMMEDIATE);
         if (restored == null) {
            throw new IllegalStateException("Rollback did not recreate the HBM tile entity");
         }

         restored.func_145839_a(originalTag);
         restored.func_70296_d();
         LOG.info("Restored original HBM crate at dim {} {}", world.field_73011_w.getDimension(), pos);
      } catch (RuntimeException var10) {
         LOG.error("Could not restore original HBM crate at dim " + world.field_73011_w.getDimension() + " " + pos, var10);
      } finally {
         this.suppressDrops = false;
      }
   }

   private static IBlockState copyFacing(IBlockState source, IBlockState target) {
      return source.func_177227_a().contains(BlockChest.field_176459_a) && target.func_177227_a().contains(BlockChest.field_176459_a)
         ? target.func_177226_a(BlockChest.field_176459_a, (EnumFacing)source.func_177229_b(BlockChest.field_176459_a))
         : target;
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

      private ReflectionBridge() {
      }

      private boolean initialize() {
         if (this.initialized) {
            return !this.initializationFailed;
         } else {
            this.initialized = true;

            try {
               this.hbmCrateClass = Class.forName("com.hbm.tileentity.machine.storage.TileEntityCrateBase");
               this.lootrInventoryClass = Class.forName("noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity");
               this.hbmGetLootTable = findMethod(this.hbmCrateClass, new String[]{"getLootTable", "func_184276_b"});
               this.hbmIsLocked = findMethod(this.hbmCrateClass, new String[]{"isLocked"});
               this.hbmInventory = this.hbmCrateClass.getField("inventory");
               this.lootrSetLootTable = findMethod(this.lootrInventoryClass, new String[]{"setLootTable", "func_189404_a"}, ResourceLocation.class, long.class);
               this.lootrSetCustomInventory = findMethod(this.lootrInventoryClass, new String[]{"setCustomInventory"}, NonNullList.class);
               Class<?> modBlocks = Class.forName("noobanidus.mods.lootr.init.ModBlocks");
               this.lootrInventoryBlock = (Block)modBlocks.getField("INVENTORY").get(null);
               Class<?> configManager = Class.forName("noobanidus.mods.lootr.config.ConfigManager");
               this.lootrDimensionBlocked = findMethod(configManager, new String[]{"isDimensionBlocked"}, int.class);
               this.lootrTableBlocked = findMethod(configManager, new String[]{"isBlacklisted"}, ResourceLocation.class);
               HbmLootrHandler.LOG.info("HBM/Lootr reflection bridge initialized");
            } catch (LinkageError | ReflectiveOperationException var3) {
               this.initializationFailed = true;
               HbmLootrHandler.LOG.error("Could not initialize HBM/Lootr compatibility bridge", var3);
            }

            return !this.initializationFailed;
         }
      }

      boolean isHbmCrate(TileEntity tile) {
         return this.initialize() && this.hbmCrateClass.isInstance(tile);
      }

      boolean isLootrInventory(TileEntity tile) {
         return this.initialize() && this.lootrInventoryClass.isInstance(tile);
      }

      ResourceLocation getLootTable(TileEntity tile) {
         if (!this.initialize()) {
            return null;
         } else {
            try {
               return (ResourceLocation)this.hbmGetLootTable.invoke(tile);
            } catch (ReflectiveOperationException var3) {
               HbmLootrHandler.LOG.error("Failed to read HBM loot table at " + tile.func_174877_v(), var3);
               return null;
            }
         }
      }

      long getLootTableSeed(TileEntity tile) {
         return tile.func_189515_b(new NBTTagCompound()).func_74763_f("LootTableSeed");
      }

      boolean isLocked(TileEntity tile) {
         if (!this.initialize()) {
            return true;
         } else {
            try {
               return (Boolean)this.hbmIsLocked.invoke(tile);
            } catch (ReflectiveOperationException var3) {
               HbmLootrHandler.LOG.error("Failed to inspect HBM crate lock at " + tile.func_174877_v(), var3);
               return true;
            }
         }
      }

      boolean isDimensionAllowed(int dimension) {
         if (!this.initialize()) {
            return false;
         } else {
            try {
               return !(Boolean)this.lootrDimensionBlocked.invoke(null, dimension);
            } catch (ReflectiveOperationException var3) {
               HbmLootrHandler.LOG.error("Failed to inspect Lootr dimension rules", var3);
               return false;
            }
         }
      }

      boolean isLootTableBlocked(ResourceLocation table) {
         if (!this.initialize()) {
            return true;
         } else {
            try {
               return (Boolean)this.lootrTableBlocked.invoke(null, table);
            } catch (ReflectiveOperationException var3) {
               HbmLootrHandler.LOG.error("Failed to inspect Lootr table blacklist for " + table, var3);
               return true;
            }
         }
      }

      NonNullList<ItemStack> copyInventory(TileEntity tile) {
         if (!this.initialize()) {
            return null;
         } else {
            try {
               Object value = this.hbmInventory.get(tile);
               if (!(value instanceof IItemHandler)) {
                  return null;
               } else {
                  IItemHandler handler = (IItemHandler)value;
                  NonNullList<ItemStack> result = NonNullList.func_191196_a();
                  boolean hasItems = false;

                  for (int slot = 0; slot < handler.getSlots(); slot++) {
                     ItemStack stack = handler.getStackInSlot(slot);
                     if (!stack.func_190926_b()) {
                        hasItems = true;
                     }

                     if (!stack.func_190926_b() || handler.getSlots() <= 27) {
                        result.add(stack.func_77946_l());
                     }
                  }

                  return hasItems ? result : null;
               }
            } catch (IllegalAccessException var8) {
               HbmLootrHandler.LOG.error("Failed to snapshot HBM crate inventory at " + tile.func_174877_v(), var8);
               return null;
            }
         }
      }

      IBlockState getLootrInventoryState(IBlockState original) {
         return !this.initialize() ? null : HbmLootrHandler.copyFacing(original, this.lootrInventoryBlock.func_176223_P());
      }

      void setLootTable(TileEntity tile, ResourceLocation table, long seed) {
         try {
            this.lootrSetLootTable.invoke(tile, table, seed);
         } catch (ReflectiveOperationException var6) {
            throw new IllegalStateException("Failed to transfer loot table " + table, var6);
         }
      }

      void setCustomInventory(TileEntity tile, NonNullList<ItemStack> inventory) {
         try {
            this.lootrSetCustomInventory.invoke(tile, inventory);
         } catch (ReflectiveOperationException var4) {
            throw new IllegalStateException("Failed to transfer custom inventory", var4);
         }
      }

      void copyCustomName(TileEntity source, TileEntity target) {
         if (target instanceof TileEntityChest) {
            try {
               Method hasName = findMethod(source.getClass(), new String[]{"hasCustomName", "func_145818_k_"});
               if (!(Boolean)hasName.invoke(source)) {
                  return;
               }

               Method getName = findMethod(source.getClass(), new String[]{"getName", "func_70005_c_"});
               ((TileEntityChest)target).func_190575_a((String)getName.invoke(source));
            } catch (ReflectiveOperationException var5) {
            }
         }
      }

      private static Method findMethod(Class<?> owner, String[] names, Class<?>... parameters) throws NoSuchMethodException {
         for (Class<?> current = owner; current != null; current = current.getSuperclass()) {
            for (String name : names) {
               try {
                  Method result = current.getDeclaredMethod(name, parameters);
                  result.setAccessible(true);
                  return result;
               } catch (NoSuchMethodException var9) {
               }
            }
         }

         throw new NoSuchMethodException(owner.getName());
      }
   }
}
