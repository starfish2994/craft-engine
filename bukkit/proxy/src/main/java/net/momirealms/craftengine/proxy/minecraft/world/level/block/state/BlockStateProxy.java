package net.momirealms.craftengine.proxy.minecraft.world.level.block.state;

import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.properties.PropertyProxy;
import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;
import net.momirealms.sparrow.reflection.proxy.annotation.Type;

@ReflectionProxy(name = "net.minecraft.world.level.block.state.BlockState")
public interface BlockStateProxy extends BlockBehaviourProxy.BlockStateBaseProxy {
    BlockStateProxy INSTANCE = ASMProxyFactory.create(BlockStateProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.level.block.state.BlockState");

    @MethodInvoker(name = "cycle")
    Object cycle(Object target, @Type(clazz = PropertyProxy.class) Object property);
}
