package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class EmptySlotDisplay<I> implements SlotDisplay<I> {
    public static final EmptySlotDisplay<?> INSTANCE = new EmptySlotDisplay<>();

    @SuppressWarnings("unchecked")
    public static <I> EmptySlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        return (EmptySlotDisplay<I>) INSTANCE;
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(0);
    }

    @Override
    public String toString() {
        return "EmptySlotDisplay{}";
    }
}
