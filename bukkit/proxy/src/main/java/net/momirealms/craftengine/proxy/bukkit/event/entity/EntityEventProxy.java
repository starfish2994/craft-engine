package net.momirealms.craftengine.proxy.bukkit.event.entity;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityEvent;

@ReflectionProxy(clazz = EntityEvent.class)
public interface EntityEventProxy {
    EntityEventProxy INSTANCE = ASMProxyFactory.create(EntityEventProxy.class);

    @FieldGetter(name = "entity")
    Entity getEntity(EntityEvent event);
}
