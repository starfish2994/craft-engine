package net.momirealms.craftengine.bukkit.plugin.injector;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlockStateProperties;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBuiltInRegistries;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MLootContextParams;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.*;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ObjectHolder;
import net.momirealms.craftengine.core.util.ReflectionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class BlockStateGenerator {
    private static MethodHandle constructor$CraftEngineBlockState;
    public static Object instance$StateDefinition$Factory;

    public static void init() throws ReflectiveOperationException {
        ByteBuddy byteBuddy = new ByteBuddy(ClassFileVersion.JAVA_V17);
        String packageWithName = BlockStateGenerator.class.getName();
        String generatedStateClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineBlockState";
        DynamicType.Builder<?> stateBuilder = byteBuddy
                .subclass(CoreReflections.clazz$BlockState, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedStateClassName)
                .defineField("immutableBlockState", ImmutableBlockState.class, Visibility.PUBLIC)
                .implement(DelegatingBlockState.class)
                .method(ElementMatchers.named("blockState"))
                .intercept(FieldAccessor.ofField("immutableBlockState"))
                .method(ElementMatchers.named("setBlockState"))
                .intercept(FieldAccessor.ofField("immutableBlockState"))
                .method(ElementMatchers.is(CoreReflections.method$BlockStateBase$getDrops))
                .intercept(MethodDelegation.to(GetDropsInterceptor.INSTANCE))
                .method(ElementMatchers.is(CoreReflections.method$StateHolder$hasProperty))
                .intercept(MethodDelegation.to(HasPropertyInterceptor.INSTANCE))
                .method(ElementMatchers.is(CoreReflections.method$StateHolder$getValue))
                .intercept(MethodDelegation.to(GetPropertyValueInterceptor.INSTANCE))
                .method(ElementMatchers.is(CoreReflections.method$StateHolder$setValue))
                .intercept(MethodDelegation.to(SetPropertyValueInterceptor.INSTANCE))
                .method(ElementMatchers.is(CoreReflections.method$BlockStateBase$isBlock))
                .intercept(MethodDelegation.to(IsBlockInterceptor.INSTANCE))
                .method(ElementMatchers.is(CoreReflections.method$BlockStateBase$isHolderSetBlock))
                .intercept(MethodDelegation.to(IsHolderSetBlockInterceptor.INSTANCE));
        if (CoreReflections.method$BlockStateBase$isHolderBlock != null) {
            stateBuilder = stateBuilder
                    .method(ElementMatchers.is(CoreReflections.method$BlockStateBase$isHolderBlock))
                    .intercept(MethodDelegation.to(IsHolderBlockInterceptor.INSTANCE));
        }
        if (CoreReflections.method$BlockStateBase$isResourceKeyBlock != null) {
            stateBuilder = stateBuilder
                    .method(ElementMatchers.is(CoreReflections.method$BlockStateBase$isResourceKeyBlock))
                    .intercept(MethodDelegation.to(IsResourceKeyBlockInterceptor.INSTANCE));
        }
        Class<?> clazz$CraftEngineBlock = stateBuilder.make().load(BlockStateGenerator.class.getClassLoader()).getLoaded();
        constructor$CraftEngineBlockState = VersionHelper.isOrAbove1_20_5() ?
                MethodHandles.publicLookup().in(clazz$CraftEngineBlock)
                .findConstructor(clazz$CraftEngineBlock, MethodType.methodType(void.class, CoreReflections.clazz$Block, Reference2ObjectArrayMap.class, MapCodec.class))
                .asType(MethodType.methodType(CoreReflections.clazz$BlockState, CoreReflections.clazz$Block, Reference2ObjectArrayMap.class, MapCodec.class)) :
                MethodHandles.publicLookup().in(clazz$CraftEngineBlock)
                .findConstructor(clazz$CraftEngineBlock, MethodType.methodType(void.class, CoreReflections.clazz$Block, ImmutableMap.class, MapCodec.class))
                .asType(MethodType.methodType(CoreReflections.clazz$BlockState, CoreReflections.clazz$Block, ImmutableMap.class, MapCodec.class));

        String generatedFactoryClassName = packageWithName.substring(0, packageWithName.lastIndexOf('.')) + ".CraftEngineStateFactory";
        DynamicType.Builder<?> factoryBuilder = byteBuddy
                .subclass(Object.class, ConstructorStrategy.Default.IMITATE_SUPER_CLASS_OPENING)
                .name(generatedFactoryClassName)
                .implement(CoreReflections.clazz$StateDefinition$Factory)
                .method(ElementMatchers.named("create"))
                .intercept(MethodDelegation.to(CreateStateInterceptor.INSTANCE));

        Class<?> clazz$Factory = factoryBuilder.make().load(BlockStateGenerator.class.getClassLoader()).getLoaded();
        instance$StateDefinition$Factory = ReflectionUtils.getTheOnlyConstructor(clazz$Factory).newInstance();
    }

    public static class GetDropsInterceptor {
        public static final GetDropsInterceptor INSTANCE = new GetDropsInterceptor();

        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            ImmutableBlockState state = ((DelegatingBlockState) thisObj).blockState();
            if (state == null) return List.of();
            Object builder = args[0];
            Object vec3 = FastNMS.INSTANCE.method$LootParams$Builder$getOptionalParameter(builder, MLootContextParams.ORIGIN);
            if (vec3 == null) return List.of();

            Object tool = FastNMS.INSTANCE.method$LootParams$Builder$getOptionalParameter(builder, MLootContextParams.TOOL);
            Item<ItemStack> item = BukkitItemManager.instance().wrap(tool == null ? null : FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(tool));
            Object optionalPlayer = FastNMS.INSTANCE.method$LootParams$Builder$getOptionalParameter(builder, MLootContextParams.THIS_ENTITY);
            if (!CoreReflections.clazz$Player.isInstance(optionalPlayer)) {
                optionalPlayer = null;
            }

            // do not drop if it's not the correct tool
            BlockSettings settings = state.settings();
            if (optionalPlayer != null && settings.requireCorrectTool()) {
                if (item.isEmpty()) return List.of();
                if (!settings.isCorrectTool(item.id()) &&
                        (!settings.respectToolComponent() || !FastNMS.INSTANCE.method$ItemStack$isCorrectToolForDrops(tool, state.customBlockState().literalObject()))) {
                    return List.of();
                }
            }

            Object serverLevel = FastNMS.INSTANCE.method$LootParams$Builder$getLevel(builder);
            World world = new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(serverLevel));
            ContextHolder.Builder lootBuilder = new ContextHolder.Builder()
                    .withParameter(DirectContextParameters.POSITION, new WorldPosition(world, FastNMS.INSTANCE.field$Vec3$x(vec3), FastNMS.INSTANCE.field$Vec3$y(vec3), FastNMS.INSTANCE.field$Vec3$z(vec3)));
            if (!item.isEmpty()) {
                lootBuilder.withParameter(DirectContextParameters.ITEM_IN_HAND, item);
            }
            BukkitServerPlayer player = optionalPlayer != null ? BukkitCraftEngine.instance().adapt(FastNMS.INSTANCE.method$ServerPlayer$getBukkitEntity(optionalPlayer)) : null;
            if (player != null) {
                lootBuilder.withParameter(DirectContextParameters.PLAYER, player);
            }
            Float radius = (Float) FastNMS.INSTANCE.method$LootParams$Builder$getOptionalParameter(builder, MLootContextParams.EXPLOSION_RADIUS);
            if (radius != null) {
                lootBuilder.withParameter(DirectContextParameters.EXPLOSION_RADIUS, radius);
            }
            return state.getDrops(lootBuilder, world, player).stream().map(Item::getLiteralObject).toList();
        }
    }

    public static class HasPropertyInterceptor {
        public static final HasPropertyInterceptor INSTANCE = new HasPropertyInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            Object property = args[0];
            if (property != MBlockStateProperties.WATERLOGGED) return false;
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState state = customState.blockState();
            if (state == null) return false;
            Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
            return waterloggedProperty != null;
        }
    }

    public static class GetPropertyValueInterceptor {
        public static final GetPropertyValueInterceptor INSTANCE = new GetPropertyValueInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            Object property = args[0];
            if (property != MBlockStateProperties.WATERLOGGED) return null;
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState state = customState.blockState();
            if (state == null) return null;
            Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
            if (waterloggedProperty == null) return null;
            return state.get(waterloggedProperty);
        }
    }

    public static class SetPropertyValueInterceptor {
        public static final SetPropertyValueInterceptor INSTANCE = new SetPropertyValueInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public Object intercept(@This Object thisObj, @AllArguments Object[] args) {
            Object property = args[0];
            if (property != MBlockStateProperties.WATERLOGGED) return thisObj;
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState state = customState.blockState();
            if (state == null) return thisObj;
            Property<Boolean> waterloggedProperty = (Property<Boolean>) state.owner().value().getProperty("waterlogged");
            if (waterloggedProperty == null) return thisObj;
            return state.with(waterloggedProperty, (boolean) args[1]).customBlockState().literalObject();
        }
    }

    public static class IsBlockInterceptor {
        public static final IsBlockInterceptor INSTANCE = new IsBlockInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState thisState = customState.blockState();
            if (thisState == null) return false;
            if (!(args[0] instanceof DelegatingBlock delegatingBlock)) return false;
            BlockBehavior behavior = delegatingBlock.behaviorDelegate().value();
            if (behavior == null) return false;
            return behavior.block().equals(thisState.owner().value());
        }
    }

    public static class IsHolderSetBlockInterceptor {
        public static final IsHolderSetBlockInterceptor INSTANCE = new IsHolderSetBlockInterceptor();

        @SuppressWarnings("unchecked")
        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState thisState = customState.blockState();
            if (thisState == null) return false;
            CustomBlock thisBlock = thisState.owner().value();
            for (Object holder : (Iterable<Object>) args[0]) {
                Object block = FastNMS.INSTANCE.method$Holder$value(holder);
                if (!(block instanceof DelegatingBlock delegatingBlock)) continue;
                BlockBehavior behavior = delegatingBlock.behaviorDelegate().value();
                if (behavior == null) continue;
                if (behavior.block().equals(thisBlock)) return true;
            }
            return false;
        }
    }

    public static class IsHolderBlockInterceptor {
        public static final IsHolderBlockInterceptor INSTANCE = new IsHolderBlockInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState thisState = customState.blockState();
            if (thisState == null) return false;
            CustomBlock thisBlock = thisState.owner().value();
            Object block = FastNMS.INSTANCE.method$Holder$value(args[0]);
            if (!(block instanceof DelegatingBlock delegatingBlock)) return false;
            BlockBehavior behavior = delegatingBlock.behaviorDelegate().value();
            if (behavior == null) return false;
            return behavior.block().equals(thisBlock);
        }
    }

    public static class IsResourceKeyBlockInterceptor {
        public static final IsResourceKeyBlockInterceptor INSTANCE = new IsResourceKeyBlockInterceptor();

        @RuntimeType
        public boolean intercept(@This Object thisObj, @AllArguments Object[] args) {
            DelegatingBlockState customState = (DelegatingBlockState) thisObj;
            ImmutableBlockState thisState = customState.blockState();
            if (thisState == null) return false;
            CustomBlock thisBlock = thisState.owner().value();
            Object block = FastNMS.INSTANCE.method$HolderGetter$getResourceKey(MBuiltInRegistries.BLOCK, args[0])
                    .map(FastNMS.INSTANCE::method$Holder$value)
                    .orElse(null);
            if (!(block instanceof DelegatingBlock delegatingBlock)) return false;
            BlockBehavior behavior = delegatingBlock.behaviorDelegate().value();
            if (behavior == null) return false;
            return behavior.block().equals(thisBlock);
        }
    }

    public static class CreateStateInterceptor {
        public static final CreateStateInterceptor INSTANCE = new CreateStateInterceptor();

        @RuntimeType
        public Object intercept(@AllArguments Object[] args) throws Throwable {
            return constructor$CraftEngineBlockState.invoke(args[0], args[1], args[2]);
        }
    }
}
