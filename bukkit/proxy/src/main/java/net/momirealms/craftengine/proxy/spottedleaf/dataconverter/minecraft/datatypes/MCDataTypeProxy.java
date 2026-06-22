package net.momirealms.craftengine.proxy.spottedleaf.dataconverter.minecraft.datatypes;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "ca.spottedleaf.dataconverter.minecraft.datatypes.MCDataType", activeIf = "has_patch=paper && !version=1.21.5 && !version=26.2"/*todo临时禁用等paper把补丁打完*/)
public interface MCDataTypeProxy {
    MCDataTypeProxy INSTANCE = ASMProxyFactory.create(MCDataTypeProxy.class);
}
