package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.BiFunction;
import java.util.function.Function;

@ApiStatus.Obsolete
public interface LegacyRecipe<I> {

    default void applyClientboundData(Function<Item<I>, Item<I>> function) {}

    void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer);

    record Type<I>(BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader<I>, LegacyRecipe<I>> reader) {

        public LegacyRecipe<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<I> reader) {
            return this.reader.apply(buf, reader);
        }
    }
}
