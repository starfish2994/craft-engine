package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.structure;

import net.momirealms.craftengine.proxy.minecraft.core.HolderProxy;
import net.momirealms.craftengine.proxy.minecraft.core.RegistryAccessProxy;
import net.momirealms.craftengine.proxy.minecraft.resources.ResourceKeyProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.ChunkPosProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelHeightAccessorProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.biome.BiomeSourceProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.chunk.ChunkGeneratorProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.RandomStateProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManagerProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.function.Predicate;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.structure.Structure")
public interface StructureProxy {
    StructureProxy INSTANCE = ASMProxyFactory.create(StructureProxy.class);

    @MethodInvoker(name = "generate", activeIf = "max_version=1.21.3")
    Object generate$0(Object target,
                      @Type(clazz = RegistryAccessProxy.class) Object registryAccess,
                      @Type(clazz = ChunkGeneratorProxy.class) Object chunkGenerator,
                      @Type(clazz = BiomeSourceProxy.class) Object biomeSource,
                      @Type(clazz = RandomStateProxy.class) Object randomState,
                      @Type(clazz = StructureTemplateManagerProxy.class) Object structureTemplateManager,
                      long seed,
                      @Type(clazz = ChunkPosProxy.class) Object sourceChunkPos,
                      int references,
                      @Type(clazz = LevelHeightAccessorProxy.class) Object heightAccessor,
                      Predicate<Object> validBiome
    );

    @MethodInvoker(name = "generate", activeIf = "min_version=1.21.4")
    Object generate$1(Object target,
                      @Type(clazz = HolderProxy.class) Object selected,
                      @Type(clazz = ResourceKeyProxy.class) Object dimension,
                      @Type(clazz = RegistryAccessProxy.class) Object registryAccess,
                      @Type(clazz = ChunkGeneratorProxy.class) Object chunkGenerator,
                      @Type(clazz = BiomeSourceProxy.class) Object biomeSource,
                      @Type(clazz = RandomStateProxy.class) Object randomState,
                      @Type(clazz = StructureTemplateManagerProxy.class) Object structureTemplateManager,
                      long seed,
                      @Type(clazz = ChunkPosProxy.class) Object sourceChunkPos,
                      int references,
                      @Type(clazz = LevelHeightAccessorProxy.class) Object heightAccessor,
                      Predicate<Object> validBiome
    );
}
