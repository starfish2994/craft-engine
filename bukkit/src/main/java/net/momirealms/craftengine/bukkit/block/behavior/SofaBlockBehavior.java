package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LevelUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.block.properties.type.SofaShape;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidStateProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidsProxy;

import java.util.Optional;
import java.util.concurrent.Callable;

public final class SofaBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<SofaBlockBehavior> FACTORY = new Factory();
    public final Property<Direction> facingProperty;
    public final Property<SofaShape> shapeProperty;

    private SofaBlockBehavior(BlockDefinition block,
                              Property<Direction> facing,
                              Property<SofaShape> shape) {
        super(block);
        this.facingProperty = facing;
        this.shapeProperty = shape;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        BlockPos clickedPos = context.getClickedPos();
        ImmutableBlockState blockState = state.owner().value().defaultState()
                .with(this.facingProperty, context.getHorizontalDirection());
        if (super.waterloggedProperty != null) {
            Object fluidState = BlockGetterProxy.INSTANCE.getFluidState(context.getLevel().serverWorld(), LocationUtils.toBlockPos(clickedPos));
            blockState = blockState.with(this.waterloggedProperty, FluidStateProxy.INSTANCE.getType(fluidState) == FluidsProxy.WATER);
        }
        return blockState.with(this.shapeProperty, getSofaShape(blockState, context.getLevel().serverWorld(), clickedPos));
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object level = args[updateShape$level];
        Object blockPos = args[updateShape$blockPos];
        Object blockState = args[0];
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isEmpty()) return blockState;
        ImmutableBlockState customState = optionalCustomState.get();
        if (super.waterloggedProperty != null && customState.get(this.waterloggedProperty)) {
            LevelUtils.scheduleFluidTick(args[updateShape$level], args[updateShape$blockPos], FluidsProxy.WATER, 5);
        }
        Direction direction = DirectionUtils.fromNMSDirection(VersionHelper.isOrAbove1_21_2() ? args[4] : args[1]);
        SofaShape sofaShape = getSofaShape(customState, level, LocationUtils.fromBlockPos(blockPos));
        return direction.axis().isHorizontal()
                ? customState.with(this.shapeProperty, sofaShape).customBlockState().literalObject()
                : superMethod.call();
    }

    private SofaShape getSofaShape(ImmutableBlockState state, Object level, BlockPos pos) {
        Direction direction = state.get(this.facingProperty);
        Object relativeBlockState = BlockGetterProxy.INSTANCE.getBlockState(level, LocationUtils.toBlockPos(pos.relative(direction.opposite())));
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(relativeBlockState);
        if (optionalCustomState.isPresent()) {
            ImmutableBlockState customState = optionalCustomState.get();
            Optional<SofaBlockBehavior> optionalStairsBlockBehavior = customState.behavior().getAs(SofaBlockBehavior.class);
            if (optionalStairsBlockBehavior.isPresent()) {
                SofaBlockBehavior stairsBlockBehavior = optionalStairsBlockBehavior.get();
                Direction direction1 = customState.get(stairsBlockBehavior.facingProperty);
                if (direction1.axis() != state.get(this.facingProperty).axis() && canTakeShape(state, level, pos, direction1)) {
                    if (direction1 == direction.counterClockWise()) {
                        return SofaShape.INNER_LEFT;
                    }
                    return SofaShape.INNER_RIGHT;
                }
            }
        }
        return SofaShape.STRAIGHT;
    }

    private boolean canTakeShape(ImmutableBlockState state, Object level, BlockPos pos, Direction face) {
        Object blockState = BlockGetterProxy.INSTANCE.getBlockState(level, LocationUtils.toBlockPos(pos.relative(face)));
        Optional<ImmutableBlockState> optionalAnotherState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalAnotherState.isEmpty()) {
            return true;
        }
        ImmutableBlockState anotherState = optionalAnotherState.get();
        Optional<SofaBlockBehavior> optionalBehavior = anotherState.behavior().getAs(SofaBlockBehavior.class);
        if (optionalBehavior.isEmpty()) {
            return true;
        }
        SofaBlockBehavior anotherBehavior = optionalBehavior.get();
        return anotherState.get(anotherBehavior.facingProperty) != state.get(this.facingProperty);
    }

    private static class Factory implements BlockBehaviorFactory<SofaBlockBehavior> {

        @Override
        public SofaBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new SofaBlockBehavior(
                    block,
                    BlockBehaviorFactory.getProperty(section.path(), block, "facing", Direction.class),
                    BlockBehaviorFactory.getProperty(section.path(), block, "shape", SofaShape.class)
            );
        }
    }
}
