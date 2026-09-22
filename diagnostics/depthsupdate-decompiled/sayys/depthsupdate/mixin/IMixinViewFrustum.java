package sayys.depthsupdate.mixin;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ViewFrustum.class)
public interface IMixinViewFrustum {
   @Invoker("func_178161_a")
   RenderChunk invokeGetRenderChunk(BlockPos var1);
}
