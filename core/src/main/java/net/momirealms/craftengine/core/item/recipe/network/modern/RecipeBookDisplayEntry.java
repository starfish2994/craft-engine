package net.momirealms.craftengine.core.item.recipe.network.modern;

import com.mojang.datafixers.util.Either;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.display.RecipeDisplay;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

public record RecipeBookDisplayEntry<I>(RecipeDisplayId displayId, RecipeDisplay<I> display, OptionalInt group, int category, Optional<List<Ingredient>> ingredients) {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <I> RecipeBookDisplayEntry<I> read(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<Item<I>> reader) {
        RecipeDisplayId displayId = RecipeDisplayId.read(buffer);
        RecipeDisplay display = RecipeDisplay.read(buffer, reader);
        OptionalInt group = buffer.readOptionalVarInt();
        int category = buffer.readVarInt(); // simplify the registry lookup since we don't care about the category
        Optional<List<Ingredient>> requirements = buffer.readOptional(buf -> buf.readCollection(ArrayList::new, byteBuf -> new Ingredient(byteBuf.readHolderSet()))); // simplify the registry lookup since we don't care about the ingredient ids
        return new RecipeBookDisplayEntry(displayId, display, group, category, requirements);
    }

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.display.applyClientboundData(function);
    }

    public void write(FriendlyByteBuf buffer, FriendlyByteBuf.Writer<Item<I>> writer) {
        this.displayId.write(buffer);
        this.display.write(buffer, writer);
        buffer.writeOptionalVarInt(this.group);
        buffer.writeVarInt(this.category);
        buffer.writeOptional(this.ingredients, (buf, recipeIngredients) -> buf.writeCollection(recipeIngredients, (byteBuf, ingredient) -> byteBuf.writeHolderSet(ingredient.holderSet)));
    }

    @Override
    public @NotNull String toString() {
        return "RecipeBookDisplayEntry{" +
                "category=" + category +
                ", displayId=" + displayId +
                ", display=" + display +
                ", group=" + group +
                ", ingredients=" + ingredients +
                '}';
    }

    public record Ingredient(Either<List<Integer>, Key> holderSet) {
    }
}
