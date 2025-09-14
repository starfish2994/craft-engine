package net.momirealms.craftengine.core.block.entity.tick;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;

public interface BlockEntityTicker<T extends BlockEntity> {

    void tick(CEWorld world, BlockPos pos, ImmutableBlockState state, T blockEntity);
}
