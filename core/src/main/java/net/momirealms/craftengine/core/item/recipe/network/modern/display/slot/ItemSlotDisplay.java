package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class ItemSlotDisplay<I> implements SlotDisplay<I> {
    private final int item;

    public ItemSlotDisplay(int item) {
        this.item = item;
    }

    public static <I> ItemSlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        int item = buf.readVarInt();
        return new ItemSlotDisplay<>(item);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(2);
        buf.writeVarInt(this.item);
    }

    public int item() {
        return item;
    }

    @Override
    public String toString() {
        return "ItemSlotDisplay{" +
                "item=" + item +
                '}';
    }
}
