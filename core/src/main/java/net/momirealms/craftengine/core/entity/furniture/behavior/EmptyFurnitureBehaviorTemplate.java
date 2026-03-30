package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.EmptyFurniture;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import org.jetbrains.annotations.NotNull;

public final class EmptyFurnitureBehaviorTemplate extends FurnitureBehaviorTemplate {
    public static final EmptyFurnitureBehaviorTemplate INSTANCE = new EmptyFurnitureBehaviorTemplate(EmptyFurniture.INSTANCE);

    public EmptyFurnitureBehaviorTemplate(CustomFurniture furniture) {
        super(furniture);
    }

    @Override
    public Controller createController(Furniture furniture) {
        return new EmptyFurnitureController(furniture);
    }

    public static class EmptyFurnitureController extends Controller {

        public EmptyFurnitureController(@NotNull Furniture furniture) {
            super(furniture);
        }
    }
}
