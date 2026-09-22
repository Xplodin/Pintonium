package com.bean.beanutils;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class keyPatch {
   private static final Logger LOGGER = LogManager.getLogger("beanutils");
   private static final ResourceLocation KEY_ID = new ResourceLocation("hbm", "key");
   private static final String LAST_DROP_TAG = "hbm_key_last_drop";
   private static final long COOLDOWN_TICKS = 36000L;
   private boolean warnedMissingKey;

   @SubscribeEvent
   public void onLivingDrops(LivingDropsEvent event) {
      DamageSource source = event.getSource();
      if (source.func_76346_g() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)source.func_76346_g();
         long now = player.field_70170_p.func_82737_E();
         NBTTagCompound data = player.getEntityData();
         NBTTagCompound persisted = data.func_74775_l("PlayerPersisted");
         if (!persisted.func_74764_b("hbm_key_last_drop") || now - persisted.func_74763_f("hbm_key_last_drop") >= 36000L) {
            Item keyItem = (Item)ForgeRegistries.ITEMS.getValue(KEY_ID);
            if (keyItem == null) {
               if (!this.warnedMissingKey) {
                  LOGGER.warn("Could not drop HBM key because item '{}' is not registered.", KEY_ID);
                  this.warnedMissingKey = true;
               }
            } else {
               ItemStack key = new ItemStack(keyItem);
               NBTTagCompound tag = new NBTTagCompound();
               NBTTagCompound gift = new NBTTagCompound();
               gift.func_74778_a("true", "");
               tag.func_74782_a("gift", gift);
               key.func_77982_d(tag);
               event.getDrops()
                  .add(
                     new EntityItem(
                        player.field_70170_p,
                        event.getEntityLiving().field_70165_t,
                        event.getEntityLiving().field_70163_u,
                        event.getEntityLiving().field_70161_v,
                        key
                     )
                  );
               persisted.func_74772_a("hbm_key_last_drop", now);
               data.func_74782_a("PlayerPersisted", persisted);
            }
         }
      }
   }
}
