package net.momirealms.craftengine.bukkit.plugin.gui;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.StorageContainer;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.gui.Inventory;

public class BukkitInventory implements Inventory {
    private final StorageContainer container;

    public BukkitInventory(StorageContainer container) {
        this.container = container;
    }

    @Override
    public void open(Player player, Component title) {
        BukkitServerPlayer serverPlayer = (BukkitServerPlayer) player;
        Object nmsPlayer = serverPlayer.serverPlayer();
        try {
            int nextId = FastNMS.INSTANCE.method$ServerPlayer$nextContainerCounter(nmsPlayer);
            Object nmsTitle = ComponentUtils.adventureToMinecraft(title);
            Object menu = FastNMS.INSTANCE.createSimpleContainerMenu(this.container, nextId, nmsPlayer, nmsTitle);
            FastNMS.INSTANCE.field$AbstractContainerMenu$checkReachable(menu, false);
            Object packet = FastNMS.INSTANCE.constructor$ClientboundOpenScreenPacket(nextId, this.container.menuType(), nmsTitle);
            serverPlayer.sendPacket(packet, false);
            FastNMS.INSTANCE.field$Player$containerMenu(nmsPlayer, menu);
            FastNMS.INSTANCE.method$ServerPlayer$initMenu(nmsPlayer, menu);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to create bukkit inventory", e);
        }
    }

    @Override
    public void setItem(int index, Item<?> item) {
        this.container.setItemStack(index, item == null ? CoreReflections.instance$ItemStack$EMPTY : item.getLiteralObject());
    }
}
