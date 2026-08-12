package net.momirealms.craftengine.core.attribute;

import com.google.common.collect.ImmutableMap;
import net.momirealms.craftengine.core.attribute.equipment.EntityEquipments;
import net.momirealms.craftengine.core.attribute.equipment.EquipmentSetSlot;
import net.momirealms.craftengine.core.attribute.sync.SyncTarget;
import net.momirealms.craftengine.core.attribute.vanilla.VanillaAttributeInstance;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.Key;

import java.util.HashMap;
import java.util.Map;

public final class AttributeContainer implements AttributeGetter {
    private final AttributeManager manager;
    private final Entity entity;
    private final Map<Key, AttributeInstance> instances = new HashMap<>();
    private final EntityEquipments equipments;
    private final Context context;

    public AttributeContainer(AttributeManager manager, Entity entity) {
        this.manager = manager;
        this.entity = entity;
        this.context = entity instanceof Player player ? PlayerOptionalContext.of(player) : PlayerOptionalContext.emptyImmutable();
        this.equipments = new EntityEquipments(this);
        for (Attribute attribute : manager.attributesByEntityType(entity.type())) {
            this.getOrCreateInstance(attribute);
        }
        if (entity instanceof LivingEntity livingEntity) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                Item item = livingEntity.getItemByEquipmentSlot(slot);
                if (!item.isEmpty()) {
                    this.equipments.add(EquipmentSetSlot.fromEquipmentSlot(slot), item);
                }
            }
        }
    }

    public Entity entity() {
        return this.entity;
    }

    public EntityEquipments equipments() {
        return this.equipments;
    }

    public AttributeInstance getOrCreateInstance(Key attribute) {
        Attribute attr = this.manager.getAttribute(attribute).orElseThrow(() -> new IllegalStateException("Attribute " + attribute + " not found"));
        return getOrCreateInstance(attr);
    }

    public AttributeInstance getOrCreateInstance(Attribute attribute) {
        return this.instances.computeIfAbsent(attribute.id(), k -> new AttributeInstance(attribute, this.context, this.entity));
    }

    public void clearSyncModifiers() {
        if (!(this.entity instanceof LivingEntity livingEntity)) return;
        for (AttributeInstance instance : this.instances.values()) {
            Attribute attribute = instance.attribute();
            for (SyncTarget target : attribute.syncTargets()) {
                VanillaAttributeInstance vanillaAttribute = livingEntity.getVanillaAttribute(target.target());
                if (vanillaAttribute != null) {
                    vanillaAttribute.removeModifier(attribute.id());
                }
            }
        }
    }

    @Override
    public double getAttributeValue(Attribute attribute) {
        AttributeInstance instance = getOrCreateInstance(attribute);
        double value = instance.getValue();
        instance.syncToVanilla();
        return value;
    }

    public void tick() {
        // 阶段一：重算脏的与动态基值的 sync 实例
        for (AttributeInstance instance : this.instances.values()) {
            if (instance.attribute().syncTargets().isEmpty()) continue;
            if (instance.isDirty() || instance.attribute().baseValueSource().isDynamic()) {
                instance.getValue();
            }
        }
        // 阶段二：统一写回原版（变化检测在内部，值没变不会重复发包）
        for (AttributeInstance instance : this.instances.values()) {
            if (instance.attribute().syncTargets().isEmpty()) continue;
            instance.syncToVanilla();
        }
    }

    public AttributeContainerSnapshot createSnapshot() {
        ImmutableMap.Builder<Key, Double> builder = ImmutableMap.builder();
        for (Map.Entry<Key, AttributeInstance> entry : this.instances.entrySet()) {
            builder.put(entry.getKey(), entry.getValue().getValue());
        }
        return new AttributeContainerSnapshot(this, builder.build());
    }
}
