package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.customdata.ItemAttributeModifiersData;

import java.util.List;

public final class ItemAttributeModifierStore {
    public static final String TAG_KEY = "craftengine:attribute_modifiers";

    private ItemAttributeModifierStore() {}

    public static List<ItemAttributeModifier> read(Item item) {
        ItemAttributeModifiersData data = item.getCustomData(ItemAttributeModifiersData.class, TAG_KEY);
        return data == null ? List.of() : data.modifiers();
    }

    public static void write(Item item, List<ItemAttributeModifier> modifiers) {
        item.setCustomData(new ItemAttributeModifiersData(List.copyOf(modifiers)), TAG_KEY);
    }
}
