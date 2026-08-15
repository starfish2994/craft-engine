package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerContext;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;

public record ItemBoundNumberProvider(NumberProvider base, Item item) implements NumberProvider {

    @Override
    public double getDouble(Context context) {
        return this.base.getDouble(bind(context, this.item));
    }

    @Override
    public float getFloat(Context context) {
        return this.base.getFloat(bind(context, this.item));
    }

    @Override
    public boolean isConstant() {
        return this.base.isConstant();
    }

    public static Context bind(Context base, Item item) {
        Player player = base instanceof PlayerContext playerContext ? playerContext.player() : null;
        ContextHolder holder = ContextHolder.mutable(base.contexts().params()).withParameter(DirectContextParameters.ITEM, item);
        return PlayerOptionalContext.of(player, holder);
    }
}
