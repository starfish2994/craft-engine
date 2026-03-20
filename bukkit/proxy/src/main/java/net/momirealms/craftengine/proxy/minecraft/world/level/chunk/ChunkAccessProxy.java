package net.momirealms.craftengine.proxy.minecraft.world.level.chunk;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldSetter;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

import java.util.Map;

@ReflectionProxy(name = "net.minecraft.world.level.chunk.ChunkAccess")
public interface ChunkAccessProxy {
    ChunkAccessProxy INSTANCE = ASMProxyFactory.create(ChunkAccessProxy.class);

    @FieldGetter(name = "blockEntities")
    Map<?, ?> getBlockEntities(Object target);

    @FieldSetter(name = "blockEntities")
    void setBlockEntities(Object target, Map<?, ?> value);

    @MethodInvoker(name = "isUnsaved")
    boolean isUnsaved(Object target);

    @MethodInvoker(name = "getSections")
    Object[] getSections(Object target);

    @MethodInvoker(name = "setUnsaved", activeIf = "max_version=1.21.1")
    void setUnsaved(Object target, boolean needsSaving);
}
