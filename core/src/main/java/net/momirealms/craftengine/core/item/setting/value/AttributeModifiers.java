package net.momirealms.craftengine.core.item.setting.value;

import net.momirealms.craftengine.core.attribute.Attribute;
import net.momirealms.craftengine.core.attribute.AttributeOperation;
import net.momirealms.craftengine.core.attribute.equipment.EquipmentSetSlot;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierConfig;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierScope;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;
import net.momirealms.craftengine.core.plugin.context.Context;

import java.util.ArrayList;
import java.util.List;

public final class AttributeModifiers {
    private final List<SlotAttributeModifierConfig> modifiers;

    public AttributeModifiers(List<SlotAttributeModifierConfig> modifiers) {
        this.modifiers = modifiers;
    }

    public static double weaponValue(List<SlotAttributeModifierConfig> modifiers, Attribute attribute, Context context) {
        double value = 0;
        for (AttributeOperation operation : attribute.operations()) {
            double phaseBase = value;
            for (SlotAttributeModifierConfig config : modifiers) {
                if (config.scope != AttributeModifierScope.WEAPON) continue;
                if (!config.attribute.equals(attribute.id())) continue;
                if (!config.operation.equals(operation.id())) continue;
                if (!config.condition.test(context)) continue;
                value = operation.apply(phaseBase, value, config.amount.getDouble(context));
            }
        }
        return attribute.limit(value);
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
