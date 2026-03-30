package net.momirealms.craftengine.core.entity.furniture.behavior;

import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface FurnitureBehaviorFactory<T extends FurnitureBehaviorTemplate> {

    T create(CustomFurniture furniture, ConfigSection section);
}
