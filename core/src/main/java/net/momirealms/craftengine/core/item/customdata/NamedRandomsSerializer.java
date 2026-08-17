package net.momirealms.craftengine.core.item.customdata;

import net.momirealms.craftengine.core.plugin.context.NamedRandoms;
import net.momirealms.craftengine.core.util.CustomDataSerializer;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.NumericTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NamedRandomsSerializer implements CustomDataSerializer<NamedRandoms> {
    public static final NamedRandomsSerializer INSTANCE = new NamedRandomsSerializer();

    private NamedRandomsSerializer() {}

    @Override
    public Tag serialize(NamedRandoms data) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Double> entry : data.values.entrySet()) {
            tag.putDouble(entry.getKey(), entry.getValue());
        }
        return tag;
    }

    @Override
    public NamedRandoms deserialize(Tag tag) {
        if (tag instanceof CompoundTag compound) {
            Map<String, Double> values = new LinkedHashMap<>();
            for (Map.Entry<String, Tag> entry : compound.entrySet()) {
                if (entry.getValue() instanceof NumericTag numeric) {
                    values.put(entry.getKey(), numeric.getAsDouble());
                }
            }
            return new NamedRandoms(values);
        }
        return null;
    }
}
