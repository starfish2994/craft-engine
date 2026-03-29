package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.EmptyFurniture;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.tick.FurnitureTicker;

public final class EmptyFurnitureBehavior extends FurnitureBehavior {
    public static final EmptyFurnitureBehavior INSTANCE = new EmptyFurnitureBehavior(EmptyFurniture.INSTANCE);

    public EmptyFurnitureBehavior(CustomFurniture furniture) {
        super(furniture);
    }

    @Override
    public Handler createHandler(Furniture furniture) {
        return new Handler(furniture) {}; // Empty Handler.
    }

}
