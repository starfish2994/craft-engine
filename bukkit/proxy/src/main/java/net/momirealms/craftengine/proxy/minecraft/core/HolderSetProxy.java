package net.momirealms.craftengine.proxy.minecraft.core;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.core.HolderSet")
public interface HolderSetProxy {
    HolderSetProxy INSTANCE = ASMProxyFactory.create(HolderSetProxy.class);
}
