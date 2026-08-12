package net.momirealms.craftengine.core.item.setting.value;

import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierConfig;
import net.momirealms.craftengine.core.attribute.equipment.EquipmentSetSlot;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;

import java.util.ArrayList;
import java.util.List;

public final class AttributeModifiers {
    private final List<SlotAttributeModifierConfig> modifiers;

    public AttributeModifiers(List<SlotAttributeModifierConfig> modifiers) {
        this.modifiers = modifiers;
    }

    public List<SlotAttributeModifierConfig> modifiers() {
        return this.modifiers;
    }

    public List<AttributeModifierConfig> modifiers(EquipmentSetSlot slot) {
        List<AttributeModifierConfig> attributeModifiers = new ArrayList<>(this.modifiers.size());
        for (SlotAttributeModifierConfig config : this.modifiers) {
            if (config.slot.test(slot)) {
                attributeModifiers.add(config);
            }
        }
        return attributeModifiers;
    }
}
