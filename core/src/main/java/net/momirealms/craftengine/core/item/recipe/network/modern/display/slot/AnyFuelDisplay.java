package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class AnyFuelDisplay<I> implements SlotDisplay<I> {
    public static final AnyFuelDisplay<?> INSTANCE = new AnyFuelDisplay<>();

    @SuppressWarnings("unchecked")
    public static <I> AnyFuelDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        return (AnyFuelDisplay<I>) INSTANCE;
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(1);
    }

    @Override
    public String toString() {
        return "AnyFuelDisplay{}";
    }
}
