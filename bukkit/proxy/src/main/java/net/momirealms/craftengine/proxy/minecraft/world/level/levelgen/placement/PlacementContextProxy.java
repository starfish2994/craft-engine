package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.placement;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.placement.PlacementContext")
public interface PlacementContextProxy {
    PlacementContextProxy INSTANCE = ASMProxyFactory.create(PlacementContextProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.level.levelgen.placement.PlacementContext");

    @MethodInvoker(name = "getLevel")
    Object getLevel(Object target);
}
