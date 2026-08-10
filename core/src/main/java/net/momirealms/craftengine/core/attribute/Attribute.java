package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.function.Predicate;

public final class Attribute {
    public final Key id;
    public final BaseValueSource baseValueSource;
    public final ValueConstraint constraint;
    public final Predicate<Entity> predicate;
    public final List<SyncTarget> syncTargets;

    public Attribute(Key id, BaseValueSource baseValueSource, ValueConstraint constraint, Predicate<Entity> predicate, List<SyncTarget> syncTargets) {
        this.id = id;
        this.baseValueSource = baseValueSource;
        this.constraint = constraint;
        this.predicate = predicate;
        this.syncTargets = syncTargets;
    }

    public Key id() {
        return this.id;
    }

    public double limit(double value) {
        return this.constraint.limit(value);
    }

    public double defaultValue(Entity entity) {
        return this.baseValueSource.resolve(entity);
    }

    public BaseValueSource baseValueSource() {
        return this.baseValueSource;
    }

    public ValueConstraint constraint() {
        return this.constraint;
    }

    public List<SyncTarget> syncTargets() {
        return this.syncTargets;
    }
}
