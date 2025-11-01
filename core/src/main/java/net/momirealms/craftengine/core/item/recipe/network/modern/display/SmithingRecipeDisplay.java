package net.momirealms.craftengine.core.item.recipe.network.modern.display;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record SmithingRecipeDisplay<I>(SlotDisplay<I> template, SlotDisplay<I> base, SlotDisplay<I> addition, SlotDisplay<I> result, SlotDisplay<I> craftingStation) implements RecipeDisplay<I> {

    public static <I> SmithingRecipeDisplay<I> read(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<Item<I>> reader) {
        SlotDisplay<I> template = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> base = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> addition = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> result = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> craftingStation = SlotDisplay.read(buffer, reader);
        return new SmithingRecipeDisplay<>(template, base, addition, result, craftingStation);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(4);
        this.template.write(buf, writer);
        this.base.write(buf, writer);
        this.addition.write(buf, writer);
        this.result.write(buf, writer);
        this.craftingStation.write(buf, writer);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.template.applyClientboundData(function);
        this.base.applyClientboundData(function);
        this.addition.applyClientboundData(function);
        this.result.applyClientboundData(function);
        this.craftingStation.applyClientboundData(function);
    }

    @Override
    public @NotNull String toString() {
        return "SmithingRecipeDisplay{" +
                "addition=" + addition +
                ", template=" + template +
                ", base=" + base +
                ", result=" + result +
                ", craftingStation=" + craftingStation +
                '}';
    }
}
