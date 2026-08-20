package net.momirealms.craftengine.core.item.setting.value;

import net.momirealms.craftengine.core.attribute.equipment.EquipmentSetSlot;
import net.momirealms.craftengine.core.item.equipment.SetPotionEffect;
import net.momirealms.craftengine.core.item.equipment.SlotPotionEffect;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentPotionEffects {
    private final List<SlotPotionEffect> effects;

    public EquipmentPotionEffects(List<SlotPotionEffect> effects) {
        this.effects = List.copyOf(effects);
    }

    public List<SlotPotionEffect> effects() {
        return this.effects;
    }

    public List<SetPotionEffect> effects(EquipmentSetSlot slot) {
        if (this.effects.isEmpty()) return List.of();
        List<SetPotionEffect> matched = new ArrayList<>(this.effects.size());
        for (SlotPotionEffect effect : this.effects) {
            if (effect.slot().test(slot)) {
                matched.add(effect.effect());
            }
        }
        return matched.isEmpty() ? List.of() : List.copyOf(matched);
    }
}
