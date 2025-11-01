package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;

public interface LegacyRecipeHolder<I> {

    void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer);

    LegacyRecipe<I> recipe();

    static <I> LegacyRecipeHolder<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
        if (VersionHelper.isOrAbove1_20_5()) {
            return ModernRecipeHolderImpl.read(buf, reader);
        } else {
            return LegacyRecipeHolderImpl.read(buf, reader);
        }
    }

    record LegacyRecipeHolderImpl<I>(Key id, Key type, LegacyRecipe<I> recipe) implements LegacyRecipeHolder<I> {

        @Override
        public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
            buf.writeKey(this.type);
            buf.writeKey(this.id);
            this.recipe.write(buf, writer);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public static <I> LegacyRecipeHolder<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
            Key type = buf.readKey();
            Key id = buf.readKey();
            return new LegacyRecipeHolderImpl(id, type, BuiltInRegistries.LEGACY_RECIPE_TYPE.getValue(type).read(buf, (FriendlyByteBuf.Reader) reader));
        }
    }

    record ModernRecipeHolderImpl<I>(Key id, int type, LegacyRecipe<I> recipe) implements LegacyRecipeHolder<I> {

        @Override
        public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
            buf.writeKey(this.id);
            buf.writeVarInt(this.type);
            this.recipe.write(buf, writer);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public static <I> LegacyRecipeHolder<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<Item<I>> reader) {
            Key id = buf.readKey();
            int type = buf.readVarInt();
            return new ModernRecipeHolderImpl(id, type, BuiltInRegistries.LEGACY_RECIPE_TYPE.getValue(type).read(buf, (FriendlyByteBuf.Reader) reader));
        }
    }
}
