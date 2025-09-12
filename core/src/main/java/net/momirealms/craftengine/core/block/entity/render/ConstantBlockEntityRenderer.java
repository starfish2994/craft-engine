package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public abstract class ConstantBlockEntityRenderer {

    public abstract void spawn();

    public abstract void despawn();

    public abstract void spawn(Player player);

    public abstract void despawn(Player player);

    public abstract void deactivate();

    public abstract void activate();
}
