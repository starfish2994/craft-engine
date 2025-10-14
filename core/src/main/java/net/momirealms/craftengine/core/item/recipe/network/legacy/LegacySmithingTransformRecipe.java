package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Obsolete
public class LegacySmithingTransformRecipe<I> implements LegacyRecipe<I> {
    private final LegacyIngredient<I> template;
    private final LegacyIngredient<I> base;
    private final LegacyIngredient<I> addition;
    private Item<I> result;

    public LegacySmithingTransformRecipe(LegacyIngredient<I> addition, LegacyIngredient<I> template, LegacyIngredient<I> base, Item<I> result) {
        this.addition = addition;
        this.template = template;
        this.base = base;
        this.result = result;
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        this.template.write(buf, writer);
        this.base.write(buf, writer);
        this.addition.write(buf, writer);
        writer.accept(buf, this.result);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.result = function.apply(this.result);
        this.template.applyClientboundData(function);
        this.base.applyClientboundData(function);
        this.addition.applyClientboundData(function);
    }

    public static <I> LegacySmithingTransformRecipe<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        LegacyIngredient<I> template = LegacyIngredient.read(buf, reader);
        LegacyIngredient<I> base = LegacyIngredient.read(buf, reader);
        LegacyIngredient<I> addition = LegacyIngredient.read(buf, reader);
        Item<I> result = reader.apply(buf);
        return new LegacySmithingTransformRecipe<>(template, base, addition, result);
    }
}
