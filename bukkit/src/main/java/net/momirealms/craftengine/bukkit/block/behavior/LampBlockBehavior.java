package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

@SuppressWarnings("DuplicatedCode")
public class LampBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<Boolean> litProperty;
    private final Property<Boolean> poweredProperty;
    private final boolean canOpenWithHand;
    private final boolean redstoneToggleMode;

    public LampBlockBehavior(CustomBlock block, Property<Boolean> litProperty, Property<Boolean> poweredProperty, boolean canOpenWithHand, boolean redstoneToggleMode) {
        super(block);
        this.litProperty = litProperty;
        this.poweredProperty = poweredProperty;
        this.canOpenWithHand = canOpenWithHand;
        this.redstoneToggleMode = redstoneToggleMode;
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        if (!this.canOpenWithHand) {
            return InteractionResult.PASS;
        }
        LampBlockBehavior behavior = state.behavior().getAs(LampBlockBehavior.class).orElse(null);
        if (behavior == null) return InteractionResult.PASS;
        FastNMS.INSTANCE.method$LevelWriter$setBlock(
                context.getLevel().serverWorld(),
                LocationUtils.toBlockPos(context.getClickedPos()),
                state.cycle(behavior.litProperty).cycle(behavior.poweredProperty).customBlockState().literalObject(),
                2
        );
        Optional.ofNullable(context.getPlayer()).ifPresent(p -> p.swingHand(context.getHand()));
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        if (this.canOpenWithHand || this.redstoneToggleMode) return state;
        Object level = context.getLevel().serverWorld();
        state = state.with(this.litProperty, FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, LocationUtils.toBlockPos(context.getClickedPos())));
        state = state.with(this.poweredProperty, FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, LocationUtils.toBlockPos(context.getClickedPos())));
        return state;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockState = args[0];
        Object world = args[1];
        if (this.canOpenWithHand || this.redstoneToggleMode || !CoreReflections.clazz$ServerLevel.isInstance(world)) return;
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isEmpty()) return;
        Object blockPos = args[2];
        ImmutableBlockState customState = optionalCustomState.get();
        if (customState.get(this.litProperty) && !FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(world, blockPos)) {
            if (FastNMS.INSTANCE.method$CraftEventFactory$callRedstoneChange(world, blockPos, 0, 15).getNewCurrent() != 15) {
                return;
            }
            FastNMS.INSTANCE.method$LevelWriter$setBlock(world, blockPos, customState.cycle(this.litProperty).cycle(this.poweredProperty).customBlockState().literalObject(), 2);
        }
    }

    @Override
    public void onPlace(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.canOpenWithHand || !this.redstoneToggleMode) return;
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        Object oldState = args[3];
        if (FastNMS.INSTANCE.method$BlockState$getBlock(oldState) != FastNMS.INSTANCE.method$BlockState$getBlock(state) && CoreReflections.clazz$ServerLevel.isInstance(level)) {
            Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(state);
            if (optionalCustomState.isEmpty()) return;
            checkAndFlip(optionalCustomState.get(), level, pos);
        }
    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object blockState = args[0];
        Object world = args[1];
        if (this.canOpenWithHand || !CoreReflections.clazz$ServerLevel.isInstance(world)) return;
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isEmpty()) return;
        Object blockPos = args[2];
        ImmutableBlockState customState = optionalCustomState.get();
        boolean lit = customState.get(this.litProperty);
        if (this.redstoneToggleMode) {
            checkAndFlip(customState, world, blockPos);
            return;
        }
        if (lit != FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(world, blockPos)) {
            if (lit) {
                FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleBlockTick(world, blockPos, thisBlock, 4);
            } else {
                if (FastNMS.INSTANCE.method$CraftEventFactory$callRedstoneChange(world, blockPos, 0, 15).getNewCurrent() != 15) {
                    return;
                }
                FastNMS.INSTANCE.method$LevelWriter$setBlock(world, blockPos, customState.cycle(this.litProperty).cycle(this.poweredProperty).customBlockState().literalObject(), 2);
            }
        }
    }

    private void checkAndFlip(ImmutableBlockState customState, Object level, Object pos) {
        boolean hasNeighborSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, pos);
        boolean isPowered = customState.get(this.poweredProperty);
        if (hasNeighborSignal != isPowered) {
            ImmutableBlockState blockState = customState;
            if (!isPowered) {
                blockState = blockState.cycle(this.litProperty);
            }
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.poweredProperty, hasNeighborSignal).customBlockState().literalObject(), 3);
        }

    }

    @SuppressWarnings("unchecked")
    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Boolean> lit = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("lit"), "warning.config.block.behavior.lamp.missing_lit");
            Property<Boolean> powered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("powered"), "warning.config.block.behavior.lamp.missing_powered");
            boolean canOpenWithHand = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("can-open-with-hand", false), "can-open-with-hand");
            boolean redstoneToggleMode = !canOpenWithHand && ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("redstone-toggle-mode", false), "redstone-toggle-mode");
            return new LampBlockBehavior(block, lit, powered, canOpenWithHand, redstoneToggleMode);
        }
    }
}
