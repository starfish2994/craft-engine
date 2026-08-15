package net.momirealms.craftengine.core.item.customdata;

import net.momirealms.craftengine.core.item.Item;

import java.util.Map;

public final class ItemRandomValueStore {
    public static final String TAG_KEY = "craftengine:random_values";

    private ItemRandomValueStore() {}

    public static Map<String, Double> read(Item item) {
        ItemRandomValuesData data = item.getCustomData(ItemRandomValuesData.class, TAG_KEY);
        return data == null ? Map.of() : data.values();
    }

    public static void write(Item item, Map<String, Double> values) {
        item.setCustomData(new ItemRandomValuesData(Map.copyOf(values)), TAG_KEY);
    }
}
