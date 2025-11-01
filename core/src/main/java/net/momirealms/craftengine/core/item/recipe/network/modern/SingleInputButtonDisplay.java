package net.momirealms.craftengine.core.item.recipe.network.modern;

import com.mojang.datafixers.util.Either;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.function.Function;

public record SingleInputButtonDisplay<I>(Either<List<Integer>, Key> ingredients, SlotDisplay<I> display) {

    public static <I> SingleInputButtonDisplay<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        Either<List<Integer>, Key> ingredients = buf.readHolderSet();
        SlotDisplay<I> slotDisplay = SlotDisplay.read(buf, reader);
        return new SingleInputButtonDisplay<>(ingredients, slotDisplay);
    }

    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeHolderSet(this.ingredients);
        this.display.write(buf, writer);
    }

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.display.applyClientboundData(function);
    }
}
