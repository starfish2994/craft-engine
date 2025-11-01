package net.momirealms.craftengine.core.item.recipe.network.modern;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.function.Function;

public record RecipeBookEntry<I>(RecipeBookDisplayEntry<I> entry, byte flags) {

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.entry.applyClientboundData(function);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <I> RecipeBookEntry<I> read(FriendlyByteBuf buffer, FriendlyByteBuf.Reader<Item<I>> reader) {
        RecipeBookDisplayEntry displayEntry = RecipeBookDisplayEntry.read(buffer, reader);
        byte flags = buffer.readByte();
        return new RecipeBookEntry(displayEntry, flags);
    }

    public void write(FriendlyByteBuf buffer, FriendlyByteBuf.Writer<Item<I>> writer) {
        this.entry.write(buffer, writer);
        buffer.writeByte(this.flags);
    }
}

