package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;

public interface ConditionalRecipe {

    boolean canUse(final PlayerOptionalContext context);
}
