package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.*;

public class AttributeContainerContext extends AbstractChainParameterContext implements PlayerContext {
    private final Entity entity;

    public AttributeContainerContext(Entity entity, ContextHolder contexts) {
        super(contexts);
        this.entity = entity;
    }

    @Override
    public Player player() {
        return this.entity instanceof Player player ? player : null;
    }
}
