package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;

public class AgeableMobData<T> extends PathfinderMobData<T> {
    public static final MobData<Boolean> Baby = new AgeableMobData<>(AgeableMobData.class, EntityDataSerializersProxy.BOOLEAN, false);

    public AgeableMobData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
