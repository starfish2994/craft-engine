package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CraftingRecipeCategory;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ApiStatus.Obsolete
public class LegacyShapelessRecipe<I> implements LegacyRecipe<I> {
    private final List<LegacyIngredient<I>> ingredients;
    private Item<I> result;
    private final String group;
    private final CraftingRecipeCategory category;

    public LegacyShapelessRecipe(List<LegacyIngredient<I>> ingredients,
                                 Item<I> result,
                                 String group,
                                 CraftingRecipeCategory category) {
        this.category = category;
        this.ingredients = ingredients;
        this.result = result;
        this.group = group;
    }

    public static <I> LegacyShapelessRecipe<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        String group = buf.readUtf();
        CraftingRecipeCategory category = CraftingRecipeCategory.byId(buf.readVarInt());
        List<LegacyIngredient<I>> ingredient = buf.readCollection(ArrayList::new, (byteBuffer) -> LegacyIngredient.read(byteBuffer, reader));
        Item<I> result = reader.apply(buf);
        return new LegacyShapelessRecipe<>(ingredient, result, group, category);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeUtf(this.group);
        buf.writeVarInt(this.category.ordinal());
        buf.writeCollection(this.ingredients, (byteBuf, legacyIngredient) -> legacyIngredient.write(buf, writer));
        writer.accept(buf, this.result);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.result = function.apply(this.result);
        for (LegacyIngredient<I> ingredient : this.ingredients) {
            ingredient.applyClientboundData(function);
        }
    }
}
