package net.momirealms.craftengine.core.block.entity.render.element;

import net.momirealms.craftengine.core.world.BlockPos;

public interface BlockEntityElementConfig<E extends BlockEntityElement> {

    E create(BlockPos pos);
}
