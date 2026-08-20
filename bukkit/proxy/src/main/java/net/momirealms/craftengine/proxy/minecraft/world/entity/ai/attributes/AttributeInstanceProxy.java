package net.momirealms.craftengine.proxy.minecraft.world.entity.ai.attributes;

import net.momirealms.craftengine.proxy.minecraft.core.HolderProxy;
import net.momirealms.craftengine.proxy.minecraft.resources.IdentifierProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.*;

import java.util.UUID;
import java.util.function.Consumer;

@ReflectionProxy(name = "net.minecraft.world.entity.ai.attributes.AttributeInstance")
public interface AttributeInstanceProxy {
    AttributeInstanceProxy INSTANCE = ASMProxyFactory.create(AttributeInstanceProxy.class);

    @ConstructorInvoker(activeIf = "min_version=1.20.5")
    Object newInstance$0(@Type(clazz = HolderProxy.class) Object type, Consumer<Object> updateCallback);

    @ConstructorInvoker(activeIf = "max_version=1.20.4")
    Object newInstance$1(@Type(clazz = AttributeProxy.class) Object type, Consumer<Object> updateCallback);

    @MethodInvoker(name = "setBaseValue")
    void setBaseValue(Object target, double baseValue);

    @MethodInvoker(name = "getValue")
    double getValue(Object target);

    @FieldGetter(name = "cachedValue")
    double getCachedValue(Object target);

    @FieldGetter(name = "baseValue")
    double getBaseValue(Object target);

    @MethodInvoker(name = "addOrUpdateTransientModifier", activeIf = "min_version=1.21")
    void addOrUpdateTransientModifier(Object target, @Type(clazz = AttributeModifierProxy.class) Object modifier);

    @MethodInvoker(name = "addTransientModifier")
    void addTransientModifier(Object target, @Type(clazz = AttributeModifierProxy.class) Object modifier);

    @MethodInvoker(name = "removeModifier", activeIf = "min_version=1.21")
    boolean removeModifier(Object target, @Type(clazz = IdentifierProxy.class) Object id);

    @MethodInvoker(name = "removeModifier", activeIf = "max_version=1.20.6")
    void removeModifier$legacy(Object target, UUID id);
}
