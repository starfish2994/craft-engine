package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.entity.Entity;

public interface BaseValueSource {

    double resolve(Entity entity);

    default double resolveCurrent(Entity entity) {
        return resolve(entity);
    }

    default BaseValueSource bind(Entity entity) {
        return this;
    }

    default boolean isDynamic() {
        return false;
    }

    default int updateInterval() {
        return isDynamic() ? 20 : 0;
    }
}
