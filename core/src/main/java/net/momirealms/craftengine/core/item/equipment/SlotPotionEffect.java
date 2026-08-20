package net.momirealms.craftengine.core.item.equipment;

import net.momirealms.craftengine.core.attribute.equipment.EquipmentSlotGroup;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;

import java.util.Objects;

public record SlotPotionEffect(
        EquipmentSlotGroup slot,
        SetPotionEffect effect
) {

    public static SlotPotionEffect fromConfig(ConfigSection section) {
        EquipmentSlotGroup slot = Objects.requireNonNull(EquipmentSlotGroup.byNameOrSlot(section.getNonEmptyString("slot")), "Unknown equipment slot group " + section.getString("slot"));
        return new SlotPotionEffect(slot, SetPotionEffect.fromConfig(section));
    }
}
