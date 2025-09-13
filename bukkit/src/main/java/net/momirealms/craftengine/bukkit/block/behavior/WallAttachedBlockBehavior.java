package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;

import java.util.Map;
import java.util.concurrent.Callable;

public class WallAttachedBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<HorizontalDirection> facingProperty;

    public WallAttachedBlockBehavior(CustomBlock customBlock, Property<HorizontalDirection> facingProperty) {
        super(customBlock);
        this.facingProperty = facingProperty;
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return args[0];
        WallAttachedBlockBehavior behavior = state.behavior().getAs(WallAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return state;
        HorizontalDirection direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]).opposite().toHorizontalDirection();
        return direction == state.get(behavior.facingProperty) && !FastNMS.INSTANCE.method$BlockStateBase$canSurvive(args[0], args[updateShape$level], args[updateShape$blockPos]) ? MBlocks.AIR$defaultState : args[0];
    }

    @Override
    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return false;
        WallAttachedBlockBehavior behavior = state.behavior().getAs(WallAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return false;
        HorizontalDirection direction = state.get(behavior.facingProperty);
        BlockPos blockPos = LocationUtils.fromBlockPos(args[2]).relative(direction.opposite().toDirection());
        Object nmsPos = LocationUtils.toBlockPos(blockPos);
        Object nmsState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(args[1], nmsPos);
        return FastNMS.INSTANCE.method$BlockStateBase$isFaceSturdy(nmsState, args[1], nmsPos, DirectionUtils.toNMSDirection(direction.opposite().toDirection()), CoreReflections.instance$SupportType$FULL);
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        WallAttachedBlockBehavior behavior = state.behavior().getAs(WallAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return null;
        World level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Direction[] nearestLookingDirections = context.getNearestLookingDirections();
        for (Direction direction : nearestLookingDirections) {
            if (direction.axis().isHorizontal()) {
                state = state.with(behavior.facingProperty, direction.opposite().toHorizontalDirection());
                if (FastNMS.INSTANCE.method$BlockStateBase$canSurvive(state.customBlockState().literalObject(), level.serverWorld(), LocationUtils.toBlockPos(clickedPos))) {
                    return state;
                }
            }
        }
        return null;
    }

    public static class Factory implements BlockBehaviorFactory {

        @SuppressWarnings("unchecked")
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<HorizontalDirection> facing = (Property<HorizontalDirection>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.wall_attached.missing_facing");
            return new WallAttachedBlockBehavior(block, facing);
        }
    }
}
