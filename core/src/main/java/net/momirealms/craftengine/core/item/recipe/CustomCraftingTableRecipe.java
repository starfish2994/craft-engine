package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.recipe.input.RecipeInput;
import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public abstract class CustomCraftingTableRecipe<T> extends AbstractGroupedRecipe<T> implements ConditionalRecipe {
    protected final CraftingRecipeCategory category;
    private final CustomRecipeResult<T> visualResult;
    private final Function<PlayerOptionalContext>[] craftingFunctions;
    private final Condition<PlayerOptionalContext> craftingCondition;

    protected CustomCraftingTableRecipe(Key id,
                                        boolean showNotification,
                                        CustomRecipeResult<T> result,
                                        @Nullable CustomRecipeResult<T> visualResult,
                                        String group,
                                        CraftingRecipeCategory category,
                                        Function<PlayerOptionalContext>[] craftingFunctions,
                                        Condition<PlayerOptionalContext> craftingCondition) {
        super(id, showNotification, result, group);
        this.category = category == null ? CraftingRecipeCategory.MISC : category;
        this.visualResult = visualResult;
        this.craftingFunctions = craftingFunctions;
        this.craftingCondition = craftingCondition;
    }

    @Override
    public boolean canUse(PlayerOptionalContext context) {
        if (this.craftingCondition == null) return true;
        return this.craftingCondition.test(context);
    }

    public CraftingRecipeCategory category() {
        return category;
    }

    @Override
    public RecipeType type() {
        return RecipeType.CRAFTING;
    }

    public CustomRecipeResult<T> visualResult() {
        return visualResult;
    }

    public boolean hasVisualResult() {
        return visualResult != null;
    }

    public T assembleVisual(RecipeInput input, ItemBuildContext context) {
        if (this.visualResult != null) {
            return this.visualResult.buildItemStack(context);
        } else {
            throw new IllegalStateException("No visual result available");
        }
    }

    public Item<T> buildVisualOrActualResult(ItemBuildContext context) {
        if (this.visualResult != null) {
            return this.visualResult.buildItem(context);
        } else {
            return super.result.buildItem(context);
        }
    }

    public Function<PlayerOptionalContext>[] craftingFunctions() {
        return craftingFunctions;
    }
}
