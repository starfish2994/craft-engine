package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.util.Key;

import java.util.Map;

public final class EntityAttributesSnapshot implements AttributeGetter {
    private final Map<Key, Double> snapshots;

    EntityAttributesSnapshot(Map<Key, Double> snapshots) {
        this.snapshots = snapshots;
    }

    public Map<Key, Double> snapshots() {
        return this.snapshots;
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        return this.snapshots.getOrDefault(attribute.id(), 0.0);
    }
}
