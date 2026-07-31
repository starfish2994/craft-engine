package net.momirealms.craftengine.proxy.minecraft.network.chat.contents;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.network.chat.contents.PlainTextContents")
public interface PlainTextContentsProxy {
    PlainTextContentsProxy INSTANCE = ASMProxyFactory.create(PlainTextContentsProxy.class);

    @MethodInvoker(name = "text")
    String getText(Object target);
}
