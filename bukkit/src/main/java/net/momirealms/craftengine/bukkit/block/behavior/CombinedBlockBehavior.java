package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.behavior.*;

public interface CombinedBlockBehavior extends PrioritizedFallOnHandler, PathFindingBlock, PlayerWillDestroyBlockBehavior,
        BukkitFallableBlock, BonemealableBlock, WorldlyContainerHolder, BukkitSimpleWaterloggedBlock, EntityBlock {
}