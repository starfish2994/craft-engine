package net.momirealms.craftengine.proxy.minecraft.world.level;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.level.StructureManager")
public interface StructureManagerProxy {
    StructureManagerProxy INSTANCE = ASMProxyFactory.create(StructureManagerProxy.class);
}
