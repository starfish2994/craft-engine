package net.momirealms.craftengine.core.item.recipe.network.legacy;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.CraftingRecipeCategory;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Obsolete
public class LegacyCustomRecipe<I> implements LegacyRecipe<I> {
    private final CraftingRecipeCategory category;

    public LegacyCustomRecipe(CraftingRecipeCategory category) {
        this.category = category;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <I> LegacyCustomRecipe<I> read(FriendlyByteBuf buf, FriendlyByteBuf.Reader<I> reader) {
        CraftingRecipeCategory category = CraftingRecipeCategory.byId(buf.readVarInt());
        return new LegacyCustomRecipe(category);
    }

    @Override
    public void write(FriendlyByteBuf buf, FriendlyByteBuf.Writer<Item<I>> writer) {
        buf.writeVarInt(this.category.ordinal());
    }
}
