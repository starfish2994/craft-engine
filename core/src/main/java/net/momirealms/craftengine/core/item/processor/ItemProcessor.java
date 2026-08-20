package net.momirealms.craftengine.core.item.processor;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.sparrow.nbt.CompoundTag;

// TODO 接口方法设计地不好，未来重构
public interface ItemProcessor {

    Item apply(Item item, ItemBuildContext context);

    default void prepareNetworkItem(Item item, ItemBuildContext context, CompoundTag networkData) {
    }
}
