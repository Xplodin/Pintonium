package org.taumc.celeritas.mixin.compat.ironchest;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps Iron Chests' client-only lid state tied to the chest the local player actually opened. Iron Chests increments
 * its viewer count independently on both logical sides, and some modded GUI close paths leave the client value stale.
 * The server inventory remains untouched; this only supplies the correct input to the existing client lid animation.
 */
@Pseudo
@Mixin(targets = "cpw.mods.ironchest.common.tileentity.chest.TileEntityIronChest", remap = false)
public abstract class IronChestAnimationSyncMixin {
    @Unique
    private static final String CELERITAS_IRON_CHEST_GUI = "cpw.mods.ironchest.client.gui.chest.GUIChest";

    @Unique
    private static final String CELERITAS_IRON_CHEST_CONTAINER =
            "cpw.mods.ironchest.common.gui.chest.ContainerIronChest";

    @Unique
    private static BlockPos celeritas$openChestPos;

    @Unique
    private static int celeritas$openChestDimension;

    @Unique
    private static long celeritas$keepOpenUntilTick;

    @Shadow
    public int numPlayersUsing;

    @Inject(method = "func_174889_b", at = @At("TAIL"), remap = false)
    private void celeritas$rememberOpenedChest(EntityPlayer player, CallbackInfo ci) {
        TileEntity tileEntity = (TileEntity) (Object) this;
        World world = tileEntity.getWorld();

        if (world == null || !world.isRemote) {
            return;
        }

        celeritas$openChestPos = tileEntity.getPos().toImmutable();
        celeritas$openChestDimension = world.provider.getDimension();
        celeritas$keepOpenUntilTick = world.getTotalWorldTime() + 5L;
    }

    @Inject(method = "func_174886_c", at = @At("TAIL"), remap = false)
    private void celeritas$forgetClosedChest(EntityPlayer player, CallbackInfo ci) {
        TileEntity tileEntity = (TileEntity) (Object) this;
        World world = tileEntity.getWorld();

        if (world != null && world.isRemote && celeritas$isTrackedChest(tileEntity, world)) {
            celeritas$openChestPos = null;
        }
    }

    @Inject(method = "func_73660_a", at = @At("HEAD"), remap = false)
    private void celeritas$correctClientViewerCount(CallbackInfo ci) {
        TileEntity tileEntity = (TileEntity) (Object) this;
        World world = tileEntity.getWorld();

        if (world == null || !world.isRemote) {
            return;
        }

        Minecraft client = Minecraft.getMinecraft();
        if (client.getIntegratedServer() == null) {
            return;
        }

        boolean trackedChest = celeritas$isTrackedChest(tileEntity, world);
        boolean guiOpen = celeritas$isIronChestGuiOpen(client);
        boolean openingGracePeriod = world.getTotalWorldTime() <= celeritas$keepOpenUntilTick;
        boolean shouldBeOpen = trackedChest && (guiOpen || openingGracePeriod);

        this.numPlayersUsing = shouldBeOpen ? 1 : 0;

        if (trackedChest && !shouldBeOpen) {
            celeritas$openChestPos = null;
        }
    }

    @Unique
    private static boolean celeritas$isTrackedChest(TileEntity tileEntity, World world) {
        return celeritas$openChestPos != null
                && celeritas$openChestDimension == world.provider.getDimension()
                && celeritas$openChestPos.equals(tileEntity.getPos());
    }

    @Unique
    private static boolean celeritas$isIronChestGuiOpen(Minecraft client) {
        if (client.currentScreen != null
                && celeritas$isClassOrSubclass(client.currentScreen.getClass(), CELERITAS_IRON_CHEST_GUI)) {
            return true;
        }

        Container container = client.player != null ? client.player.openContainer : null;
        return container != null
                && celeritas$isClassOrSubclass(container.getClass(), CELERITAS_IRON_CHEST_CONTAINER);
    }

    @Unique
    private static boolean celeritas$isClassOrSubclass(Class<?> type, String expectedName) {
        while (type != null) {
            if (type.getName().equals(expectedName) || type.getName().startsWith(expectedName + "$")) {
                return true;
            }

            type = type.getSuperclass();
        }

        return false;
    }
}
