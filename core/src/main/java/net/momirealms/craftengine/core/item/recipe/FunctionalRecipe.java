package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.function.Function;

public interface FunctionalRecipe<T> extends Recipe<T> {


    Function<PlayerOptionalContext>[] functions();
}
