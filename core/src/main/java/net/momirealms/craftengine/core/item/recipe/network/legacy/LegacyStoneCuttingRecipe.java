package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Obsolete
public class LegacyStoneCuttingRecipe<I> implements LegacyRecipe<I> {
    private Item<I> result;
    private final String group;
    private final LegacyIngredient<I> ingredient;

    public LegacyStoneCuttingRecipe(LegacyIngredient<I> ingredient,
                                    Item<I> result,
                                    String group) {
        this.ingredient = ingredient;
        this.result = result;
        this.group = group;
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.result = function.apply(this.result);
        this.ingredient.applyClientboundData(function);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <I> LegacyStoneCuttingRecipe<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        String group = buf.readUtf();
        LegacyIngredient<I> ingredient = LegacyIngredient.read(buf, reader);
        Item<I> result = reader.apply(buf);
        return new LegacyStoneCuttingRecipe(ingredient, result, group);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeUtf(this.group);
        this.ingredient.write(buf, writer);
        writer.accept(buf, this.result);
    }
}
