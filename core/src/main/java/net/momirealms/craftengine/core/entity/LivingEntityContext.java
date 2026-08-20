package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.AbstractChainParameterContext;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;

public final class LivingEntityContext extends AbstractChainParameterContext implements PlayerContext {
    public final LivingEntity entity;

    LivingEntityContext(LivingEntity entity, ContextHolder contexts) {
        super(contexts);
        this.entity = entity;
    }

    @Override
    public Player player() {
        return this.entity instanceof Player player ? player : null;
    }

    public LivingEntity entity() {
        return entity;
    }
}
