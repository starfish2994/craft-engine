package net.momirealms.craftengine.core.attribute;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifier;
import net.momirealms.craftengine.core.attribute.sync.SyncTarget;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributes;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributes1_21;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.LivingEntityContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.SwapList;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeInstance {
    private final Attribute attribute;
    private final Map<Key, Map<Key, AttributeModifier>> byOperation = new HashMap<>();
    private final Map<Key, AttributeModifier> byId = new Object2ObjectArrayMap<>();
    private final LivingEntityContext context;
    @Nullable
    private Map<Key, TrackedModifier> trackedById;
    @Nullable
    private SwapList<TrackedModifier> trackedList;
    // 最早到期 tick，未到期时整表跳过
    private volatile int nextTrackedTick = Integer.MAX_VALUE;
    private double cachedValue;
    private boolean dirty = true;
    private double lastBase;
    // 上次写回原版时的 (value, base)，用于变化检测
    private double lastSyncValue = Double.NaN;
    private double lastSyncBase = Double.NaN;

    public AttributeInstance(Attribute attribute, LivingEntityContext context) {
        this.attribute = attribute;
        this.context = context;
        this.lastBase = attribute.baseValueSource().resolve(context.entity);
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
        if (this.trackedById != null) {
            TrackedModifier removedTracked = this.trackedById.remove(id);
            if (removedTracked != null) {
                this.trackedList.swapRemove(removedTracked);
                if (this.trackedById.isEmpty()) {
                    this.nextTrackedTick = Integer.MAX_VALUE;
                }
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
            if (this.trackedById == null) {
                this.trackedById = new Object2ObjectOpenHashMap<>();
                this.trackedList = new SwapList<>();
            }
            TrackedModifier tracked = new TrackedModifier(modifier, modifier.amount(this.context), modifier.test(this.context));
            TrackedModifier previous = this.trackedById.put(modifier.id(), tracked);
            if (previous != null) {
                // 原位替换列表中的旧快照
                this.trackedList.set(previous.index(), tracked);
            } else {
                this.trackedList.add(tracked);
            }
            // 新条目待初始化，下一 tick 强制扫描一次
            this.nextTrackedTick = Integer.MIN_VALUE;
        } else if (this.trackedById != null) {
            TrackedModifier removed = this.trackedById.remove(modifier.id());
            if (removed != null) {
                this.trackedList.swapRemove(removed);
                if (this.trackedById.isEmpty()) {
                    this.nextTrackedTick = Integer.MAX_VALUE;
                }
            }
        }
    }

    public void updateTrackedModifiers(int tick) {
        if (tick < this.nextTrackedTick) return;
        SwapList<TrackedModifier> tracked = this.trackedList;
        if (tracked == null) {
            this.nextTrackedTick = Integer.MAX_VALUE;
            return;
        }
        Object[] array = tracked.elements();
        int size = array.length;
        if (size == 0) {
            this.nextTrackedTick = Integer.MAX_VALUE;
            return;
        }
        int nextDue = Integer.MAX_VALUE;
        for (int i = 0; i < size; i++) {
            TrackedModifier state = (TrackedModifier) array[i];
            if (state == null || state.index() == -1) continue;
            AttributeModifier modifier = state.modifier;
            int interval = modifier.updateInterval();
            if (state.nextTick == -1) {
                // 首次调度按 id 哈希错开，避免批量刷新后同 interval 的修饰符挤在同一 tick 求值
                state.nextTick = tick + interval + Math.floorMod(modifier.id().hashCode(), interval);
            }
            if (tick < state.nextTick) {
                nextDue = Math.min(nextDue, state.nextTick);
                continue;
            }
            state.nextTick = tick + interval;
            nextDue = Math.min(nextDue, state.nextTick);
            boolean condition = modifier.test(this.context);
            // 条件不满足时快照值不参与 recalculate，跳过 amount 求值
            double amount = condition ? modifier.amount(this.context) : state.amount;
            if (condition != state.condition || amount != state.amount) {
                state.amount = amount;
                state.condition = condition;
                this.setDirty();
            }
        }
        this.nextTrackedTick = nextDue;
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
        double base = this.attribute.baseValueSource().resolve(this.context.entity);
        if (base != this.lastBase) {
            this.lastBase = base;
            this.setDirty();
        }
    }

    public double recalculate() {
        double value = this.lastBase;
        Map<Key, TrackedModifier> tracked = this.trackedById;
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
                    } else if (modifier.test(this.context)) {
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

    public void syncToVanilla() {
        List<SyncTarget> targets = this.attribute.syncTargets();
        if (targets.isEmpty()) return;
        LivingEntity living = this.context.entity;
        double value = this.cachedValue;
        double base = this.lastBase;
        if (Double.compare(value, this.lastSyncValue) == 0 && Double.compare(base, this.lastSyncBase) == 0) return;
        this.lastSyncValue = value;
        this.lastSyncBase = base;
        for (SyncTarget target : targets) {
            VanillaAttributeInstance vanillaAttribute = living.getVanillaAttribute(target.target());
            if (vanillaAttribute != null) {
                if (isMaxHealth(target.target())) {
                    double oldMaxHealth = vanillaAttribute.getValue();
                    double health = living.health();
                    vanillaAttribute.addOrUpdateTransientModifier(this.attribute.id(), target.operation(), target.evaluate(value, base));
                    double newMaxHealth = vanillaAttribute.getValue();
                    if (oldMaxHealth > 0 && newMaxHealth > 0 && newMaxHealth != oldMaxHealth) {
                        living.setHealth(health * newMaxHealth / oldMaxHealth);
                    }
                } else {
                    vanillaAttribute.addOrUpdateTransientModifier(this.attribute.id(), target.operation(), target.evaluate(value, base));
                }
            }
        }
    }

    private boolean isMaxHealth(Key id) {
        if (VersionHelper.isOrAbove1_21) {
            return VanillaAttributes1_21.MAX_HEALTH.equals(id);
        } else {
            return VanillaAttributes.MAX_HEALTH.equals(id);
        }
    }

    private static final class TrackedModifier implements SwapList.Indexed {
        private final AttributeModifier modifier;
        private int nextTick = -1;
        private double amount;
        private boolean condition;
        private int index = -1;

        private TrackedModifier(AttributeModifier modifier, double amount, boolean condition) {
            this.modifier = modifier;
            this.amount = amount;
            this.condition = condition;
        }

        @Override
        public int index() {
            return this.index;
        }

        @Override
        public void index(int index) {
            this.index = index;
        }
    }
}
