package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;

public class NotTrackedHolder implements AttributeGetter {
    private final Entity entity;

    public NotTrackedHolder(Entity entity) {
        this.entity = entity;
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        // 如果没被跟踪，但是不一定是默认值
        return attribute.defaultValue(this.entity);
    }
}
