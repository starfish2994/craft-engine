package net.momirealms.craftengine.core.block.behavior;

public interface PlayerWillDestroyBlockBehavior {

    /**
     * <code>args[0]</code>: Level (level)<br>
     * <code>args[1]</code>: BlockPos (pos)<br>
     * <code>args[2]</code>: BlockState (state)<br>
     * <code>args[3]</code>: Player (player)<br>
     * <p>
     * Returns: BlockState
     */
    Object playerWillDestroy(Object thisBlock, Object[] args);
}
