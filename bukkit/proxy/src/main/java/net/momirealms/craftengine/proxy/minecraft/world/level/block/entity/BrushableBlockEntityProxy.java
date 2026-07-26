package net.momirealms.craftengine.proxy.minecraft.world.level.block.entity;

import net.momirealms.craftengine.proxy.minecraft.resources.ResourceKeyProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldSetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.level.block.entity.BrushableBlockEntity")
public interface BrushableBlockEntityProxy {
    BrushableBlockEntityProxy INSTANCE = ASMProxyFactory.create(BrushableBlockEntityProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.level.block.entity.BrushableBlockEntity");

    @FieldGetter(name = "lootTable")
    Object getLootTable(Object target);

    @FieldSetter(name = "lootTable")
    void setLootTable(Object target, @Type(clazz = ResourceKeyProxy.class) Object lootTable);

    @FieldGetter(name = "item")
    Object getItem(Object target);

    @FieldSetter(name = "item")
    void setItem(Object target, @Type(clazz = ItemStackProxy.class) Object item);
}
