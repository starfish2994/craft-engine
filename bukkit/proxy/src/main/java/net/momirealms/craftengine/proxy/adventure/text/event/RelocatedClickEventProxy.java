package net.momirealms.craftengine.proxy.adventure.text.event;

import net.kyori.adventure.text.event.ClickEvent;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(clazz = ClickEvent.class)
public interface RelocatedClickEventProxy {
    RelocatedClickEventProxy INSTANCE = ASMProxyFactory.create(RelocatedClickEventProxy.class);

    @ConstructorInvoker
    <T extends ClickEvent.Payload> ClickEvent<T> newInstance(ClickEvent.Action<T> action, T payload);
}
