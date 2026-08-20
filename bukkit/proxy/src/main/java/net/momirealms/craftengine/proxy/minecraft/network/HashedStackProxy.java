package net.momirealms.craftengine.proxy.minecraft.network;

import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.network.HashedStack", activeIf = "min_version=1.21.5")
public interface HashedStackProxy {
    HashedStackProxy INSTANCE = ASMProxyFactory.create(HashedStackProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.HashedStack");
    Object STREAM_CODEC = INSTANCE != null ? INSTANCE.getStreamCodec() : null;

    @FieldGetter(name = "STREAM_CODEC", isStatic = true)
    Object getStreamCodec();

    @MethodInvoker(name = "matches")
    boolean matches(Object target, @Type(clazz = ItemStackProxy.class) Object stack, @Type(clazz = HashedPatchMapProxy.HashGeneratorProxy.class) Object hashGenerator);

    @ReflectionProxy(name = "net.minecraft.network.HashedStack$ActualItem", activeIf = "min_version=1.21.5")
    interface ActualItemProxy {
        ActualItemProxy INSTANCE = ASMProxyFactory.create(ActualItemProxy.class);
        Class<?> CLASS = SparrowClass.find("net.minecraft.network.HashedStack$ActualItem");

        @MethodInvoker(name = "count")
        int count(Object target);
    }
}
