package net.momirealms.craftengine.core.item.recipe.network.modern;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record SingleInputDisplay<I>(List<Integer> ingredients, Optional<SlotDisplay<I>> display) {

    public static <I> SingleInputDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        List<Integer> ingredients = buf.readCollection(ArrayList::new, FriendlyByteBuf::readVarInt);
        Optional<SlotDisplay<I>> slotDisplay = buf.readOptional(b -> SlotDisplay.read(b, reader));
        return new SingleInputDisplay<>(ingredients, slotDisplay);
    }

    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeCollection(this.ingredients, FriendlyByteBuf::writeVarInt);
        buf.writeOptional(this.display, (b, d) -> d.write(buf, writer));
    }

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.display.ifPresent(i -> i.applyClientboundData(function));
    }
}
