package net.momirealms.craftengine.core.pack.allocator;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BlockStateAllocator {
    private final List<BlockStateCandidate> blockStates = new ArrayList<>();
    private int pointer = 0;
    private int max = -1;

    public void addCandidate(BlockStateCandidate state) {
        this.blockStates.add(state);
        this.max = this.blockStates.size() - 1;
    }

    @Nullable
    public BlockStateCandidate findNext() {
        while (this.pointer < this.max) {
            final BlockStateCandidate state = this.blockStates.get(this.pointer);
            if (!state.isUsed()) {
                return state;
            }
            this.pointer++;
        }
        return null;
    }

    public void processPendingAllocations() {

    }
}
