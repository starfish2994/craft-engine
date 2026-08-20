package net.momirealms.craftengine.proxy.minecraft.network.codec;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.network.codec.ByteBufCodecs", activeIf = "min_version=1.20.5")
public interface ByteBufCodecsProxy {
    ByteBufCodecsProxy INSTANCE = ASMProxyFactory.create(ByteBufCodecsProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.network.codec.ByteBufCodecs");

    @MethodInvoker(name = "trackDepth", isStatic = true, activeIf = "min_version=1.21.4 && has_patch=paper")
    Object trackDepth(@Type(clazz = StreamCodecProxy.class) Object codec);
}
