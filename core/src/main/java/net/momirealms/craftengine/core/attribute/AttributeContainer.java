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
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.SwapList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class AttributeContainer implements AttributeGetter, SwapList.Indexed {
    private final Entity entity;
    private final AttributeInstance[] instances;
    private final ImmutableMap<Key, AttributeInstance> instancesById;
    private final EntityEquipments equipments;
    // 不可变 context
    private final Context context;
    // 在管理器 tick 列表中的下标，-1 表示不在列表中
    private int tickListIndex = -1;

    public AttributeContainer(Entity entity, List<Attribute> applicable) {
        this.entity = entity;
        this.context = new AttributeContainerContext(entity, ContextHolder.builder()
                .withParameter(DirectContextParameters.ENTITY, entity)
                .withOptionalParameter(DirectContextParameters.PLAYER, entity instanceof Player player ? player : null)
                .immutable(true)
                .build());
        this.equipments = new EntityEquipments(this);
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
            this.equipments.updateSets();
        }
    }

    private AttributeInstance create(Attribute attribute) {
        return new AttributeInstance(attribute, this.context, this.entity);
    }

    public Entity entity() {
        return this.entity;
    }

    public Context context() {
        return this.context;
    }

    public EntityEquipments equipments() {
        return this.equipments;
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

    @Override
    public int index() {
        return this.tickListIndex;
    }

    @Override
    public void index(int index) {
        this.tickListIndex = index;
    }

    public AttributeContainerSnapshot createSnapshot() {
        ImmutableMap.Builder<Key, Double> builder = ImmutableMap.builder();
        for (AttributeInstance instance : this.instances) {
            builder.put(instance.attribute().id(), instance.getValue());
        }
        return new AttributeContainerSnapshot(this, builder.build());
    }
}
