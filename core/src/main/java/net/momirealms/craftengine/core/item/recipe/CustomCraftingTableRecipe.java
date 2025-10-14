package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.item.recipe.result.CustomRecipeResult;
import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

public abstract class CustomCraftingTableRecipe<T> extends AbstractGroupedRecipe<T>
        implements ConditionalRecipe<T>, VisualResultRecipe<T>, FunctionalRecipe<T> {
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
        return this.category;
    }

    @Override
    public RecipeType type() {
        return RecipeType.CRAFTING;
    }

    @Override
    public CustomRecipeResult<T> visualResult() {
        return this.visualResult;
    }

    @Override
    public Function<PlayerOptionalContext>[] functions() {
        return this.craftingFunctions;
    }
}
