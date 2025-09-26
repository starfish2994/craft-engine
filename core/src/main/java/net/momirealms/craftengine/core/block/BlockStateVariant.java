package net.momirealms.craftengine.core.block;

public class BlockStateVariant {
    private final String appearance;
    private final BlockSettings settings;
    private final BlockStateWrapper blockState;

    public BlockStateVariant(String appearance, BlockSettings settings, BlockStateWrapper blockState) {
        this.appearance = appearance;
        this.settings = settings;
        this.blockState = blockState;
    }

    public String appearance() {
        return appearance;
    }

    public BlockSettings settings() {
        return settings;
    }

    public BlockStateWrapper blockState() {
        return blockState;
    }
}
