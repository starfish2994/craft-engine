package net.momirealms.craftengine.core.customdata;

import net.momirealms.craftengine.core.util.CustomDataSerializer;
import net.momirealms.craftengine.core.util.Key;

public record CustomDataKey<T>(Key id, CustomDataSerializer<T> serializer) {
}
