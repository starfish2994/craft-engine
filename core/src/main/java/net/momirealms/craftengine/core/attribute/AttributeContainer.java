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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AttributeContainer implements AttributeGetter {
    private final AttributeManager manager;
    private final Entity entity;
    private final AttributeInstance[] instances;
    private final ImmutableMap<Key, AttributeInstance> instancesById;
    private final EntityEquipments equipments;
    private final Context context;

    public AttributeContainer(AttributeManager manager, Entity entity) {
        this.manager = manager;
        this.entity = entity;
        this.context = entity instanceof Player player ? PlayerOptionalContext.of(player) : PlayerOptionalContext.emptyImmutable();
        this.equipments = new EntityEquipments(this);
        List<Attribute> applicable = manager.attributesByEntityType(entity.type());
        ImmutableMap.Builder<Key, AttributeInstance> mapBuilder = ImmutableMap.builder();
        int count = 0;
        for (Attribute attribute : applicable) {
            if (attribute.derived() != null) continue;
            count++;
        }
        this.instances = new AttributeInstance[count];
        int index = 0;
        for (Attribute attribute : applicable) {
            if (attribute.derived() != null) continue;
            AttributeInstance instance = create(attribute);
            this.instances[index++] = instance;
            mapBuilder.put(attribute.id(), instance);
        }
        this.instancesById = mapBuilder.build();
        if (entity instanceof LivingEntity livingEntity) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                Item item = livingEntity.getItemByEquipmentSlot(slot);
                if (!item.isEmpty()) {
                    this.equipments.add(EquipmentSetSlot.fromEquipmentSlot(slot), item);
                }
            }
        }
    }

    private AttributeInstance create(Attribute attribute) {
        return new AttributeInstance(attribute, this.context, this.entity);
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

    // 该实体不适用的属性（含派生属性）返回 null
    @Nullable
    public AttributeInstance getInstance(Key attribute) {
        return this.instancesById.get(attribute);
    }

    public void clearSyncModifiers() {
        if (!(this.entity instanceof LivingEntity livingEntity)) return;
        for (AttributeInstance instance : this.instances) {
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
        AttributeInstance instance = getInstance(attribute.id());
        if (instance == null) return 0;
        return instance.getValue();
    }

    public void tick(int gameTicks) {
        for (AttributeInstance instance : this.instances) {
            instance.updateBaseValue();
            instance.updateTrackedModifiers(gameTicks);
            if (instance.needVanillaSync()) {
                instance.getValue(); // 触发 dirty
                instance.syncToVanilla();
            }
        }
    }

    public AttributeContainerSnapshot createSnapshot() {
        ImmutableMap.Builder<Key, Double> builder = ImmutableMap.builder();
        for (AttributeInstance instance : this.instances) {
            builder.put(instance.attribute().id(), instance.getValue());
        }
        return new AttributeContainerSnapshot(this, builder.build());
    }
}
