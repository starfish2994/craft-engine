package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;

public interface ConditionalRecipe<T> extends Recipe<T> {

    boolean canUse(final PlayerOptionalContext context);

    boolean hasCondition();
}
