package net.momirealms.craftengine.core.item.recipe.network.modern.display;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.slot.SlotDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public record ShapelessCraftingRecipeDisplay<I>(List<SlotDisplay<I>> ingredients, SlotDisplay<I> result, SlotDisplay<I> craftingStation) implements RecipeDisplay<I> {

    public static <I> ShapelessCraftingRecipeDisplay<I> read(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<Item<I>> reader) {
        List<SlotDisplay<I>> ingredients = buffer.readCollection(ArrayList::new, buf -> SlotDisplay.read(buf, reader));
        SlotDisplay<I> result = SlotDisplay.read(buffer, reader);
        SlotDisplay<I> craftingStation = SlotDisplay.read(buffer, reader);
        return new ShapelessCraftingRecipeDisplay<>(ingredients, result, craftingStation);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(0);
        buf.writeCollection(this.ingredients, (byteBuf, slotDisplay) -> slotDisplay.write(buf, writer));
        this.result.write(buf, writer);
        this.craftingStation.write(buf, writer);
    }

    @Override
    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        for (SlotDisplay<I> ingredient : ingredients) {
            ingredient.applyClientboundData(function);
        }
        this.result.applyClientboundData(function);
        this.craftingStation.applyClientboundData(function);
    }

    @Override
    public @NotNull String toString() {
        return "ShapelessCraftingRecipeDisplay{" +
                "craftingStation=" + craftingStation +
                ", ingredients=" + ingredients +
                ", result=" + result +
                '}';
    }
}
