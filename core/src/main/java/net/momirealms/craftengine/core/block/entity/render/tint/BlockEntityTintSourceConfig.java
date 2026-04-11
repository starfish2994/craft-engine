package net.momirealms.craftengine.core.block.entity.render.tint;

import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;

public interface BlockEntityTintSourceConfig<T extends BlockEntityTintSource> {

    T create(World world, BlockPos pos);
}
