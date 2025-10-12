package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface SlotDisplay<I> {

    void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer);

    default void applyClientboundData(Function<Item<I>, Item<I>> function) {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <I> SlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        return buf.readById(BuiltInRegistries.SLOT_DISPLAY_TYPE).read(buf, (FriendlyByteBuf.Reader) reader);
    }

    record Type<I>(BiFunction<FriendlyByteBuf, FriendlyByteBuf.Reader<Item<I>>, SlotDisplay<I>> reader) {

        public SlotDisplay<I> read(final FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
            return this.reader.apply(buf, reader);
        }
    }
}
