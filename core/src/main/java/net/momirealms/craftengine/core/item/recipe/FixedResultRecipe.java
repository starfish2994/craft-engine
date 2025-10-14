package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;

public interface FixedResultRecipe<T> extends Recipe<T> {

    CustomRecipeResult<T> result();

    default T result(ItemBuildContext context) {
        return this.result().buildItemStack(context);
    }
}
