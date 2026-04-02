package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.culling.Cullable;
import net.momirealms.craftengine.core.entity.culling.CullingData;
import org.jetbrains.annotations.Nullable;

public final class ConstantBlockEntityRenderer extends BlockEntityRenderer implements Cullable {
    public final CullingData cullingData;

    public ConstantBlockEntityRenderer(BlockEntityElement[] constantElements, @Nullable CullingData cullingData) {
        super(constantElements);
        this.cullingData = cullingData;
    }

    @Override
    public CullingData cullingData() {
        return this.cullingData;
    }
}
