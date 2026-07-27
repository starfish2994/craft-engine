package net.momirealms.craftengine.proxy.minecraft.world.level;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.*;

import java.util.stream.Stream;

@ReflectionProxy(name = "net.minecraft.world.level.ChunkPos")
public interface ChunkPosProxy {
    ChunkPosProxy INSTANCE = ASMProxyFactory.create(ChunkPosProxy.class);

    @ConstructorInvoker
    Object newInstance(int x, int z);

    @FieldGetter(name = "x")
    int getX(Object target);

    @FieldGetter(name = "z")
    int getZ(Object target);

    @MethodInvoker(name = "rangeClosed", isStatic = true)
    Stream<Object> rangeClosed(@Type(clazz = ChunkPosProxy.class) Object from, @Type(clazz = ChunkPosProxy.class) Object to);

    @MethodInvoker(name = "getWorldPosition")
    Object getWorldPosition(Object target);

    @MethodInvoker(name = "getMinBlockX")
    int getMinBlockX(Object target);

    @MethodInvoker(name = "getMinBlockZ")
    int getMinBlockZ(Object target);

    @MethodInvoker(name = "getMaxBlockX")
    int getMaxBlockX(Object target);

    @MethodInvoker(name = "getMaxBlockZ")
    int getMaxBlockZ(Object target);
}
