package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.entity.player.Player;

public abstract class BlockEntityRenderer {

    public abstract void spawn();

    public abstract void despawn();

    public abstract void spawn(Player player);

    public abstract void despawn(Player player);
}
