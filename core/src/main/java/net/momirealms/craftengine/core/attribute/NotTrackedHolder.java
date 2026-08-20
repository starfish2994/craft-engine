package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;

public class NotTrackedHolder implements AttributeGetter {
    private final Entity entity;

    public NotTrackedHolder(Entity entity) {
        this.entity = entity;
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        if (attribute.derived() != null) {
            return attribute.derive(this::getAttributeValue);
        }
        return attribute.currentValue(this.entity);
    }
}
