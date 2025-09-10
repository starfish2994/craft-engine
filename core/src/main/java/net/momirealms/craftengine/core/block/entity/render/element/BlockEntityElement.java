package net.momirealms.craftengine.core.block.entity.render.element;

import net.momirealms.craftengine.core.entity.player.Player;

public interface BlockEntityElement {

    void spawn(Player player);

    void despawn(Player player);

    void update(Player player);
}
