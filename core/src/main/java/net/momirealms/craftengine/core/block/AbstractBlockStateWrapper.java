package net.momirealms.craftengine.core.block;

public abstract class AbstractBlockStateWrapper implements BlockStateWrapper {
    protected final Object blockState;
    protected final int registryId;

    protected AbstractBlockStateWrapper(Object blockState, int registryId) {
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
}
