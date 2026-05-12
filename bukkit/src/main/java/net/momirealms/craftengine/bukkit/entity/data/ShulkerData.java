package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.core.DirectionProxy;
import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;

public class ShulkerData<T> extends MobData<T> {
    public static final ShulkerData<Object> AttachFace = new ShulkerData<>(ShulkerData.class, EntityDataSerializersProxy.DIRECTION, DirectionProxy.DOWN);
    public static final ShulkerData<Byte> Peek = new ShulkerData<>(ShulkerData.class, EntityDataSerializersProxy.BYTE, (byte) 0);
    public static final ShulkerData<Byte> Color = new ShulkerData<>(ShulkerData.class, EntityDataSerializersProxy.BYTE, (byte) 16);

    public ShulkerData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}