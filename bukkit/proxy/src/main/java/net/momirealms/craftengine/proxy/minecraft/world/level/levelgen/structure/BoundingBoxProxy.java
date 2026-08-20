package net.momirealms.craftengine.proxy.minecraft.world.level.levelgen.structure;

import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.ConstructorInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.level.levelgen.structure.BoundingBox")
public interface BoundingBoxProxy {
    BoundingBoxProxy INSTANCE = ASMProxyFactory.create(BoundingBoxProxy.class);

    @ConstructorInvoker
    Object newInstance(int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

    @FieldGetter(name = "minX")
    int getMinX(Object target);

    @FieldGetter(name = "minZ")
    int getMinZ(Object target);

    @FieldGetter(name = "maxX")
    int getMaxX(Object target);

    @FieldGetter(name = "maxZ")
    int getMaxZ(Object target);
}
