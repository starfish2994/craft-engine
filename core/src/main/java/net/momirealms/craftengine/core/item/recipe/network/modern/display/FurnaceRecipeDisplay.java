package net.momirealms.craftengine.core.item.recipe.network.modern.display;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public record FurnaceRecipeDisplay<I>(SlotDisplay<I> ingredient, SlotDisplay<I> fuel, SlotDisplay<I> result, SlotDisplay<I> craftingStation, int duration, float experience)
        implements RecipeDisplay<I> {

    public static <I> FurnaceRecipeDisplay<I> read(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<Item<I>> reader) {
        SlotDisplay<I> ingredient = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> fuel = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> result = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> craftingStation = SlotDisplay.read(buffer, reader);
        int duration = buffer.readVarInt();
        float experience = buffer.readFloat();
        return new FurnaceRecipeDisplay<>(ingredient, fuel, result, craftingStation, duration, experience);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(2);
        this.ingredient.write(buf, writer);
        this.fuel.write(buf, writer);
        this.result.write(buf, writer);
        this.craftingStation.write(buf, writer);
        buf.writeVarInt(this.duration);
        buf.writeFloat(this.experience);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.ingredient.applyClientboundData(function);
        this.fuel.applyClientboundData(function);
        this.result.applyClientboundData(function);
        this.craftingStation.applyClientboundData(function);
    }

    @Override
    public @NotNull String toString() {
        return "FurnaceRecipeDisplay{" +
                "craftingStation=" + craftingStation +
                ", ingredient=" + ingredient +
                ", fuel=" + fuel +
                ", result=" + result +
                ", duration=" + duration +
                ", experience=" + experience +
                '}';
    }
}
