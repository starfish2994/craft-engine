package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CookingRecipeCategory;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Obsolete
public class LegacyCookingRecipe<I> implements LegacyRecipe<I> {
    private Item<I> result;
    private final CookingRecipeCategory category;
    private final String group;
    private final LegacyIngredient<I> ingredient;
    private final float experience;
    private final int cookingTime;

    public LegacyCookingRecipe(LegacyIngredient<I> ingredient,
                               CookingRecipeCategory category,
                               float experience,
                               int cookingTime,
                               Item<I> result,
                               String group) {
        this.ingredient = ingredient;
        this.category = category;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.result = result;
        this.group = group;
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.result = function.apply(this.result);
        this.ingredient.applyClientboundData(function);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <I> LegacyCookingRecipe<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        String group = buf.readUtf();
        CookingRecipeCategory category = CookingRecipeCategory.byId(buf.readVarInt());
        LegacyIngredient<I> ingredient = LegacyIngredient.read(buf, reader);
        Item<I> result = reader.apply(buf);
        float experience = buf.readFloat();
        int cookingTime = buf.readVarInt();
        return new LegacyCookingRecipe(ingredient, category, experience, cookingTime, result, group);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeUtf(this.group);
        buf.writeVarInt(this.category.ordinal());
        this.ingredient.write(buf, writer);
        writer.accept(buf, this.result);
        buf.writeFloat(this.experience);
        buf.writeVarInt(this.cookingTime);
    }
}
