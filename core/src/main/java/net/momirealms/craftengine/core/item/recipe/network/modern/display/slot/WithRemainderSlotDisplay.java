package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class WithRemainderSlotDisplay<I> implements SlotDisplay<I> {
    private final SlotDisplay<I> input;
    private final SlotDisplay<I> remainder;

    public WithRemainderSlotDisplay(SlotDisplay<I> input, SlotDisplay<I> remainder) {
        this.input = input;
        this.remainder = remainder;
    }

    public static <I> WithRemainderSlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        SlotDisplay<I> input = SlotDisplay.read(buf, reader);
        SlotDisplay<I> remainder = SlotDisplay.read(buf, reader);
        return new WithRemainderSlotDisplay<>(input, remainder);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(6);
        this.input.write(buf, writer);
        this.remainder.write(buf, writer);
    }

    @Override
    public String toString() {
        return "WithRemainderSlotDisplay{" +
                "input=" + input +
                ", remainder=" + remainder +
                '}';
    }
}
