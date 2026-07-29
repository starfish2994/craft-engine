package net.momirealms.craftengine.proxy.minecraft.network;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.network.HashedPatchMap", activeIf = "min_version=1.21.5")
public interface HashedPatchMapProxy {
    HashedPatchMapProxy INSTANCE = ASMProxyFactory.create(HashedPatchMapProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.HashedPatchMap");

    @ReflectionProxy(name = "net.minecraft.network.HashedPatchMap$HashGenerator", activeIf = "min_version=1.21.5")
    interface HashGeneratorProxy {
        HashGeneratorProxy INSTANCE = ASMProxyFactory.create(HashGeneratorProxy.class);
        Class<?> CLASS = SparrowClass.find("net.minecraft.network.HashedPatchMap$HashGenerator");
    }
}
