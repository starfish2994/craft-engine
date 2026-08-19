package net.momirealms.craftengine.proxy.bukkit.craftbukkit.persistence;

import net.momirealms.craftengine.proxy.minecraft.nbt.TagProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.Map;

@ReflectionProxy(name = "org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer")
public interface CraftPersistentDataContainerProxy {
    CraftPersistentDataContainerProxy INSTANCE = ASMProxyFactory.create(CraftPersistentDataContainerProxy.class);

    @MethodInvoker(name = "put")
    void put(Object target, String key, @Type(clazz = TagProxy.class) Object tag);

    @MethodInvoker(name = "getRaw")
    Map<String, Object> getRaw(Object target);
}
