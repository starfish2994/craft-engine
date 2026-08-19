package net.momirealms.craftengine.core.attribute;

import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;

public final class EntityAttributes implements AttributeGetter {
    private final LivingEntityHolder holder;
    private final AttributeInstance[] instances;
    private final ImmutableMap<Key, AttributeInstance> instancesById;
    private final BitSet dirtySyncInstances;
    private final BitSet scheduledInstances;
    private boolean running;

    public EntityAttributes(LivingEntityHolder holder, List<Attribute> applicable) {
        this.holder = holder;
        ImmutableMap.Builder<Key, AttributeInstance> mapBuilder = ImmutableMap.builder();
        int count = 0;
        for (Attribute attribute : applicable) {
            if (attribute.derived() == null) count++;
        }
        this.instances = new AttributeInstance[count];
        this.dirtySyncInstances = new BitSet(count);
        this.scheduledInstances = new BitSet(count);
        int index = 0;
        for (Attribute attribute : applicable) {
            if (attribute.derived() != null) continue;
            AttributeInstance instance = new AttributeInstance(attribute, holder.context, this, index);
            this.instances[index] = instance;
            mapBuilder.put(attribute.id(), instance);
            if (instance.needInitialVanillaSync()) {
                // Only non-neutral initial amounts need an NMS modifier. Equipment
                // mutations that happen after construction wake the instance normally.
                this.dirtySyncInstances.set(index);
            }
            if (instance.nextRequiredTick() != Long.MAX_VALUE) {
                this.scheduledInstances.set(index);
            }
            index++;
        }
        this.instancesById = mapBuilder.build();
        if (!this.dirtySyncInstances.isEmpty() || nextRequiredTick() != Long.MAX_VALUE) {
            this.holder.wakeAttributes();
        }
    }

    /** Returns {@code null} when this entity does not support the attribute. */
    @Nullable
    public AttributeInstance getInstance(Key attribute) {
        return this.instancesById.get(attribute);
    }

    public void clearSyncModifiers() {
        for (AttributeInstance instance : this.instances) {
            instance.clearSyncModifiers();
        }
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        if (attribute.derived() != null) {
            return attribute.derive(this::getAttributeValue);
        }
        AttributeInstance instance = getInstance(attribute.id());
        return instance == null ? 0 : instance.getValue();
    }

    /** Called by an instance whenever its calculated value may have changed. */
    void onInstanceDirty(AttributeInstance instance) {
        if (!instance.needVanillaSync()) return;
        this.dirtySyncInstances.set(instance.index());
        if (!this.running) {
            this.holder.wakeAttributes();
        }
    }

    /** Called when a dynamic modifier was added, removed, or rescheduled. */
    void onScheduleChanged(AttributeInstance instance) {
        this.scheduledInstances.set(instance.index(), instance.nextRequiredTick() != Long.MAX_VALUE);
        if (!this.running) {
            this.holder.wakeAttributes();
        }
    }

    /** Runs only dynamic evaluations that are due and one-shot vanilla sync work. */
    public long runDue(long gameTick) {
        this.running = true;
        try {
            for (int index = this.scheduledInstances.nextSetBit(0);
                 index >= 0;
                 index = this.scheduledInstances.nextSetBit(index + 1)) {
                AttributeInstance instance = this.instances[index];
                if (instance.nextRequiredTick() <= gameTick) {
                    instance.runDue(gameTick);
                    if (instance.nextRequiredTick() == Long.MAX_VALUE) {
                        this.scheduledInstances.clear(index);
                    }
                }
            }
            flushDirtySyncInstances();
            return nextRequiredTick();
        } finally {
            this.running = false;
        }
    }

    public long nextRequiredTick() {
        long next = Long.MAX_VALUE;
        for (int index = this.scheduledInstances.nextSetBit(0);
             index >= 0;
             index = this.scheduledInstances.nextSetBit(index + 1)) {
            next = Math.min(next, this.instances[index].nextRequiredTick());
        }
        return next;
    }

    private void flushDirtySyncInstances() {
        for (int index = this.dirtySyncInstances.nextSetBit(0); index >= 0; index = this.dirtySyncInstances.nextSetBit(index + 1)) {
            AttributeInstance instance = this.instances[index];
            instance.getValue();
            instance.syncToVanilla();
            this.dirtySyncInstances.clear(index);
        }
    }

    public EntityAttributesSnapshot createSnapshot() {
        ImmutableMap.Builder<Key, Double> builder = ImmutableMap.builder();
        for (AttributeInstance instance : this.instances) {
            builder.put(instance.attribute().id(), instance.getValue());
        }
        return new EntityAttributesSnapshot(builder.build());
    }
}
