package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;

public interface BaseValueSource {

    double resolve(Entity entity);

    default boolean isDynamic() {
        return false;
    }
}
