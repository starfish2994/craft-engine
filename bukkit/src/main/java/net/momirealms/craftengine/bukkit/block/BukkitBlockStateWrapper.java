package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.util.Key;

public class BukkitBlockStateWrapper implements BlockStateWrapper {
    private final Object blockState;
    private final int registryId;

    public BukkitBlockStateWrapper(Object blockState, int registryId) {
        this.blockState = blockState;
        this.registryId = registryId;
    }

    @Override
    public Object literalObject() {
        return this.blockState;
    }

    @Override
    public int registryId() {
        return this.registryId;
    }

    @Override
    public String toString() {
        return this.blockState.toString();
    }

    @Override
    public Key ownerId() {
        return null;
    }
}
