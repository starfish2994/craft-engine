package net.momirealms.craftengine.core.item.recipe.network.modern.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CompositeSlotDisplay<I> implements SlotDisplay<I> {
    private final List<SlotDisplay<I>> slots;

    public CompositeSlotDisplay(List<SlotDisplay<I>> slots) {
        this.slots = slots;
    }

    public static <I> CompositeSlotDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        List<SlotDisplay<I>> slots = buf.readCollection(ArrayList::new, buffer -> SlotDisplay.read(buf, reader));
        return new CompositeSlotDisplay<>(slots);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        for (SlotDisplay<I> slotDisplay : this.slots) {
            slotDisplay.applyClientboundData(function);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(7);
        buf.writeCollection(this.slots, (byteBuf, slotDisplay) -> slotDisplay.write(buf, writer));
    }

    public List<SlotDisplay<I>> slots() {
        return this.slots;
    }

    @Override
    public String toString() {
        return "CompositeSlotDisplay{" +
                "slots=" + slots +
                '}';
    }
}
