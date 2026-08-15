package net.momirealms.craftengine.core.item.customdata;

import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NumericTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ItemRandomValuesDataSerializer implements CustomDataSerializer<ItemRandomValuesData> {
    public static final ItemRandomValuesDataSerializer INSTANCE = new ItemRandomValuesDataSerializer();

    private ItemRandomValuesDataSerializer() {}

    @Override
    public Tag serialize(ItemRandomValuesData data) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Double> entry : data.values().entrySet()) {
            tag.putDouble(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    @Override
    public ItemRandomValuesData deserialize(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            Map<String, Double> values = new LinkedHashMap<>();
            for (Map.Entry<String, Tag> entry : compound.entrySet()) {
                if (entry.getValue() instanceof NumericTag numeric) {
                    values.put(entry.getKey(), numeric.getAsDouble());
                }
            }
            return new ItemRandomValuesData(values);
        }
        return null;
    }
}
