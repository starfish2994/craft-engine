package net.momirealms.craftengine.proxy.minecraft.network.chat.contents;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = {"net.minecraft.network.chat.contents.PlainTextContents", "net.minecraft.network.chat.contents.LiteralContents"})
public interface PlainTextContentsProxy {
    PlainTextContentsProxy INSTANCE = ASMProxyFactory.create(PlainTextContentsProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.chat.contents.PlainTextContents");

    @MethodInvoker(name = "text")
    String getText(Object target);
}
