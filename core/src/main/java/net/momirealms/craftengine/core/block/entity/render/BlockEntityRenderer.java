package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.entity.player.Player;

public abstract class BlockEntityRenderer {
    private final int entityId;

    public BlockEntityRenderer(int entityId) {
        this.entityId = entityId;
    }

    public int entityId() {
        return this.entityId;
    }

    public abstract void spawn();

    public abstract void despawn();

    public abstract void spawn(Player player);

    public abstract void despawn(Player player);
}
