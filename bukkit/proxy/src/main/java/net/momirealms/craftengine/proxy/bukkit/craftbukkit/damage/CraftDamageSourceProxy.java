package net.momirealms.craftengine.proxy.bukkit.craftbukkit.damage;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "org.bukkit.craftbukkit.damage.CraftDamageSource")
public interface CraftDamageSourceProxy {
    CraftDamageSourceProxy INSTANCE = ASMProxyFactory.create(CraftDamageSourceProxy.class);

    @MethodInvoker(name = "getHandle")
    Object getHandle(Object target);

}
