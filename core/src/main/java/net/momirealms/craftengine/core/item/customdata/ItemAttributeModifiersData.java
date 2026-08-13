package net.momirealms.craftengine.core.item.customdata;

import net.momirealms.craftengine.core.attribute.modifier.ItemAttributeModifier;

import java.util.List;

/**
 * 物品实例级属性覆盖的 CustomData 载体，经 CustomData API 持久化在物品上
 */
public record ItemAttributeModifiersData(List<ItemAttributeModifier> modifiers) {
}
