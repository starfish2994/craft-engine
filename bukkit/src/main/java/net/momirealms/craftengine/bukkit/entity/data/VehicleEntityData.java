package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;

public class VehicleEntityData<T> extends BaseEntityData<T> {
    public static final VehicleEntityData<Integer> Hurt = new VehicleEntityData<>(VehicleEntityData.class, EntityDataSerializersProxy.INT, 0);
    public static final VehicleEntityData<Integer> HurtDir = new VehicleEntityData<>(VehicleEntityData.class, EntityDataSerializersProxy.INT, 1);
    public static final VehicleEntityData<Float> Damage = new VehicleEntityData<>(VehicleEntityData.class, EntityDataSerializersProxy.FLOAT, 0.0F);

    public VehicleEntityData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
