package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.attribute.equipment.EquipmentSlotGroup;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class SlotAttributeModifierConfig extends AttributeModifierConfig {
    public final EquipmentSlotGroup slot;

    public SlotAttributeModifierConfig(Key attribute, Key id, NumberProvider amount, Key operation, Predicate<Context> condition, AttributeModifierScope scope, EquipmentSlotGroup slot, int updateInterval, boolean dynamic) {
        super(attribute, id, amount, operation, condition, scope, updateInterval, dynamic);
        this.slot = slot;
    }

    public ItemBoundAttributeModifier build(Item item) {
        return new ItemBoundAttributeModifier(this, item);
    }

    @SuppressWarnings("DuplicatedCode")
    public static SlotAttributeModifierConfig fromConfig(ConfigSection section) {
        EquipmentSlotGroup slot = Objects.requireNonNull(EquipmentSlotGroup.byNameOrSlot(section.getNonEmptyString("slot")));

        Key attribute = section.getNonNullIdentifier("type");
        Key id = section.getNonNullIdentifier("id");
        AttributeModifierScope scope = section.getEnum("scope", AttributeModifierScope.class, AttributeModifierScope.ENTITY);
        Key operation = section.getNonNullIdentifier("operation");

        NumberProvider amount = section.getNonNullNumber("amount");
        List<Predicate<Context>> conditionsList = section.getList(ConfigKeys.of("condition(s)"), CommonConditions::fromConfig);
        Predicate<Context> conditions = MiscUtils.allOf(conditionsList);

        boolean dynamic = !amount.isConstant() || !conditionsList.isEmpty();
        int updateInterval;
        if (dynamic) {
            updateInterval = section.getInt("update_interval", 20);
        } else {
            updateInterval = 0;
        }
        return new SlotAttributeModifierConfig(attribute, id, amount, operation, conditions, scope, slot, updateInterval, dynamic);
    }
}
