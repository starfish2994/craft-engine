package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.bukkit.nms.FastNMS;

import java.util.List;

public interface EntityData<T> {

    Object serializer();

    int id();

    T defaultValue();

    Object entityDataAccessor();

    default Object createEntityDataIfNotDefaultValue(T value) {
        if (defaultValue().equals(value)) return null;
        return EntityDataValue.create(id(), serializer(), entityDataAccessor(), value);
    }

    default void addEntityDataIfNotDefaultValue(T value, List<Object> list) {
        if (!defaultValue().equals(value)) {
            list.add(EntityDataValue.create(id(), serializer(), entityDataAccessor(), value));
        }
    }

    default void addEntityData(T value, List<Object> list) {
        list.add(EntityDataValue.create(id(), serializer(), entityDataAccessor(), value));
    }

    @SuppressWarnings("unchecked")
    default T get(Object entityData) {
        return (T) FastNMS.INSTANCE.method$SynchedEntityData$get(entityData, entityDataAccessor());
    }

    static <T> EntityData<T> of(Class<?> clazz, Object serializer, T defaultValue) {
        return new SimpleEntityData<>(clazz, serializer, defaultValue);
    }
}
