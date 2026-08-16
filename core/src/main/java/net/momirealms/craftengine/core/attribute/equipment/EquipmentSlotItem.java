package net.momirealms.craftengine.core.attribute.equipment;

import net.momirealms.craftengine.core.attribute.AttributeContainer;
import net.momirealms.craftengine.core.attribute.AttributeInstance;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierConfig;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierScope;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentSlotItem {
    private final Item item;
    private final List<SlotAttributeModifierConfig> snapshots;

    private EquipmentSlotItem(Item item, List<SlotAttributeModifierConfig> snapshots) {
        this.item = item;
        this.snapshots = snapshots;
    }

    public static EquipmentSlotItem create(EquipmentSetSlot slot, Item item) {
        List<SlotAttributeModifierConfig> modifiers = CraftEngine.instance().attributeManager().getItemAttributeModifiers(item);
        if (modifiers.isEmpty()) return new EquipmentSlotItem(item, List.of());
        List<SlotAttributeModifierConfig> snapshots = new ArrayList<>(modifiers.size());
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

    public List<SlotAttributeModifierConfig> snapshots() {
        return this.snapshots;
    }

    public void addOrUpdateModifiers(AttributeContainer container) {
        for (SlotAttributeModifierConfig config : this.snapshots) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = container.getInstance(config.attribute);
            if (instance == null) continue;
            instance.addOrUpdateModifier(config.build(this.item));
        }
    }

    public void removeModifiers(AttributeContainer container) {
        for (SlotAttributeModifierConfig config : this.snapshots) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = container.getInstance(config.attribute);
            if (instance == null) continue;
            instance.removeModifier(config.id);
        }
    }
}
