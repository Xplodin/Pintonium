package sayys.depthsupdate.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import sayys.depthsupdate.core.HeightManager;

@Mixin(Entity.class)
public abstract class MixinEntity {
   @Shadow
   public World field_70170_p;

   @ModifyConstant(constant = @Constant(doubleValue = -64.0), method = "Lnet/minecraft/entity/Entity;func_70030_z()V")
   private double depthsupdate$modifyVoidDamageLevel(double original) {
      return HeightManager.getVoidDamageLevel(this.field_70170_p);
   }
}
