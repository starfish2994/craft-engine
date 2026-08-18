package net.momirealms.craftengine.core.attribute.base;

import net.momirealms.craftengine.core.entity.LivingEntity;

public interface BaseValueSource {

    double resolve(LivingEntity entity);

    default BaseValueSource bind(LivingEntity entity) {
        return this;
    }

    default boolean isDynamic() {
        return false;
    }

    default int updateInterval() {
        return isDynamic() ? 1 : 0;
    }
}
