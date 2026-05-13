package net.momirealms.craftengine.proxy.spottedleaf.moonrise.common.util;

import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.AABBProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = {"ca.spottedleaf.moonrise.common.util.TickThread", "io.papermc.paper.util.TickThread"})
public interface TickThreadProxy {
    TickThreadProxy INSTANCE = ASMProxyFactory.create(TickThreadProxy.class);

    @MethodInvoker(name = "isTickThreadFor", isStatic = true)
    boolean isTickThreadFor(@Type(clazz = LevelProxy.class) Object level, @Type(clazz = AABBProxy.class) Object aabb);
}
