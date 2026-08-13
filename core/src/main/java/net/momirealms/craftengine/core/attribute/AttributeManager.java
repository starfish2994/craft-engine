package net.momirealms.craftengine.core.attribute;
import net.momirealms.craftengine.core.attribute.formula.*;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.item.setting.value.AttributeModifiers;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributeManager extends Manageable {
    String META_KEY = "ce:attr";

    Optional<Attribute> getAttribute(Key id);

    Collection<Attribute> getAttributes();

    List<Attribute> attributesByEntityType(Key entityType);

    double getAttributeValue(Entity entity, Attribute attribute);

    // 指定物品作为"使用中的武器"时对属性的贡献（weapon 作用域修饰符现算，忽略槽位）
    default double getWeaponAttributeValue(@Nullable Item weapon, Attribute attribute, Context context) {
        if (weapon == null || weapon.isEmpty()) return 0;
        Optional<ItemDefinition> definition = weapon.getDefinition();
        if (definition.isEmpty()) return 0;
        AttributeModifiers modifiers = definition.get().settings().attributeModifiers();
        if (modifiers == null) return 0;
        return modifiers.weaponValue(attribute, context);
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
