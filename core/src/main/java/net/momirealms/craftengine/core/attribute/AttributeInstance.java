package net.momirealms.craftengine.core.attribute;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
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
    private Double manualBaseValue;
    private double cachedValue;
    private boolean dirty = true;
    // 上次写回原版时的 (value, base)，用于变化检测
    private double lastSyncValue = Double.NaN;
    private double lastSyncBase = Double.NaN;

    public AttributeInstance(Attribute attribute, Context context, Entity entity) {
        this.attribute = attribute;
        this.context = context;
        this.entity = entity;
    }

    public Attribute attribute() {
        return this.attribute;
    }

    public double getValue() {
        // 动态基值（原版映射）每次读取都重算，保证原版基值变化及时反映
        if (this.dirty || this.attribute.baseValueSource().isDynamic()) {
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
            this.setDirty();
        }
    }

    public void addOrUpdateModifier(AttributeModifier modifier) {
        AttributeModifier oldModifier = this.byId.put(modifier.id(), modifier);
        if (modifier != oldModifier) {
            this.getModifiersByOperation(modifier.operation()).put(modifier.id(), modifier);
            this.setDirty();
        }
    }

    /**
     * 手动覆盖基值；设置后优先于 BaseValueSource。
     */
    public void setBaseValue(double baseValue) {
        this.manualBaseValue = baseValue;
        this.setDirty();
    }

    public void setDirty() {
        this.dirty = true;
    }

    public Map<Key, AttributeModifier> getModifiersByOperation(Key operation) {
        return this.byOperation.computeIfAbsent(operation, k -> new Object2ObjectOpenHashMap<>());
    }

    public double recalculate() {
        double base = this.manualBaseValue != null ? this.manualBaseValue : this.attribute.baseValueSource().resolve(this.entity);
        double value = base;
        for (AttributeOperation operation : CraftEngine.instance().attributeManager().sortedOperations()) {
            Map<Key, AttributeModifier> attributeModifiers = this.byOperation.get(operation.id());
            if (attributeModifiers != null) {
                double phaseBase = value;
                for (AttributeModifier modifier : attributeModifiers.values()) {
                    if (modifier.condition().test(this.context)) {
                        value = operation.apply(phaseBase, value, modifier.amount(this.context));
                    }
                }
            }
        }
        value = this.attribute.limit(value);
        syncToVanilla(value, base);
        return value;
    }

    private void syncToVanilla(double value, double base) {
        List<SyncTarget> targets = this.attribute.syncTargets();
        if (targets.isEmpty()) return;
        // 变化检测：value 与 base 都没变就不重复写回，避免无意义的原版 dirty 与属性包重发
        if (Double.compare(value, this.lastSyncValue) == 0 && Double.compare(base, this.lastSyncBase) == 0) return;
        if (!(this.entity instanceof LivingEntity livingEntity)) return;
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
