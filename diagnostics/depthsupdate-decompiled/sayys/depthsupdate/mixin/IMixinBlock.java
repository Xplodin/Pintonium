package sayys.depthsupdate.mixin;

import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Block.class)
public interface IMixinBlock {
   @Accessor("field_149783_u")
   void depthsupdate$setUseNeighborBrightness(boolean var1);

   @Accessor("field_149785_s")
   boolean depthsupdate$isTranslucent();

   @Accessor("field_149786_r")
   int depthsupdate$getLightOpacity();
}
