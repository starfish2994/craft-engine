package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;

public class MobData<T> extends LivingEntityData<T> {
    public static final MobData<Byte> MobFlags = new MobData<>(MobData.class, EntityDataSerializersProxy.BYTE, (byte) 0);

    public MobData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}