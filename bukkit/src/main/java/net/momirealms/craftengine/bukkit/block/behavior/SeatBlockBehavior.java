package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.SeatBlockEntity;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.joml.Vector3f;

import java.util.Map;

public class SeatBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Vector3f offset;
    private final float yaw;
    private final boolean limitPlayerRotation;

    public SeatBlockBehavior(CustomBlock customBlock, Vector3f offset, float yaw, boolean limitPlayerRotation) {
        super(customBlock);
        this.offset = offset;
        this.yaw = yaw;
        this.limitPlayerRotation = limitPlayerRotation;
    }

    @Override
    public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) {
        BukkitServerPlayer player = (BukkitServerPlayer) context.getPlayer();
        if (player == null || player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        player.swingHand(context.getHand());
        CEWorld world = context.getLevel().storageWorld();
        BlockEntity blockEntity = world.getBlockEntityAtIfLoaded(context.getClickedPos());
        if (!(blockEntity instanceof SeatBlockEntity seatBlockEntity) || !seatBlockEntity.seatEntities().isEmpty()) {
            return InteractionResult.PASS;
        }
        seatBlockEntity.spawnSeatEntityForPlayer(player.platformPlayer(), this.offset, this.yaw, this.limitPlayerRotation);
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return (BlockEntityType<T>) BukkitBlockEntityTypes.SEAT;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new SeatBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return EntityBlockBehavior.createTickerHelper(SeatBlockEntity::tick);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Vector3f offset = ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("offset", "0,0,0"), "offset");
            float yaw = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw");
            boolean limitPlayerRotation = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("limit-player-rotation", true), "limit-player-rotation");
            return new SeatBlockBehavior(block, offset, yaw, limitPlayerRotation);
        }
    }
}
