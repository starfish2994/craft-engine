package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;

import java.util.Optional;

public class EnderManData<T> extends MonsterData<T> {
    public static final EnderManData<Optional<Object>> CarryState = new EnderManData<>(EnderManData.class, EntityDataSerializersProxy.OPTIONAL_BLOCK_STATE, Optional.empty());
    public static final EnderManData<Boolean> Creepy = new EnderManData<>(EnderManData.class, EntityDataSerializersProxy.BOOLEAN, false);
    public static final EnderManData<Boolean> StaredAt = new EnderManData<>(EnderManData.class, EntityDataSerializersProxy.BOOLEAN, false);

    public EnderManData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
