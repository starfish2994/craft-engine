package net.momirealms.craftengine.core.util;

import net.momirealms.sparrow.nbt.Tag;

public interface CustomDataSerializer<T> {

    Tag serialize(T data);

    T deserialize(Tag tag);
}
