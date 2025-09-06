package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.block.entity.render.BlockEntityRendererConfig;

import java.util.Optional;

public record BlockStateAppearance(int stateRegistryId, Optional<BlockEntityRendererConfig> blockEntityRenderer) {
    public static final BlockStateAppearance INVALID = new BlockStateAppearance(-1, Optional.empty());

    public boolean isInvalid() {
        return this.stateRegistryId < 0;
    }
}
