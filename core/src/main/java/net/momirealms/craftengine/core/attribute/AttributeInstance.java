package net.momirealms.craftengine.core.attribute;
import net.momirealms.craftengine.core.attribute.modifier.*;
import net.momirealms.craftengine.core.attribute.sync.*;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeInstance {
    private final Attribute attribute;
    private final Entity entity;
    private final Map<Key, Map<Key, AttributeModifier>> byOperation = new HashMap<>();
    private final Map<Key, AttributeModifier> byId = new Object2ObjectArrayMap<>();
    private final Context context;
    @Nullable
    private Map<Key, TrackedModifier> trackedModifiers;
    private double cachedValue;
    private boolean dirty = true;
    private double lastBase;
    // 上次写回原版时的 (value, base)，用于变化检测
    private double lastSyncValue = Double.NaN;
    private double lastSyncBase = Double.NaN;

    public AttributeInstance(Attribute attribute, Context context, Entity entity) {
        this.attribute = attribute;
        this.context = context;
        this.entity = entity;
        this.lastBase = attribute.baseValueSource().resolve(entity);
    }

    public Attribute attribute() {
        return this.attribute;
    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.recalculate();
            this.dirty = false;
        }
        return this.cachedValue;
    }

    public boolean hasModifier(Key id) {
        return this.byId.containsKey(id);
    }

    public AttributeModifier getModifier(Key id) {
        return this.byId.get(id);
    }

    public void removeModifier(Key id) {
        AttributeModifier removed = this.byId.remove(id);
        if (removed != null) {
            Map<Key, AttributeModifier> operations = this.getModifiersByOperation(removed.operation());
            if (operations != null) {
                operations.remove(id);
            }
        }
        if (this.trackedModifiers != null) {
            this.trackedModifiers.remove(id);
        }
        this.setDirty();
    }

    public void removeModifier(AttributeModifier modifier) {
        this.removeModifier(modifier.id());
    }

    public void addModifier(AttributeModifier modifier) {
        AttributeModifier previous = this.byId.putIfAbsent(modifier.id(), modifier);
        if (previous != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            this.getModifiersByOperation(modifier.operation()).put(modifier.id(), modifier);
            this.trackIfNeeded(modifier);
            this.setDirty();
        }
    }

    public void addOrUpdateModifier(AttributeModifier modifier) {
        AttributeModifier oldModifier = this.byId.put(modifier.id(), modifier);
        if (modifier != oldModifier) {
            if (oldModifier != null && !oldModifier.operation().equals(modifier.operation())) {
                Map<Key, AttributeModifier> oldOperations = this.byOperation.get(oldModifier.operation());
                if (oldOperations != null) {
                    oldOperations.remove(modifier.id());
                }
            }
            this.getModifiersByOperation(modifier.operation()).put(modifier.id(), modifier);
            this.trackIfNeeded(modifier);
            this.setDirty();
        }
    }

    private void trackIfNeeded(AttributeModifier modifier) {
        if (modifier.isDynamic() && modifier.updateInterval() > 0) {
            if (this.trackedModifiers == null) {
                this.trackedModifiers = new Object2ObjectArrayMap<>();
            }
            this.trackedModifiers.put(modifier.id(), new TrackedModifier(modifier.updateInterval(), -1, modifier.amount(this.context), modifier.condition().test(this.context)));
        } else if (this.trackedModifiers != null) {
            this.trackedModifiers.remove(modifier.id());
        }
    }

    public void updateTrackedModifiers(long tick) {
        Map<Key, TrackedModifier> tracked = this.trackedModifiers;
        if (tracked == null || tracked.isEmpty()) return;
        for (Map.Entry<Key, TrackedModifier> entry : tracked.entrySet()) {
            TrackedModifier state = entry.getValue();
            if (state.nextTick == -1) {
                state.nextTick = tick + state.interval;
                continue;
            }
            if (tick < state.nextTick) continue;
            state.nextTick = tick + state.interval;
            AttributeModifier modifier = this.byId.get(entry.getKey());
            if (modifier == null) continue;
            double amount = modifier.amount(this.context);
            boolean condition = modifier.condition().test(this.context);
            if (amount != state.amount || condition != state.condition) {
                state.amount = amount;
                state.condition = condition;
                this.setDirty();
            }
        }
    }

    public void setDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public Map<Key, AttributeModifier> getModifiersByOperation(Key operation) {
        return this.byOperation.computeIfAbsent(operation, k -> new Object2ObjectOpenHashMap<>());
    }

    public void updateBaseValue() {
        double base = this.attribute.baseValueSource().resolve(this.entity);
        if (base != this.lastBase) {
            this.lastBase = base;
            this.setDirty();
        }
    }

    public double recalculate() {
        double value = this.lastBase;
        Map<Key, TrackedModifier> tracked = this.trackedModifiers;
        for (AttributeOperation operation : this.attribute.operations()) {
            Map<Key, AttributeModifier> attributeModifiers = this.byOperation.get(operation.id());
            if (attributeModifiers != null) {
                double phaseBase = value;
                for (AttributeModifier modifier : attributeModifiers.values()) {
                    // 被轮询跟踪的修饰符吃快照值——轮询点是唯一求值点，避免双重求值与异步变量的前后不一致
                    TrackedModifier snapshot = tracked == null ? null : tracked.get(modifier.id());
                    if (snapshot != null) {
                        if (snapshot.condition) {
                            value = operation.apply(phaseBase, value, snapshot.amount);
                        }
                    } else if (modifier.condition().test(this.context)) {
                        value = operation.apply(phaseBase, value, modifier.amount(this.context));
                    }
                }
            }
        }
        return this.attribute.limit(value);
    }

    public boolean needVanillaSync() {
        return !this.attribute.syncTargets().isEmpty();
    }

    private static final class TrackedModifier {
        private final int interval;
        private long nextTick;
        private double amount;
        private boolean condition;

        private TrackedModifier(int interval, long nextTick, double amount, boolean condition) {
            this.interval = interval;
            this.nextTick = nextTick;
            this.amount = amount;
            this.condition = condition;
        }
    }

    public void syncToVanilla() {
        List<SyncTarget> targets = this.attribute.syncTargets();
        if (targets.isEmpty()) return;
        if (!(this.entity instanceof LivingEntity livingEntity)) return;
        double value = this.cachedValue;
        double base = this.lastBase;
        if (Double.compare(value, this.lastSyncValue) == 0 && Double.compare(base, this.lastSyncBase) == 0) return;
        this.lastSyncValue = value;
        this.lastSyncBase = base;
        for (SyncTarget target : targets) {
            VanillaAttributeInstance vanillaAttribute = livingEntity.getVanillaAttribute(target.target());
            if (vanillaAttribute != null) {
                vanillaAttribute.addOrUpdateTransientModifier(this.attribute.id(), target.operation(), target.evaluate(value, base));
            }
        }
    }
}
