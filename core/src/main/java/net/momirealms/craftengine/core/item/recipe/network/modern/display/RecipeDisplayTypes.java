package net.momirealms.craftengine.core.item.recipe.network.modern.display;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.function.BiFunction;

@SuppressWarnings({"unchecked", "rawtypes"})
public final class RecipeDisplayTypes {
    private RecipeDisplayTypes() {}

    public static final Key CRAFTING_SHAPELESS = Key.of("crafting_shapeless");
    public static final Key CRAFTING_SHAPED = Key.of("crafting_shaped");
    public static final Key FURNACE = Key.of("furnace");
    public static final Key STONECUTTER = Key.of("stonecutter");
    public static final Key SMITHING = Key.of("smithing");

    public static void init() {
    }

    static {
        register(CRAFTING_SHAPELESS, new RecipeDisplay.Type(createReaderFunction(ShapelessCraftingRecipeDisplay::read)));
        register(CRAFTING_SHAPED, new RecipeDisplay.Type(createReaderFunction(ShapedCraftingRecipeDisplay::read)));
        register(FURNACE, new RecipeDisplay.Type(createReaderFunction(FurnaceRecipeDisplay::read)));
        register(STONECUTTER, new RecipeDisplay.Type(createReaderFunction(StonecutterRecipeDisplay::read)));
        register(SMITHING, new RecipeDisplay.Type(createReaderFunction(SmithingRecipeDisplay::read)));
    }

    private static <I> BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader<Item<I>>, RecipeDisplay<I>> createReaderFunction(
            BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader, RecipeDisplay> function) {
        return (BiFunction) function;
    }
    
    public static <I> void register(Key key, RecipeDisplay.Type<I> type) {
        ((WritableRegistry<RecipeDisplay.Type<?>>) BuiltInRegistries.RECIPE_DISPLAY_TYPE)
                .register(ResourceKey.create(Registries.RECIPE_DISPLAY_TYPE.location(), key), type);
    }
}
