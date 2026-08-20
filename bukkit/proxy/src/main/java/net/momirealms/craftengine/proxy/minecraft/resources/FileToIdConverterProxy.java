package net.momirealms.craftengine.proxy.minecraft.resources;

import net.momirealms.craftengine.proxy.minecraft.server.packs.resources.ResourceManagerProxy;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

import java.util.Map;

@ReflectionProxy(name = "net.minecraft.resources.FileToIdConverter")
public interface FileToIdConverterProxy {
    FileToIdConverterProxy INSTANCE = ASMProxyFactory.create(FileToIdConverterProxy.class);

    @MethodInvoker(name = "json", isStatic = true)
    Object json(String name);

    @MethodInvoker(name = "listMatchingResources")
    Map<Object, Object> listMatchingResources(Object target, @Type(clazz = ResourceManagerProxy.class) Object resourceManager);

    @MethodInvoker(name = "listMatchingResourceStacks")
    Map<Object, java.util.List<Object>> listMatchingResourceStacks(Object target, @Type(clazz = ResourceManagerProxy.class) Object resourceManager);

    @MethodInvoker(name = "fileToId")
    Object fileToId(Object target, @Type(clazz = IdentifierProxy.class) Object file);
}