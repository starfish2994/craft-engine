package net.momirealms.craftengine.core.advancement.network;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.util.function.Function;

public record AdvancementHolder<I>(Key id, Advancement<I> advancement) {

    public static <I> AdvancementHolder<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        Key key = buf.readKey();
        Advancement<I> ad = Advancement.read(buf, reader);
        return new AdvancementHolder<>(key, ad);
    }

    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeKey(this.id);
        this.advancement.write(buf, writer);
    }

    public void applyClientboundData(Function<Item<I>, Item<I>> function) {
        this.advancement.applyClientboundData(function);
    }

    public void replaceNetworkTags(Function<Component, Component> function) {
        this.advancement.replaceNetworkTags(function);
    }
}
