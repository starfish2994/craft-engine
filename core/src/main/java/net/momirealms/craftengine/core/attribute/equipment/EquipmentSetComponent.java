package net.momirealms.craftengine.core.attribute.equipment;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;

public final class EquipmentSetComponent {
    private final List<EquipmentSetSlot> slots;
    private final List<Key> sets;

    public EquipmentSetComponent(List<EquipmentSetSlot> slots, List<Key> sets) {
        this.slots = slots;
        this.sets = sets;
    }

    public static EquipmentSetComponent fromConfig(ConfigSection section) {
        List<EquipmentSetSlot> slots = section.getList("slots", v -> {
            EquipmentSetSlot slot = EquipmentSetSlot.byName(v.getAsString());
            if (slot == null) {
                throw new KnownResourceException("resource.equipment_set.invalid_slot_name", section.assemblePath("slots"), v.getAsString());
            }
            return slot;
        });
        List<Key> sets = section.getList("sets", ConfigValue::getAsIdentifier);
        return new EquipmentSetComponent(slots, sets);
    }

    public List<EquipmentSetSlot> slots() {
        return this.slots;
    }

    public List<Key> sets() {
        return this.sets;
    }
}
