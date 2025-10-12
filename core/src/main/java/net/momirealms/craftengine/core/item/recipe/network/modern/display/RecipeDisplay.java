package net.momirealms.craftengine.core.item.recipe.network.modern.display;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface RecipeDisplay<I> {

    void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer);

    void applyClientboundData(Function<Item<I>, Item<I>> function);

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <I> RecipeDisplay<I> read(final FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        return buf.readById(BuiltInRegistries.RECIPE_DISPLAY_TYPE).read(buf, (FriendlyByteBuf.Reader) reader);
    }

    record Type<I>(BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader<Item<I>>, RecipeDisplay<I>> reader) {

        public RecipeDisplay<I> read(final FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
            return this.reader.apply(buf, reader);
        }
    }
}
