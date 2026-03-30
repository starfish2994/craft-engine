package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.BlockTags;
import net.momirealms.craftengine.bukkit.util.LevelUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.proxy.minecraft.core.DirectionProxy;
import net.momirealms.craftengine.proxy.minecraft.core.MutableBlockPosProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlockProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.LeavesBlockProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.StateHolderProxy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.block.LeavesDecayEvent;

import java.util.Optional;
import java.util.concurrent.Callable;

public final class LeavesBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<LeavesBlockBehavior> FACTORY = new Factory();
    public static final Object LOG_TAG = BlockTags.getOrCreate(Key.of("minecraft", "logs"));
    public final int maxDistance;
    public final Property<Integer> distanceProperty;
    public final Property<Boolean> persistentProperty;

    private LeavesBlockBehavior(BlockDefinition block,
                                Property<Integer> distanceProperty,
                                Property<Boolean> persistentProperty) {
        super(block);
        this.maxDistance = distanceProperty.possibleValues().getLast();
        this.distanceProperty = distanceProperty;
        this.persistentProperty = persistentProperty;
    }

    public int getDistance(ImmutableBlockState state) {
        return state.get(this.distanceProperty);
    }

    public boolean isPersistent(ImmutableBlockState state) {
        return state.get(this.persistentProperty);
    }

    public boolean isWaterLogged(ImmutableBlockState state) {
        if (this.waterloggedProperty == null) return false;
        return state.get(this.waterloggedProperty);
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object world = args[updateShape$level];
        Object blockPos = args[updateShape$blockPos];
        Object neighborState = args[updateShape$neighborState];
        Object blockState = args[0];
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isPresent()) {
            Optional<LeavesBlockBehavior> optionalBehavior = optionalCustomState.get().behavior().getAs(LeavesBlockBehavior.class);
            if (optionalBehavior.isPresent()) {
                LeavesBlockBehavior behavior = optionalBehavior.get();
                int distance = behavior.getDistanceAt(neighborState) + 1;
                if (distance != 1 || behavior.getDistance(optionalCustomState.get()) != distance) {
                    LevelUtils.scheduleBlockTick(world, blockPos, thisBlock, 1);
                }
            }
        }
        return blockState;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isPresent()) {
            ImmutableBlockState customState = optionalCustomState.get();
            Optional<LeavesBlockBehavior> optionalBehavior = customState.behavior().getAs(LeavesBlockBehavior.class);
            if (optionalBehavior.isPresent()) {
                LeavesBlockBehavior behavior = optionalBehavior.get();
                ImmutableBlockState newState = behavior.updateDistance(customState, level, blockPos);
                if (newState != customState) {
                    if (blockState == newState.customBlockState().literalObject()) {
                        BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.updateNeighbourShapes(blockState, level, blockPos, UpdateFlags.UPDATE_ALL, 512);
                    } else {
                        LevelWriterProxy.INSTANCE.setBlock(level, blockPos, newState.customBlockState().literalObject(), UpdateFlags.UPDATE_ALL);
                    }
                }
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        BlockStateUtils.getOptionalCustomBlockState(blockState).ifPresent(customState -> {
            // 可能是另一种树叶
            Optional<LeavesBlockBehavior> optionalBehavior = customState.behavior().getAs(LeavesBlockBehavior.class);
            if (optionalBehavior.isPresent()) {
                LeavesBlockBehavior behavior = optionalBehavior.get();
                if (behavior.isDecaying(customState)) {
                    World bukkitWorld = LevelProxy.INSTANCE.getWorld(level);
                    BlockPos pos = LocationUtils.fromBlockPos(blockPos);
                    // call bukkit event
                    LeavesDecayEvent event = new LeavesDecayEvent(bukkitWorld.getBlockAt(pos.x(), pos.y(), pos.z()));
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        return;
                    }
                    LevelProxy.INSTANCE.removeBlock(level, blockPos, false);
                    BlockProxy.INSTANCE.dropResources(blockState, level, blockPos);
                }
            }
        });
    }

    private boolean isDecaying(ImmutableBlockState blockState) {
        return !isPersistent(blockState) && getDistance(blockState) == this.maxDistance;
    }

    private ImmutableBlockState updateDistance(ImmutableBlockState state, Object world, Object blockPos) throws ReflectiveOperationException {
        int i = this.maxDistance;
        Object mutablePos = MutableBlockPosProxy.INSTANCE.newInstance();
        int j = Direction.values().length;
        for (int k = 0; k < j; ++k) {
            Object direction = DirectionProxy.VALUES[k];
            MutableBlockPosProxy.INSTANCE.setWithOffset(mutablePos, blockPos, direction);
            Object blockState = BlockGetterProxy.INSTANCE.getBlockState(world, mutablePos);
            i = Math.min(i, getDistanceAt(blockState) + 1);
            if (i == 1) {
                break;
            }
        }
        return state.with(this.distanceProperty, i);
    }

    private int getDistanceAt(Object blockState) {
        boolean isLog = BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(blockState, LOG_TAG);
        if (isLog) return 0;
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isEmpty()) {
            boolean hasDistanceProperty = StateHolderProxy.INSTANCE.hasProperty(blockState, LeavesBlockProxy.DISTANCE);
            if (!hasDistanceProperty) return this.maxDistance;
            return StateHolderProxy.INSTANCE.getValue(blockState, LeavesBlockProxy.DISTANCE);
        } else {
            ImmutableBlockState anotherBlockState = optionalCustomState.get();
            Optional<LeavesBlockBehavior> optionalAnotherBehavior = anotherBlockState.behavior().getAs(LeavesBlockBehavior.class);
            return optionalAnotherBehavior.map(leavesBlockBehavior -> leavesBlockBehavior.getDistance(anotherBlockState)).orElse(this.maxDistance);
        }
    }

    private static class Factory implements BlockBehaviorFactory<LeavesBlockBehavior> {

        @Override
        public LeavesBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new LeavesBlockBehavior(
                    block,
                    BlockBehaviorFactory.getProperty(section.path(), block, "distance", Integer.class),
                    BlockBehaviorFactory.getProperty(section.path(), block, "persistent", Boolean.class)
            );
        }
    }
}
