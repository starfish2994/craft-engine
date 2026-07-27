package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.structure;

import net.momirealms.craftengine.proxy.minecraft.util.RandomSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.ChunkPosProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.StructureManagerProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.WorldGenLevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.ChunkGeneratorProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.structure.StructureStart")
public interface StructureStartProxy {
    StructureStartProxy INSTANCE = ASMProxyFactory.create(StructureStartProxy.class);

    @MethodInvoker(name = "isValid")
    boolean isValid(Object target);

    @MethodInvoker(name = "getBoundingBox")
    Object getBoundingBox(Object target);

    @MethodInvoker(name = "placeInChunk")
    void placeInChunk(Object target,
            @Type(clazz = WorldGenLevelProxy.class) Object level,
            @Type(clazz = StructureManagerProxy.class) Object structureManager,
            @Type(clazz = ChunkGeneratorProxy.class) Object generator,
            @Type(clazz = RandomSourceProxy.class) Object random,
            @Type(clazz = BoundingBoxProxy.class) Object chunkBB,
            @Type(clazz = ChunkPosProxy.class) Object chunkPos
    );
}
