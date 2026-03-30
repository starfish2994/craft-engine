package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.Player;
import org.bukkit.inventory.InventoryHolder;

public interface FurnitureInventoryHolder extends InventoryHolder {

    Furniture furniture();

    void onOpen(Player player);

    void onClose(Player player);
}
