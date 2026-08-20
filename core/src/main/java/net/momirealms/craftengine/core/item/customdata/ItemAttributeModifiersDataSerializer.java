package net.momirealms.craftengine.core.item.customdata;

import net.momirealms.craftengine.core.attribute.equipment.EquipmentSlotGroup;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierScope;
import net.momirealms.craftengine.core.attribute.modifier.ItemAttributeModifier;
import net.momirealms.craftengine.core.util.CustomDataSerializer;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import net.momirealms.sparrow.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public final class ItemAttributeModifiersDataSerializer implements CustomDataSerializer<ItemAttributeModifiersData> {
    public static final ItemAttributeModifiersDataSerializer INSTANCE = new ItemAttributeModifiersDataSerializer();

    private ItemAttributeModifiersDataSerializer() {}

    @Override
    public Tag serialize(ItemAttributeModifiersData data) {
        ListTag list = new ListTag();
        for (ItemAttributeModifier modifier : data.modifiers()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("attribute", modifier.attribute().asString());
            tag.putString("id", modifier.id().asString());
            tag.putDouble("amount", modifier.amount());
            tag.putString("operation", modifier.operation().asString());
            tag.putString("scope", modifier.scope().id());
            tag.putString("slot", modifier.slot().name());
            list.addTag(list.size(), tag);
        }
        return list;
    }

    @Override
    public ItemAttributeModifiersData deserialize(Tag tag) {
        if (tag instanceof ListTag list) {
            List<ItemAttributeModifier> modifiers = new ArrayList<>(list.size());
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof CompoundTag compound) {
                    EquipmentSlotGroup slot = EquipmentSlotGroup.byNameOrSlot(compound.getString("slot"));
                    AttributeModifierScope scope = AttributeModifierScope.byId(compound.getString("scope"));
                    modifiers.add(new ItemAttributeModifier(
                            Key.of(compound.getString("attribute")),
                            Key.of(compound.getString("id")),
                            compound.getDouble("amount"),
                            Key.of(compound.getString("operation")),
                            scope == null ? AttributeModifierScope.ENTITY : scope,
                            slot == null ? EquipmentSlotGroup.ANY : slot
                    ));
                }
            }
            return new ItemAttributeModifiersData(modifiers);
        }
        return null;
    }
}
