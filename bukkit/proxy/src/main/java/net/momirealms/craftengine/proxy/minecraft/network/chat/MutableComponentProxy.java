package net.momirealms.craftengine.proxy.minecraft.network.chat;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

import java.util.List;

@ReflectionProxy(name = "net.minecraft.network.chat.MutableComponent")
public interface MutableComponentProxy {
    MutableComponentProxy INSTANCE = ASMProxyFactory.create(MutableComponentProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.chat.MutableComponent");

    @FieldGetter(name = "contents")
    Object getContents(Object target);

    @FieldGetter(name = "siblings")
    List<Object> getSiblings(Object target);

    @FieldGetter(name = "style")
    Object getStyle(Object target);
}
