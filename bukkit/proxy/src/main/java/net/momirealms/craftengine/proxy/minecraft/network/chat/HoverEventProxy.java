package net.momirealms.craftengine.proxy.minecraft.network.chat;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.network.chat.HoverEvent")
public interface HoverEventProxy {
    HoverEventProxy INSTANCE = ASMProxyFactory.create(HoverEventProxy.class);

    @ReflectionProxy(name = "net.minecraft.network.chat.HoverEvent$Action")
    interface ActionProxy {
        ActionProxy INSTANCE = ASMProxyFactory.create(ActionProxy.class);

        @FieldGetter(name = "SHOW_TEXT", isStatic = true)
        Object getShowText();

        @FieldGetter(name = "SHOW_ITEM", isStatic = true)
        Object getShowItem();
    }

    @ReflectionProxy(name = "net.minecraft.network.chat.HoverEvent$ShowItem")
    interface ShowItemProxy {
        ShowItemProxy INSTANCE = ASMProxyFactory.create(ShowItemProxy.class);

        @FieldGetter(name = "item")
        Object getItem(Object target);
    }

    @ReflectionProxy(name = "net.minecraft.network.chat.HoverEvent$ShowText")
    interface ShowTextProxy {
        ShowTextProxy INSTANCE = ASMProxyFactory.create(ShowTextProxy.class);

        @FieldGetter(name = "value")
        Object getValue(Object target);
    }
}
