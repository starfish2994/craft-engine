package net.momirealms.craftengine.proxy.minecraft.network.chat.contents;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.network.chat.contents.TranslatableContents")
public interface TranslatableContentsProxy {
    TranslatableContentsProxy INSTANCE = ASMProxyFactory.create(TranslatableContentsProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.chat.contents.TranslatableContents");

    @FieldGetter(name = "args")
    Object[] getArgs(Object target);
}
