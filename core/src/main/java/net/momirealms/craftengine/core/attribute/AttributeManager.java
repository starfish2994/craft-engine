package net.momirealms.craftengine.core.attribute;

import net.momirealms.craftengine.core.attribute.formula.DamageEvent;
import net.momirealms.craftengine.core.attribute.formula.DamageFormula;
import net.momirealms.craftengine.core.attribute.modifier.ItemAttributeModifier;
import net.momirealms.craftengine.core.attribute.modifier.ItemAttributeModifierStore;
import net.momirealms.craftengine.core.attribute.modifier.ItemAttributeModifiersProvider;
import net.momirealms.craftengine.core.attribute.modifier.SlotAttributeModifierConfig;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.LivingEntity;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.setting.value.AttributeModifiers;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface AttributeManager extends Manageable {
    String META_KEY = "ce:attr";

    Optional<Attribute> getAttribute(Key id);

    Collection<Attribute> getAttributes();

    List<Attribute> attributesByEntityType(Key entityType);

    double getAttributeValue(Entity entity, Attribute attribute);

    List<SlotAttributeModifierConfig> getItemAttributeModifiers(Item item);

    void registerItemModifiersProvider(Key id, int priority, ItemAttributeModifiersProvider provider);

    void unregisterItemModifiersProvider(Key id);

    default List<ItemAttributeModifier> getPersistentItemAttributeModifiers(Item item) {
        return ItemAttributeModifierStore.read(item);
    }

    default void addPersistentItemAttributeModifier(Item item, ItemAttributeModifier modifier) {
        List<ItemAttributeModifier> modifiers = new ArrayList<>(ItemAttributeModifierStore.read(item));
        modifiers.removeIf(m -> m.id().equals(modifier.id()));
        modifiers.add(modifier);
        ItemAttributeModifierStore.write(item, modifiers);
    }

    default void removePersistentItemAttributeModifier(Item item, Key id) {
        List<ItemAttributeModifier> modifiers = new ArrayList<>(ItemAttributeModifierStore.read(item));
        if (modifiers.removeIf(m -> m.id().equals(id))) {
            ItemAttributeModifierStore.write(item, modifiers);
        }
    }

    default void clearPersistentItemAttributeModifiers(Item item) {
        ItemAttributeModifierStore.write(item, List.of());
    }

    default double getWeaponAttributeValue(@Nullable Item weapon, Attribute attribute, Context context) {
        if (weapon == null || weapon.isEmpty()) return 0;
        return AttributeModifiers.weaponValue(getItemAttributeModifiers(weapon), attribute, context);
    }

    default void refreshEquipments(LivingEntity entity) {
        AttributeContainer container = getContainer(entity.uuid());
        if (container != null) {
            container.refreshEquipments();
        }
    }

    @Nullable
    AttributeContainer getContainer(UUID uuid);

    void removeContainer(UUID uuid);

    Optional<AttributeOperation> getOperation(Key id);

    DamageFormula findFormula(DamageEvent event);

    ConfigParser[] parsers();

    void processDamageEvent(DamageEvent event);

    double vanillaAttributeDefaultBaseValue(Key entityType, Key attribute, double fallback);
}
