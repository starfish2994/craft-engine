package net.momirealms.craftengine.proxy.spottedleaf.dataconverter.minecraft.datatypes;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry", activeIf = "has_patch=paper")
public interface MCTypeRegistryProxy {
    MCTypeRegistryProxy INSTANCE = ASMProxyFactory.create(MCTypeRegistryProxy.class);
    Object ITEM_STACK = INSTANCE.getItemStack();

    @FieldGetter(name = "ITEM_STACK")
    Object getItemStack();
}
