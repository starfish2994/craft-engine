package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.item.Item;

public class BlockEntityRendererConfig {
    private final float yRot;
    private final float xRot;
    private final ItemDisplayContext displayContext;
    private final Item<?> item;
    private final float scale;

    public BlockEntityRendererConfig(ItemDisplayContext displayContext,
                                     float yRot,
                                     float xRot,
                                     Item<?> item,
                                     float scale) {
        this.displayContext = displayContext;
        this.yRot = yRot;
        this.xRot = xRot;
        this.item = item;
        this.scale = scale;
    }

    public ItemDisplayContext displayContext() {
        return displayContext;
    }

    public Item<?> item() {
        return item;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }

    public float scale() {
        return scale;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ItemDisplayContext displayContext = ItemDisplayContext.NONE;
        private Item<?> item;
        private float xRot;
        private float yRot;
        private float scale = 1f;

        public Builder() {
        }

        public Builder displayContext(ItemDisplayContext displayContext) {
            this.displayContext = displayContext;
            return this;
        }

        public Builder item(Item<?> item) {
            this.item = item;
            return this;
        }

        public Builder xRot(float xRot) {
            this.xRot = xRot;
            return this;
        }

        public Builder yRot(float yRot) {
            this.yRot = yRot;
            return this;
        }

        public Builder scale(float scale) {
            this.scale = scale;
            return this;
        }

        public BlockEntityRendererConfig build() {
            return new BlockEntityRendererConfig(this.displayContext, this.yRot, this.xRot, this.item, this.scale);
        }
    }
}
