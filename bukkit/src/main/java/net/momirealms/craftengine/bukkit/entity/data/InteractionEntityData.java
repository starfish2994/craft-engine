package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;

public class InteractionEntityData<T> extends BaseEntityData<T> {
    public static final InteractionEntityData<Float> Width = new InteractionEntityData<>(InteractionEntityData.class, EntityDataSerializersProxy.FLOAT, 1F);
    public static final InteractionEntityData<Float> Height = new InteractionEntityData<>(InteractionEntityData.class, EntityDataSerializersProxy.FLOAT, 1F);
    public static final InteractionEntityData<Boolean> Responsive = new InteractionEntityData<>(InteractionEntityData.class, EntityDataSerializersProxy.BOOLEAN, false);

    public InteractionEntityData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
