package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorType;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviors;
import net.momirealms.craftengine.core.util.Key;

public final class BukkitFurnitureBehaviors extends FurnitureBehaviors {
    private BukkitFurnitureBehaviors() {}

    public static final FurnitureBehaviorType<SimpleStorageFurnitureBehavior> SIMPLE_STORAGE_FURNITURE = register(Key.ce("simple_storage_furniture"), SimpleStorageFurnitureBehavior.FACTORY);
    public static final FurnitureBehaviorType<DisplayItemFurnitureBehavior> DISPLAY_ITEM_FURNITURE = register(Key.ce("display_item_furniture"), DisplayItemFurnitureBehavior.FACTORY);

    public static void init() {
    }
}
