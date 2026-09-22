package sayys.depthsupdate.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistrationFeature {
   private final BooleanSupplier configToggle;
   private final List<IForgeRegistryEntry<?>> entries = new ArrayList<>();
   private final Set<IForgeRegistryEntry<?>> skippedModels = new HashSet<>();
   private BiConsumer<Block, Register<Item>> itemBlockProvider = (block, event) -> event.getRegistry()
      .register((Item)new ItemBlock(block).setRegistryName(block.getRegistryName()));
   private Consumer<ModelRegistryEvent> modelOverrideCallback = event -> {};
   private Runnable initCallback = () -> {};

   public RegistrationFeature(BooleanSupplier configToggle) {
      this.configToggle = configToggle;
   }

   public RegistrationFeature add(IForgeRegistryEntry<?>... newEntries) {
      Collections.addAll(this.entries, newEntries);
      return this;
   }

   public RegistrationFeature withItemBlockProvider(BiConsumer<Block, Register<Item>> provider) {
      this.itemBlockProvider = provider;
      return this;
   }

   public RegistrationFeature skipDefaultModel(IForgeRegistryEntry<?>... entriesToSkip) {
      Collections.addAll(this.skippedModels, entriesToSkip);
      return this;
   }

   public RegistrationFeature withModelOverrides(Consumer<ModelRegistryEvent> callback) {
      this.modelOverrideCallback = callback;
      return this;
   }

   public RegistrationFeature withInit(Runnable callback) {
      this.initCallback = callback;
      return this;
   }

   public boolean isEnabled() {
      return this.configToggle.getAsBoolean();
   }

   public void registerBlocks(Register<Block> event) {
      if (this.isEnabled()) {
         for (IForgeRegistryEntry<?> entry : this.entries) {
            if (entry instanceof Block) {
               event.getRegistry().register(entry);
            }
         }
      }
   }

   public void registerItems(Register<Item> event) {
      if (this.isEnabled()) {
         for (IForgeRegistryEntry<?> entry : this.entries) {
            if (entry instanceof Item) {
               event.getRegistry().register(entry);
            } else if (entry instanceof Block) {
               this.itemBlockProvider.accept((Block)entry, event);
            }
         }
      }
   }

   public void registerSounds(Register<SoundEvent> event) {
      if (this.isEnabled()) {
         for (IForgeRegistryEntry<?> entry : this.entries) {
            if (entry instanceof SoundEvent) {
               event.getRegistry().register(entry);
            }
         }
      }
   }

   public void registerModels(ModelRegistryEvent event) {
      if (this.isEnabled()) {
         for (IForgeRegistryEntry<?> entry : this.entries) {
            if (!this.skippedModels.contains(entry)) {
               if (entry instanceof IHasModel) {
                  Item item = entry instanceof Item ? (Item)entry : Item.func_150898_a((Block)entry);
                  if (item != Items.field_190931_a) {
                     ((IHasModel)entry).registerModel(item);
                  }
               } else if (entry instanceof Block block) {
                  Item item = Item.func_150898_a(block);
                  if (item != Items.field_190931_a) {
                     this.registerDefaultBlockModel(block);
                  }
               } else if (entry instanceof Item item && item.getRegistryName() != null) {
                  this.registerDefaultItemModel(item);
               }
            }
         }

         this.modelOverrideCallback.accept(event);
      }
   }

   public void init() {
      if (this.isEnabled()) {
         this.initCallback.run();
      }
   }

   private void registerDefaultBlockModel(Block block) {
      if (block.getRegistryName() != null) {
         ModelLoader.setCustomModelResourceLocation(Item.func_150898_a(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
      }
   }

   private void registerDefaultItemModel(Item item) {
      if (item.getRegistryName() != null) {
         ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
      }
   }
}
