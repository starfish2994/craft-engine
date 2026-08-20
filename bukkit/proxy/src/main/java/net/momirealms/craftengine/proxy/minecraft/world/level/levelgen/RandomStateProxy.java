package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.RandomState")
public interface RandomStateProxy {
    RandomStateProxy INSTANCE = ASMProxyFactory.create(RandomStateProxy.class);
}
