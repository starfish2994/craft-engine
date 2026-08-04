package net.momirealms.craftengine.proxy.minecraft.world;

import net.momirealms.sparrow.reflection.clazz.SparrowClass;
import net.momirealms.sparrow.reflection.proxy.ASMProxyFactory;
import net.momirealms.sparrow.reflection.proxy.annotation.FieldGetter;
import net.momirealms.sparrow.reflection.proxy.annotation.MethodInvoker;
import net.momirealms.sparrow.reflection.proxy.annotation.ReflectionProxy;

@ReflectionProxy(name = "net.minecraft.world.InteractionResult")
public interface InteractionResultProxy {
    InteractionResultProxy INSTANCE = ASMProxyFactory.create(InteractionResultProxy.class);
    Class<?> CLASS = SparrowClass.find("net.minecraft.world.InteractionResult");

    @FieldGetter(name = "SUCCESS_SERVER", isStatic = true, activeIf = "min_version=1.21.2")
    Object getSuccessServer();

    @FieldGetter(name = "SUCCESS", isStatic = true)
    Object getSuccess();

    @FieldGetter(name = "CONSUME", isStatic = true)
    Object getConsume();

    @FieldGetter(name = "FAIL", isStatic = true)
    Object getFail();

    @FieldGetter(name = "PASS", isStatic = true)
    Object getPass();

    @MethodInvoker(name = "consumesAction")
    boolean consumesAction(Object target);

    @ReflectionProxy(name = "net.minecraft.world.InteractionResult$Success", activeIf = "min_version=1.21.2")
    interface SuccessProxy {
        SuccessProxy INSTANCE = ASMProxyFactory.create(SuccessProxy.class);
        Class<?> CLASS = SparrowClass.find("net.minecraft.world.InteractionResult$Success");

        @MethodInvoker(name = "heldItemTransformedTo")
        Object heldItemTransformedTo(Object target);
    }
}
