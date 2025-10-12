package net.momirealms.craftengine.core.item.recipe.network.modern.display;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record StonecutterRecipeDisplay<I>(SlotDisplay<I> input, SlotDisplay<I> result, SlotDisplay<I> craftingStation) implements RecipeDisplay<I> {

    public static <I> StonecutterRecipeDisplay<I> read(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<Item<I>> reader) {
        SlotDisplay<I> input = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> result = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> craftingStation = SlotDisplay.read(buffer, reader);
        return new StonecutterRecipeDisplay<>(input, result, craftingStation);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(3);
        this.input.write(buf, writer);
        this.result.write(buf, writer);
        this.craftingStation.write(buf, writer);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.input.applyClientboundData(function);
        this.result.applyClientboundData(function);
        this.craftingStation.applyClientboundData(function);
    }

    @Override
    public @NotNull String toString() {
        return "StonecutterRecipeDisplay{" +
                "craftingStation=" + craftingStation +
                ", input=" + input +
                ", result=" + result +
                '}';
    }
}
