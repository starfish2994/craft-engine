package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.attribute.equipment.EquipmentSlotGroup;
import net.momirealms.craftengine.core.plugin.context.number.ConstantNumberProvider;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

public record ItemAttributeModifier(Key attribute, Key id, double amount, Key operation, AttributeModifierScope scope, EquipmentSlotGroup slot) {

    public ItemAttributeModifier(Key attribute, Key id, double amount, Key operation, AttributeModifierScope scope) {
        this(attribute, id, amount, operation, scope, EquipmentSlotGroup.ANY);
    }

    public SlotAttributeModifierConfig toConfig() {
        return new SlotAttributeModifierConfig(this.attribute, this.id, ConstantNumberProvider.constant(this.amount), this.operation, MiscUtils.allOf(), this.scope, this.slot, 0, false);
    }
}
