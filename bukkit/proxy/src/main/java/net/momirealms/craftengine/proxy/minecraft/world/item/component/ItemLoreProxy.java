package net.momirealms.craftengine.proxy.minecraft.world.item.component;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

import java.util.List;

@ReflectionProxy(name = "net.minecraft.world.item.component.ItemLore", activeIf = "min_version=1.20.5")
public interface ItemLoreProxy {
    ItemLoreProxy INSTANCE = ASMProxyFactory.create(ItemLoreProxy.class);

    @FieldGetter(name = "styledLines")
    List<Object> getStyleLines(Object target);

    @FieldGetter(name = "lines")
    List<Object> getLines(Object target);
}
