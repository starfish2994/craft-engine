package net.momirealms.craftengine.core.item.recipe.remainder;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;

public interface CraftRemainder {

    <T> Item<T> remainder(Key recipeId, Item<T> item);

}
