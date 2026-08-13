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
            // 派生属性无实例：无缓存、无修饰符，查询时现算
            if (attribute.derived() != null) continue;
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

    // 重扫全部装备槽：实例覆盖/动态来源变化后，已穿戴物品经此方法重新生效
    public void refreshEquipments() {
        if (!(this.entity instanceof LivingEntity livingEntity)) return;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.equipments.remove(EquipmentSetSlot.fromEquipmentSlot(slot));
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item item = livingEntity.getItemByEquipmentSlot(slot);
            if (!item.isEmpty()) {
                this.equipments.add(EquipmentSetSlot.fromEquipmentSlot(slot), item);
            }
        }
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
        if (attribute.derived != null) {
            return attribute.derive(this::getAttributeValue);
        }
        AttributeInstance instance = getOrCreateInstance(attribute);
        if (instance == null) return 0;
        return instance.getValue();
    }

    public void tick() {
        for (AttributeInstance instance : this.instances.values()) {
            instance.updateBaseValue();
            if (instance.needVanillaSync()) {
                instance.getValue(); // 触发 dirty
                instance.syncToVanilla();
            }
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
