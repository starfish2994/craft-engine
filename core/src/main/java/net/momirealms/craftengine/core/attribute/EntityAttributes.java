package net.momirealms.craftengine.core.attribute;

import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.core.attribute.sync.SyncTarget;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.LivingEntityContext;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EntityAttributes implements AttributeGetter {
    private final LivingEntityHolder holder;
    private final AttributeInstance[] instances;
    private final ImmutableMap<Key, AttributeInstance> instancesById;

    public EntityAttributes(LivingEntityHolder holder, List<Attribute> applicable) {
        this.holder = holder;
        ImmutableMap.Builder<Key, AttributeInstance> mapBuilder = ImmutableMap.builder();
        int count = 0;
        for (Attribute attribute : applicable) {
            if (attribute.derived() == null) count++;
        }
        this.instances = new AttributeInstance[count];
        int index = 0;
        for (Attribute attribute : applicable) {
            if (attribute.derived() != null) continue;
            AttributeInstance instance = new AttributeInstance(attribute, holder.context);
            this.instances[index++] = instance;
            mapBuilder.put(attribute.id(), instance);
        }
        this.instancesById = mapBuilder.build();
    }

    /** Returns {@code null} when this entity does not support the attribute. */
    @Nullable
    public AttributeInstance getInstance(Key attribute) {
        return this.instancesById.get(attribute);
    }

    public void clearSyncModifiers() {
        for (AttributeInstance instance : this.instances) {
            Attribute attribute = instance.attribute();
            for (SyncTarget target : attribute.syncTargets()) {
                VanillaAttributeInstance vanillaAttribute = this.holder.entity.getVanillaAttribute(target.target());
                if (vanillaAttribute != null) {
                    vanillaAttribute.removeModifier(attribute.id());
                }
            }
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

    public void tick(int gameTicks) {
        for (AttributeInstance instance : this.instances) {
            instance.updateBaseValue();
            instance.updateTrackedModifiers(gameTicks);
            if (instance.needVanillaSync()) {
                instance.getValue();
                instance.syncToVanilla();
            }
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
