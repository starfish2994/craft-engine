package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.RandomTickBlock;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.random.RandomUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.event.CraftEventFactoryProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlocksProxy;

import java.util.Optional;

public final class VerticalCropBlockBehavior extends BukkitBlockBehavior implements RandomTickBlock {
    public static final BlockBehaviorFactory<VerticalCropBlockBehavior> FACTORY = new Factory();
    public final int maxHeight;
    public final IntegerProperty ageProperty;
    public final float growSpeed;
    public final boolean direction;

    private VerticalCropBlockBehavior(BlockDefinition blockDefinition,
                                      IntegerProperty ageProperty,
                                      int maxHeight,
                                      float growSpeed,
                                      boolean direction) {
        super(blockDefinition);
        this.maxHeight = maxHeight;
        this.ageProperty = ageProperty;
        this.growSpeed = growSpeed;
        this.direction = direction;
    }

    @Override
    public boolean canRandomlyTick(ImmutableBlockState state) {
        return true;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args) {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        Optional<ImmutableBlockState> optionalCurrentState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCurrentState.isEmpty()) {
            return;
        }
        ImmutableBlockState currentState = optionalCurrentState.get();
        // above block is empty
        if (BlockGetterProxy.INSTANCE.getBlockState(level, (this.direction ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos))) == BlocksProxy.AIR$defaultState) {
            int currentHeight = 1;
            BlockPos currentPos = LocationUtils.fromBlockPos(blockPos);
            for (; ; ) {
                Object nextPos = LocationUtils.toBlockPos(currentPos.x(), this.direction ? currentPos.y() - currentHeight : currentPos.y() + currentHeight, currentPos.z());
                Object nextState = BlockGetterProxy.INSTANCE.getBlockState(level, nextPos);
                Optional<ImmutableBlockState> optionalBelowCustomState = BlockStateUtils.getOptionalCustomBlockState(nextState);
                if (optionalBelowCustomState.isPresent() && optionalBelowCustomState.get().owner().value() == super.blockDefinition) {
                    currentHeight++;
                } else {
                    break;
                }
            }
            if (currentHeight < this.maxHeight) {
                int age = currentState.get(ageProperty);
                if (age >= this.ageProperty.max || RandomUtils.generateRandomFloat(0, 1) < this.growSpeed) {
                    Object nextPos = this.direction ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos);
                    boolean success;
                    if (VersionHelper.isOrAbove1_21_5()) {
                        success = CraftEventFactoryProxy.INSTANCE.handleBlockGrowEvent(level, nextPos, super.blockDefinition.defaultState().customBlockState().minecraftState(), UpdateFlags.UPDATE_ALL);
                    } else {
                        success = CraftEventFactoryProxy.INSTANCE.handleBlockGrowEvent(level, nextPos, super.blockDefinition.defaultState().customBlockState().minecraftState());
                    }
                    if (success) {
                        LevelWriterProxy.INSTANCE.setBlock(level, blockPos, currentState.with(this.ageProperty, this.ageProperty.min).customBlockState().minecraftState(), UpdateFlags.UPDATE_NONE);
                    }
                } else if (RandomUtils.generateRandomFloat(0, 1) < this.growSpeed) {
                    LevelWriterProxy.INSTANCE.setBlock(level, blockPos, currentState.with(this.ageProperty, age + 1).customBlockState().minecraftState(), UpdateFlags.UPDATE_NONE);
                }
            }
        }
    }

    private static class Factory implements BlockBehaviorFactory<VerticalCropBlockBehavior> {
        private static final String[] MAX_HEIGHT = new String[] {"max_height", "max-height"};
        private static final String[] GROW_SPEED = new String[] {"grow_speed", "grow-speed"};

        @Override
        public VerticalCropBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new VerticalCropBlockBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(section.path(), block, "age", Integer.class),
                    section.getInt(MAX_HEIGHT, 3),
                    section.getFloat(GROW_SPEED, 1f),
                    section.getString("direction", "up").equalsIgnoreCase("up")
            );
        }
    }
}
