package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.placement;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.placement.PlacementFilter")
public interface PlacementFilterProxy {
    PlacementFilterProxy INSTANCE = ASMProxyFactory.create(PlacementFilterProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.level.levelgen.placement.PlacementFilter");
}
