package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.core.block.entity.BlockEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BlockEntityHolder implements InventoryHolder {
    private final BlockEntity blockEntity;
    private Inventory inventory;

    public BlockEntityHolder(BlockEntity entity) {
        this.blockEntity = entity;
    }

    public BlockEntity blockEntity() {
        return blockEntity;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
