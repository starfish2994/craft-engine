package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeGetter;
import net.momirealms.craftengine.core.attribute.EmptyAttributeHolder;
import net.momirealms.craftengine.core.attribute.EntityAttributes;
import net.momirealms.craftengine.core.attribute.equipment.EntityEquipments;
import net.momirealms.craftengine.core.attribute.equipment.EquipmentPotionEffectController;
import net.momirealms.craftengine.core.attribute.equipment.EquipmentSetSlot;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.SwapList;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class LivingEntityHolder implements SwapList.Indexed {
    public final LivingEntity entity;
    public final AttributeGetter attributes;
    public final EntityEquipments equipments;
    public final EquipmentPotionEffectController potionEffects;
    public final LivingEntityContext context;
    // 在管理器 tick 列表中的下标，-1 表示不在列表中
    private int tickListIndex = -1;

    public LivingEntityHolder(LivingEntity entity) {
        this.entity = entity;
        this.context = new LivingEntityContext(entity, ContextHolder.builder()
                .withParameter(DirectContextParameters.ENTITY, entity)
                .withOptionalParameter(DirectContextParameters.PLAYER, entity instanceof Player player ? player : null)
                .immutable(true)
                .build());
        List<Attribute> attributeList = Config.enableAttributeSystem() ? CraftEngine.instance().attributeManager().attributesByEntityType(entity.type()) : List.of();
        this.attributes = attributeList.isEmpty() ? EmptyAttributeHolder.INSTANCE : new EntityAttributes(this, attributeList);
        this.potionEffects = new EquipmentPotionEffectController(entity);
        this.equipments = new EntityEquipments(this);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Item item = this.entity.getItemByEquipmentSlot(slot);
            if (!item.isEmpty()) {
                this.equipments.add(EquipmentSetSlot.fromEquipmentSlot(slot), item);
            }
        }
        this.equipments.updateSets(false);
    }

    @Override
    public int index() {
        return this.tickListIndex;
    }

    @Override
    public void index(int index) {
        this.tickListIndex = index;
    }

    public LivingEntity entity() {
        return this.entity;
    }

    public AttributeGetter attributes() {
        return this.attributes;
    }

    public void ifAttributesExist(Consumer<EntityAttributes> consumer) {
        if (this.attributes instanceof EntityAttributes attr) {
            consumer.accept(attr);
        }
    }

    public void applyEquipmentChanges(Map<EquipmentSetSlot, ? extends Item> changes) {
        for (EquipmentSetSlot slot : changes.keySet()) {
            this.equipments.remove(slot);
        }
        for (Map.Entry<EquipmentSetSlot, ? extends Item> entry : changes.entrySet()) {
            Item item = entry.getValue();
            if (item != null && !item.isEmpty()) {
                this.equipments.add(entry.getKey(), item);
            }
        }
        this.equipments.updateSets();
    }

    public void tick(int gameTick, boolean tickAttributes) {
        this.potionEffects.tick();
        if (tickAttributes && this.attributes instanceof EntityAttributes entityAttributes) {
            entityAttributes.tick(gameTick);
        }
    }

    public synchronized void close(boolean death) {
        this.equipments.clearSetEffects();
        this.potionEffects.close(death);
        if (this.attributes instanceof EntityAttributes entityAttributes) {
            entityAttributes.clearSyncModifiers();
        }
    }
}
