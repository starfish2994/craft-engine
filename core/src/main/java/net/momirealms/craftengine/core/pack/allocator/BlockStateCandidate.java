package net.momirealms.craftengine.core.pack.allocator;

import net.momirealms.craftengine.core.block.BlockStateWrapper;

public class BlockStateCandidate {
    private final BlockStateWrapper blockState;
    private boolean used = false;

    public BlockStateCandidate(BlockStateWrapper blockState) {
        this.blockState = blockState;
    }

    public void setUsed() {
        this.used = true;
    }

    public boolean isUsed() {
        return used;
    }

    public BlockStateWrapper blockState() {
        return blockState;
    }
}
