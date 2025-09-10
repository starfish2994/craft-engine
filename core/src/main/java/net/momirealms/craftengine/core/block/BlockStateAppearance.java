package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;

import java.util.Optional;

public record BlockStateAppearance(int stateRegistryId, Optional<BlockEntityElementConfig<? extends BlockEntityElement>[]> blockEntityRenderer) {
    public static final BlockStateAppearance INVALID = new BlockStateAppearance(-1, Optional.empty());

    public boolean isInvalid() {
        return this.stateRegistryId < 0;
    }
}
