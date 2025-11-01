package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.Obsolete
public class LegacyIngredient<I> {
    private final Item<I>[] items;

    public LegacyIngredient(Item<I>[] items) {
        this.items = items;
    }

    public Item<I>[] items() {
        return items;
    }

    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeArray(this.items, writer);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <I> LegacyIngredient<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        Item[] items = buf.readArray((FriendlyByteBuf.Reader) reader, Item.class);
        return new LegacyIngredient(items);
    }

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        for (int i = 0; i < this.items.length; i++) {
            Item<I> item = this.items[i];
            this.items[i] = function.apply(item);
        }
    }
}
