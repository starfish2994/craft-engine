package net.momirealms.craftengine.proxy.minecraft.util.context;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = {"net.minecraft.util.context.ContextKeySet"})
public interface ContextKeySetProxy {
    ContextKeySetProxy INSTANCE = ASMProxyFactory.create(ContextKeySetProxy.class);

    @ReflectionProxy(name = {"net.minecraft.util.context.ContextKeySet$Builder"})
    interface BuilderProxy {
        BuilderProxy INSTANCE = ASMProxyFactory.create(BuilderProxy.class);

        @MethodInvoker(name = "optional")
        Object optional(Object target, @Type(clazz = ContextKeyProxy.class) Object key);
    }
}
