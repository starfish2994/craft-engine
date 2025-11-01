package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public interface EntityBlockBehavior {

    @Nullable
    <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state);

    @Nullable
    BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state);

    default <T extends BlockEntity> BlockEntityTicker<T> createSyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }

    default <T extends BlockEntity> BlockEntityTicker<T> createAsyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }

    @SuppressWarnings("unchecked")
    static <E extends BlockEntity, T extends BlockEntity> BlockEntityTicker<E> createTickerHelper(BlockEntityTicker<? super T> ticker) {
        return (BlockEntityTicker<E>) ticker;
    }

    @SuppressWarnings("unchecked")
    static <E extends BlockEntity> BlockEntityType<E> blockEntityTypeHelper(BlockEntityType<?> type) {
        return (BlockEntityType<E>) type;
    }
}
