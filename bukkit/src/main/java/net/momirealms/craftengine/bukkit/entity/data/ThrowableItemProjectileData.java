package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;

public class ThrowableItemProjectileData<T> extends BaseEntityData<T> {
    public static final ThrowableItemProjectileData<Object> ItemStack = new ThrowableItemProjectileData<>(ThrowableItemProjectileData.class, EntityDataSerializersProxy.ITEM_STACK, ItemStackProxy.EMPTY);

    public ThrowableItemProjectileData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
