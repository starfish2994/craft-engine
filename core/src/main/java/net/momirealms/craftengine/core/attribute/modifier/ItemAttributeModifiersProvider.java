package net.momirealms.craftengine.core.attribute.modifier;

import net.momirealms.craftengine.core.item.Item;

import java.util.List;

@FunctionalInterface
public interface ItemAttributeModifiersProvider {
    int PRIORITY_SETTINGS = 100;
    int PRIORITY_PERSISTENT = 200;

    List<SlotAttributeModifierConfig> getModifiers(Item item);
}
