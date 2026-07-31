package net.momirealms.craftengine.proxy.minecraft.network.chat;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.network.chat.Style")
public interface StyleProxy {
    StyleProxy INSTANCE = ASMProxyFactory.create(StyleProxy.class);

    @FieldGetter(name = "hoverEvent")
    Object getHoverEvent(Object target);
}
