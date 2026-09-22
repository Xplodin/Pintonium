package sayys.depthsupdate.registry;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import sayys.depthsupdate.DepthsUpdateConfig;
import sayys.depthsupdate.item.ItemSpyglass;

public class StandaloneRegistry {
   public static final Item spyglass = new ItemSpyglass();
   public static final SoundEvent spyglass_use = createSoundEvent("item.spyglass.use");
   public static final SoundEvent spyglass_stop = createSoundEvent("item.spyglass.stop");
   public static final RegistrationFeature SPYGLASS_FEATURE = new RegistrationFeature(() -> DepthsUpdateConfig.REGISTRY.enableSpyglass)
      .add(spyglass, spyglass_use, spyglass_stop)
      .withModelOverrides(
         event -> ModelBakery.registerItemVariants(
            spyglass, new ResourceLocation[]{spyglass.getRegistryName(), new ResourceLocation("depthsupdate", "item/spyglass_3d")}
         )
      )
      .withInit(
         () -> GameRegistry.addShapedRecipe(
            new ResourceLocation("depthsupdate", "spyglass"),
            null,
            new ItemStack(spyglass),
            new Object[]{" A ", " I ", " I ", 'A', AmethystRegistry.amethyst_shard, 'I', DeepslateRegistry.copper_ingot}
         )
      );

   private static SoundEvent createSoundEvent(String name) {
      ResourceLocation location = new ResourceLocation("depthsupdate", name);
      SoundEvent event = new SoundEvent(location);
      event.setRegistryName(location);
      return event;
   }
}
