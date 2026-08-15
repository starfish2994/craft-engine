package net.momirealms.craftengine.core.attribute.equipment;

import net.momirealms.craftengine.core.attribute.AttributeContainer;
import net.momirealms.craftengine.core.attribute.AttributeInstance;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierConfig;
import net.momirealms.craftengine.core.attribute.modifier.AttributeModifierScope;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.number.ItemBoundNumberProvider;

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
                snapshots.add(bindItem(config, item));
            }
        }
        return new EquipmentSlotItem(item, snapshots);
    }

    private static AttributeModifierConfig bindItem(SlotAttributeModifierConfig config, Item item) {
        if (!config.dynamic) return config;
        return new SlotAttributeModifierConfig(
                config.attribute,
                config.id,
                config.amount.isConstant() ? config.amount : new ItemBoundNumberProvider(config.amount, item),
                config.operation,
                ctx -> config.condition.test(ItemBoundNumberProvider.bind(ctx, item)),
                config.scope,
                config.slot,
                config.updateInterval,
                config.dynamic
        );
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
            AttributeInstance instance = container.getInstance(config.attribute);
            if (instance == null) continue;
            instance.addOrUpdateModifier(config.build());
        }
    }

    public void removeModifiers(AttributeContainer container) {
        for (AttributeModifierConfig config : this.snapshots) {
            if (config.scope == AttributeModifierScope.WEAPON) continue;
            AttributeInstance instance = container.getInstance(config.attribute);
            if (instance == null) continue;
            instance.removeModifier(config.id);
        }
    }
}
