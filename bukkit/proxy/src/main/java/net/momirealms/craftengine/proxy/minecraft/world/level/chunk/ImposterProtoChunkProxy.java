package net.momirealms.craftengine.proxy.minecraft.world.level.chunk;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.level.chunk.ImposterProtoChunk", activeIf = "min_version=1.21.4")
public interface ImposterProtoChunkProxy {
    ImposterProtoChunkProxy INSTANCE = ASMProxyFactory.create(ImposterProtoChunkProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.level.chunk.ImposterProtoChunk");

    @FieldGetter(name = "wrapped")
    Object getWrapped(Object target);
}
