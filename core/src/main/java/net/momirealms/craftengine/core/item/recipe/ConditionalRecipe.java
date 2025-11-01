package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.context.Context;

public interface ConditionalRecipe<T> extends Recipe<T> {

    boolean canUse(final Context context);

    boolean hasCondition();
}
