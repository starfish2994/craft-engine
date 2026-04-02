package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface EntityBlockBehavior {

    BlockEntityController createController(BlockEntity blockEntity, int controllerId);
}
