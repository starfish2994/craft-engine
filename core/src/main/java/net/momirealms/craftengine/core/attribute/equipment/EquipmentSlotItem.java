package net.momirealms.craftengine.core.attribute.equipment;
import net.momirealms.craftengine.core.attribute.*;
import net.momirealms.craftengine.core.attribute.modifier.*;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentSlotItem {
    private final Item item;
    private final List<AttributeModifierConfig> snapshots;

    private EquipmentSlotItem(Item item, List<AttributeModifierConfig> snapshots) {
        this.item = item;
        this.snapshots = snapshots;
    }

    public static EquipmentSlotItem create(EquipmentSetSlot slot, Item item) {
        List<SlotAttributeModifierConfig> modifiers = CraftEngine.instance().attributeManager().getItemAttributeModifiers(item);
        if (modifiers.isEmpty()) return new EquipmentSlotItem(item, List.of());
        List<AttributeModifierConfig> snapshots = new ArrayList<>(modifiers.size());
        for (SlotAttributeModifierConfig config : modifiers) {
            if (config.slot.test(slot)) {
                snapshots.add(config);
            }
        }
        return new EquipmentSlotItem(item, snapshots);
    }

    public Item item() {
        return this.item;
    }

    public List<AttributeModifierConfig> snapshots() {
        return this.snapshots;
    }

    public void addOrUpdateModifiers(AttributeContainer container) {
        for (AttributeModifierConfig config : this.snapshots) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = container.getOrCreateInstance(config.attribute);
            instance.addOrUpdateModifier(config.build());
        }
    }

    public void removeModifiers(AttributeContainer container) {
        for (AttributeModifierConfig config : this.snapshots) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = container.getOrCreateInstance(config.attribute);
            instance.removeModifier(config.id);
        }
    }
}
