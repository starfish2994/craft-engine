package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;

public interface VisualResultRecipe<T> extends FixedResultRecipe<T> {

    CustomRecipeResult<T> visualResult();

    default boolean hasVisualResult() {
        return visualResult() != null;
    }

    default T assembleVisual(RecipeInput input, ItemBuildContext context) {
        CustomRecipeResult<T> result = visualResult();
        if (result != null) {
            return result.buildItemStack(context);
        }
        return null;
    }

    default Item<T> buildVisualOrActualResult(ItemBuildContext context) {
        CustomRecipeResult<T> visualResult = visualResult();
        if (visualResult != null) {
            return visualResult.buildItem(context);
        } else {
            return this.result().buildItem(context);
        }
    }
}
