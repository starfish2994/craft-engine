package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.attribute.base.*;
import net.momirealms.craftengine.core.attribute.format.*;
import net.momirealms.craftengine.core.attribute.sync.*;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public final class Attribute {
    public final Key id;
    public final BaseValueSource baseValueSource;
    public final ValueConstraint constraint;
    @Nullable
    public final Set<Key> applicableEntityTypes;
    public final List<SyncTarget> syncTargets;
    @Nullable
    public final ValueFormatter formatter;

    public Attribute(Key id, BaseValueSource baseValueSource, ValueConstraint constraint, @Nullable Set<Key> applicableEntityTypes, List<SyncTarget> syncTargets, @Nullable ValueFormatter formatter) {
        this.id = id;
        this.baseValueSource = baseValueSource;
        this.constraint = constraint;
        this.applicableEntityTypes = applicableEntityTypes;
        this.syncTargets = syncTargets;
        this.formatter = formatter;
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

    public boolean appliesTo(Entity entity) {
        return this.applicableEntityTypes == null || this.applicableEntityTypes.contains(entity.type());
    }

    public List<SyncTarget> syncTargets() {
        return this.syncTargets;
    }

    @Nullable
    public ValueFormatter formatter() {
        return this.formatter;
    }

    public String format(double value) {
        return this.formatter == null ? String.valueOf(value) : this.formatter.format(value);
    }
}
