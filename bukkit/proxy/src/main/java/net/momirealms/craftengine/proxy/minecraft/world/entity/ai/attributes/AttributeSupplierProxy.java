package net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes;

import net.momirealms.craftengine.proxy.minecraft.core.HolderProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.entity.ai.attributes.AttributeSupplier")
public interface AttributeSupplierProxy {
    AttributeSupplierProxy INSTANCE = ASMProxyFactory.create(AttributeSupplierProxy.class);

    @MethodInvoker(name = "hasAttribute", activeIf = "min_version=1.20.5")
    boolean hasAttribute(Object target, @Type(clazz = HolderProxy.class) Object holder);

    @MethodInvoker(name = "hasAttribute", activeIf = "max_version=1.20.4")
    boolean hasAttribute$legacy(Object target, @Type(clazz = AttributeProxy.class) Object attribute);

    @MethodInvoker(name = "getBaseValue", activeIf = "min_version=1.20.5")
    double getBaseValue(Object target, @Type(clazz = HolderProxy.class) Object holder);

    @MethodInvoker(name = "getBaseValue", activeIf = "max_version=1.20.4")
    double getBaseValue$legacy(Object target, @Type(clazz = AttributeProxy.class) Object attribute);
}
