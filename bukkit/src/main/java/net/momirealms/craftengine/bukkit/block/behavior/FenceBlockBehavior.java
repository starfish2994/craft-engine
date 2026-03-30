package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.antigrieflib.Flag;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.IsPathFindableBlockBehavior;
import net.momirealms.craftengine.core.block.properties.BooleanProperty;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.proxy.minecraft.core.DirectionProxy;
import net.momirealms.craftengine.proxy.minecraft.tags.BlockTagsProxy;
import net.momirealms.craftengine.proxy.minecraft.world.InteractionResultProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.LeadItemProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlocksProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.FenceGateBlockProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.LeavesBlockProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.SupportTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidStateProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidsProxy;
import org.bukkit.Location;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class FenceBlockBehavior extends BukkitBlockBehavior implements IsPathFindableBlockBehavior {
    public static final BlockBehaviorFactory<FenceBlockBehavior> FACTORY = new Factory();
    public static final Object InteractionResult$SUCCESS_SERVER = VersionHelper.isOrAbove1_21_2() ? InteractionResultProxy.INSTANCE.getSuccessServer() : InteractionResultProxy.INSTANCE.getSuccess();
    private static final Key DEFAULT_CONNECTABLE = Key.of("minecraft:wooden_fences");
    public final Property<Boolean> northProperty;
    public final Property<Boolean> eastProperty;
    public final Property<Boolean> southProperty;
    public final Property<Boolean> westProperty;
    public final Object connectableBlockTag;
    public final boolean canLeash;

    private FenceBlockBehavior(BlockDefinition blockDefinition,
                               Property<Boolean> northProperty,
                               Property<Boolean> eastProperty,
                               Property<Boolean> southProperty,
                               Property<Boolean> westProperty,
                               Object connectableBlockTag,
                               boolean canLeash) {
        super(blockDefinition);
        this.northProperty = northProperty;
        this.eastProperty = eastProperty;
        this.southProperty = southProperty;
        this.westProperty = westProperty;
        this.connectableBlockTag = connectableBlockTag;
        this.canLeash = canLeash;
    }

    @Override
    public boolean isPathFindable(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        return false;
    }

    public boolean connectsTo(BlockStateWrapper state, boolean isSideSolid, HorizontalDirection direction) {
        boolean isSameFence = this.isSameFence(state);
        boolean flag = FenceGateBlockProxy.CLASS.isInstance(BlockStateUtils.getBlockOwner(state.literalObject()))
                ? FenceGateBlockProxy.INSTANCE.connectsToDirection(state.literalObject(), DirectionUtils.toNMSDirection(direction.toDirection()))
                : FenceGateBlockBehavior.connectsToDirection(state, direction);
        return !isExceptionForConnection(state) && isSideSolid || isSameFence || flag;
    }

    public static boolean isExceptionForConnection(BlockStateWrapper state) {
        Object blockState = state.literalObject();
        return LeavesBlockProxy.CLASS.isInstance(BlockStateUtils.getBlockOwner(blockState))
                || BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$0(blockState, BlocksProxy.BARRIER)
                || BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$0(blockState, BlocksProxy.CARVED_PUMPKIN)
                || BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$0(blockState, BlocksProxy.JACK_O_LANTERN)
                || BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$0(blockState, BlocksProxy.MELON)
                || BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$0(blockState, BlocksProxy.PUMPKIN)
                || BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(blockState, BlockTagsProxy.SHULKER_BOXES);
    }

    private boolean isSameFence(BlockStateWrapper state) {
        Object blockState = state.literalObject();
        return BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(blockState, BlockTagsProxy.FENCES)
                && BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(blockState, this.connectableBlockTag)
                == BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(this.blockDefinition.defaultState().customBlockState().literalObject(), this.connectableBlockTag);
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        if (!this.canLeash) return InteractionResult.PASS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        BlockPos pos = context.getClickedPos();
        World world = context.getLevel();
        Location location = new Location((org.bukkit.World) world.platformWorld(), pos.x, pos.y, pos.z);
        if (!BukkitCraftEngine.instance().antiGriefProvider().test((org.bukkit.entity.Player) player.platformPlayer(), Flag.INTERACT, location)) {
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        Object interactionResult = LeadItemProxy.INSTANCE.bindPlayerMobs(player.serverPlayer(), context.getLevel().serverWorld(), LocationUtils.toBlockPos(pos));
        if (interactionResult == InteractionResult$SUCCESS_SERVER) {
            player.swingHand(InteractionHand.MAIN_HAND);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        World level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        Object fluidState = BlockGetterProxy.INSTANCE.getFluidState(level.serverWorld(), LocationUtils.toBlockPos(clickedPos));
        BlockPos blockPos = clickedPos.north();
        BlockPos blockPos1 = clickedPos.east();
        BlockPos blockPos2 = clickedPos.south();
        BlockPos blockPos3 = clickedPos.west();
        BlockStateWrapper blockState = level.getBlock(blockPos).blockState();
        BlockStateWrapper blockState1 = level.getBlock(blockPos1).blockState();
        BlockStateWrapper blockState2 = level.getBlock(blockPos2).blockState();
        BlockStateWrapper blockState3 = level.getBlock(blockPos3).blockState();
        BooleanProperty waterlogged = (BooleanProperty) state.owner().value().getProperty("waterlogged");
        if (waterlogged != null) {
            state = state.with(waterlogged, FluidStateProxy.INSTANCE.getType(fluidState) == FluidsProxy.WATER);
        }
        return state
                .with(this.northProperty, this.connectsTo(blockState, BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(blockState.literalObject(), level.serverWorld(), LocationUtils.toBlockPos(blockPos), DirectionProxy.SOUTH, SupportTypeProxy.FULL), HorizontalDirection.SOUTH))
                .with(this.eastProperty, this.connectsTo(blockState1, BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(blockState1.literalObject(), level.serverWorld(), LocationUtils.toBlockPos(blockPos1), DirectionProxy.WEST, SupportTypeProxy.FULL), HorizontalDirection.WEST))
                .with(this.southProperty, this.connectsTo(blockState2, BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(blockState2.literalObject(), level.serverWorld(), LocationUtils.toBlockPos(blockPos2), DirectionProxy.NORTH, SupportTypeProxy.FULL), HorizontalDirection.NORTH))
                .with(this.westProperty, this.connectsTo(blockState3, BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(blockState3.literalObject(), level.serverWorld(), LocationUtils.toBlockPos(blockPos3), DirectionProxy.EAST, SupportTypeProxy.FULL), HorizontalDirection.EAST));
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Optional<ImmutableBlockState> optionalState = BlockStateUtils.getOptionalCustomBlockState(args[0]);
        BooleanProperty waterlogged = (BooleanProperty) optionalState
                .map(ImmutableBlockState::owner)
                .map(Holder::value)
                .map(block -> block.getProperty("waterlogged"))
                .orElse(null);
        if (waterlogged != null) {
            LevelUtils.scheduleFluidTick(args[updateShape$level], args[updateShape$blockPos], FluidsProxy.WATER, 5);
        }
        if (DirectionUtils.fromNMSDirection(args[updateShape$direction]).axis().isHorizontal() && optionalState.isPresent()) {
            Direction direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]);
            ImmutableBlockState state = optionalState.get();
            if (state.owner() != null) {
                BooleanProperty booleanProperty = (BooleanProperty) state.owner().value().getProperty(direction.name().toLowerCase(Locale.ROOT));
                if (booleanProperty != null) {
                    BlockStateWrapper wrapper = BlockStateUtils.toBlockStateWrapper(args[updateShape$neighborState]);
                    return state.with(booleanProperty, this.connectsTo(wrapper, BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(wrapper.literalObject(), args[updateShape$level], args[5], DirectionUtils.toNMSDirection(direction.opposite()), SupportTypeProxy.FULL), direction.opposite().toHorizontalDirection())).customBlockState().literalObject();
                }
            }
        }
        return superMethod.call();
    }

    private static class Factory implements BlockBehaviorFactory<FenceBlockBehavior> {
        private static final String[] CAN_LEASH = new String[]{"can_leash", "can-leash"};
        private static final String[] CONNECTABLE_BLOCK_TAG = new String[]{"connectable_block_tag", "connectable-block-tag"};

        @Override
        public FenceBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new FenceBlockBehavior(
                    block,
                    BlockBehaviorFactory.getProperty(section.path(), block, "north", Boolean.class),
                    BlockBehaviorFactory.getProperty(section.path(), block, "east", Boolean.class),
                    BlockBehaviorFactory.getProperty(section.path(), block, "south", Boolean.class),
                    BlockBehaviorFactory.getProperty(section.path(), block, "west", Boolean.class),
                    BlockTags.getOrCreate(section.getIdentifier(CONNECTABLE_BLOCK_TAG, DEFAULT_CONNECTABLE)),
                    section.getBoolean(CAN_LEASH)
            );
        }
    }
}
