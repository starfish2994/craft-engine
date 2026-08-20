package net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes;

import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypeProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.entity.ai.attributes.DefaultAttributes")
public interface DefaultAttributesProxy {
    DefaultAttributesProxy INSTANCE = ASMProxyFactory.create(DefaultAttributesProxy.class);

    @MethodInvoker(name = "getSupplier", isStatic = true)
    Object getSupplier(@Type(clazz = EntityTypeProxy.class) Object entityType);
}
