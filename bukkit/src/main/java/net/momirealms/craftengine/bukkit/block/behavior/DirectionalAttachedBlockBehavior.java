package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.BlockTags;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Tuple;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlocksProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.SupportTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.state.BlockBehaviourProxy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public final class DirectionalAttachedBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<DirectionalAttachedBlockBehavior> FACTORY = new Factory();
    public final Property<?> facingProperty;
    public final boolean isSixDirection;
    public final List<Object> tagsCanSurviveOn;
    public final Set<Object> blockStatesCanSurviveOn;
    public final Set<String> customBlocksCansSurviveOn;
    public final boolean blacklistMode;

    private DirectionalAttachedBlockBehavior(BlockDefinition blockDefinition,
                                             Property<?> facingProperty,
                                             boolean isSixDirection,
                                             boolean blacklist,
                                             List<Object> tagsCanSurviveOn,
                                             Set<Object> blockStatesCanSurviveOn,
                                             Set<String> customBlocksCansSurviveOn) {
        super(blockDefinition);
        this.facingProperty = facingProperty;
        this.isSixDirection = isSixDirection;
        this.tagsCanSurviveOn = tagsCanSurviveOn;
        this.blockStatesCanSurviveOn = blockStatesCanSurviveOn;
        this.customBlocksCansSurviveOn = customBlocksCansSurviveOn;
        this.blacklistMode = blacklist;
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return args[0];
        DirectionalAttachedBlockBehavior behavior = state.behavior().getAs(DirectionalAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return state;
        boolean flag;
        if (isSixDirection) {
            Direction direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]).opposite();
            flag = direction == state.get(behavior.facingProperty);
        } else {
            HorizontalDirection direction = DirectionUtils.fromNMSDirection(args[updateShape$direction]).opposite().toHorizontalDirection();
            flag = direction == state.get(behavior.facingProperty);
        }
        return flag && !BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.canSurvive(args[0], args[updateShape$level], args[updateShape$blockPos]) ? BlocksProxy.AIR$defaultState : args[0];
    }

    @Override
    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        ImmutableBlockState state = BlockStateUtils.getOptionalCustomBlockState(args[0]).orElse(null);
        if (state == null) return false;
        DirectionalAttachedBlockBehavior behavior = state.behavior().getAs(DirectionalAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return false;
        Direction direction;
        if (isSixDirection) {
            direction = (Direction) state.get(behavior.facingProperty);
        } else {
            direction = ((HorizontalDirection) state.get(behavior.facingProperty)).toDirection();
        }
        BlockPos blockPos = LocationUtils.fromBlockPos(args[2]).relative(direction.opposite());
        Object nmsPos = LocationUtils.toBlockPos(blockPos);
        Object nmsState = BlockGetterProxy.INSTANCE.getBlockState(args[1], nmsPos);
        return BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.isFaceSturdy(nmsState, args[1], nmsPos, DirectionUtils.toNMSDirection(direction), SupportTypeProxy.FULL)
                && mayPlaceOn(nmsState);
    }

    private boolean mayPlaceOn(Object state) {
        for (Object tag : this.tagsCanSurviveOn) {
            if (BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.is$1(state, tag)) {
                return !this.blacklistMode;
            }
        }
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(state);
        if (optionalCustomState.isEmpty()) {
            if (!this.blockStatesCanSurviveOn.isEmpty() && this.blockStatesCanSurviveOn.contains(state)) {
                return !this.blacklistMode;
            }
        } else {
            ImmutableBlockState belowCustomState = optionalCustomState.get();
            if (this.customBlocksCansSurviveOn.contains(belowCustomState.owner().value().id().toString())) {
                return !this.blacklistMode;
            }
            if (this.customBlocksCansSurviveOn.contains(belowCustomState.toString())) {
                return !this.blacklistMode;
            }
        }
        return this.blacklistMode;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        DirectionalAttachedBlockBehavior behavior = state.behavior().getAs(DirectionalAttachedBlockBehavior.class).orElse(null);
        if (behavior == null) return null;
        World level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        for (Direction direction : context.getNearestLookingDirections()) {
            if (isSixDirection) {
                state = state.with((Property<Direction>) behavior.facingProperty, direction.opposite());
                if (BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.canSurvive(state.customBlockState().literalObject(), level.serverWorld(), LocationUtils.toBlockPos(clickedPos))) {
                    return state;
                }
            } else if (direction.axis().isHorizontal()) {
                state = state.with((Property<HorizontalDirection>) behavior.facingProperty, direction.opposite().toHorizontalDirection());
                if (BlockBehaviourProxy.BlockStateBaseProxy.INSTANCE.canSurvive(state.customBlockState().literalObject(), level.serverWorld(), LocationUtils.toBlockPos(clickedPos))) {
                    return state;
                }
            }
        }
        return null;
    }

    private static class Factory implements BlockBehaviorFactory<DirectionalAttachedBlockBehavior> {

        @Override
        public DirectionalAttachedBlockBehavior create(BlockDefinition block, ConfigSection section) {
            Property<?> facing = block.getProperty("facing");
            if (facing == null) {
                throw new KnownResourceException("resource.block.behavior.missing_property", section.path(), "facing");
            }
            boolean isHorizontalDirection = facing.valueClass() == HorizontalDirection.class;
            boolean isDirection = facing.valueClass() == Direction.class;
            if (!(isHorizontalDirection || isDirection)) {
                throw new KnownResourceException("resource.block.behavior.property_type_mismatch", section.path(), facing.valueClass().getSimpleName(), Direction.class.getSimpleName() + "/" + HorizontalDirection.class.getSimpleName());
            }
            Tuple<List<Object>, Set<Object>, Set<String>> tuple = readTagsAndState(section);
            return new DirectionalAttachedBlockBehavior(
                    block,
                    facing,
                    isDirection,
                    section.getBoolean("blacklist", true),
                    tuple.left(),
                    tuple.mid(),
                    tuple.right()
            );
        }
    }

    // todo 重构
    public static Tuple<List<Object>, Set<Object>, Set<String>> readTagsAndState(ConfigSection section) {
        List<Object> mcTags = section.getList(new String[] {"attached_block_tags", "attached-block-tags"}, v -> BlockTags.getOrCreate(v.getAsIdentifier()));
        Set<Object> mcBlocks = new HashSet<>();
        Set<String> customBlocks = new HashSet<>();
        for (String blockStateStr : section.getStringList(new String[] {"attached_blocks", "attached-blocks"})) {
            int index = blockStateStr.indexOf('[');
            Key blockType = index != -1 ? Key.from(blockStateStr.substring(0, index)) : Key.from(blockStateStr);
            Material material = Registry.MATERIAL.get(new NamespacedKey(blockType.namespace(), blockType.value()));
            if (material != null) {
                if (index == -1) {
                    // vanilla
                    mcBlocks.addAll(BlockStateUtils.getPossibleBlockStates(blockType));
                } else {
                    mcBlocks.add(BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData(blockStateStr)));
                }
            } else {
                // custom maybe
                customBlocks.add(blockStateStr);
            }
        }
        return new Tuple<>(mcTags, mcBlocks, customBlocks);
    }
}
