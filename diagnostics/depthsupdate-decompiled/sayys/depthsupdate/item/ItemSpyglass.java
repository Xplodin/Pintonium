package sayys.depthsupdate.item;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sayys.depthsupdate.registry.StandaloneRegistry;

public class ItemSpyglass extends Item {
   public ItemSpyglass() {
      this.setRegistryName("depthsupdate", "spyglass");
      this.func_77655_b("spyglass");
      this.func_77625_d(1);
      this.func_77637_a(CreativeTabs.field_78040_i);
      this.func_185043_a(new ResourceLocation("depthsupdate", "in_hand"), new IItemPropertyGetter() {
         {
            Objects.requireNonNull(ItemSpyglass.this);
         }

         @SideOnly(Side.CLIENT)
         public float func_185085_a(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
            return entityIn != null && worldIn != null ? 1.0F : 0.0F;
         }
      });
   }

   public int func_77626_a(ItemStack stack) {
      return 72000;
   }

   public EnumAction func_77661_b(ItemStack stack) {
      return EnumAction.NONE;
   }

   public ActionResult<ItemStack> func_77659_a(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
      ItemStack itemstack = playerIn.func_184586_b(handIn);
      playerIn.func_184598_c(handIn);
      if (!worldIn.field_72995_K) {
         worldIn.func_184148_a(
            null, playerIn.field_70165_t, playerIn.field_70163_u, playerIn.field_70161_v, StandaloneRegistry.spyglass_use, SoundCategory.PLAYERS, 1.0F, 1.0F
         );
      }

      return new ActionResult(EnumActionResult.SUCCESS, itemstack);
   }

   public void func_77615_a(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
      if (!worldIn.field_72995_K) {
         worldIn.func_184148_a(
            null,
            entityLiving.field_70165_t,
            entityLiving.field_70163_u,
            entityLiving.field_70161_v,
            StandaloneRegistry.spyglass_stop,
            SoundCategory.PLAYERS,
            1.0F,
            1.0F
         );
      }
   }
}
