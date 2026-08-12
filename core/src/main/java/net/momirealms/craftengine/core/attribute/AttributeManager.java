package net.momirealms.craftengine.core.attribute;
import net.momirealms.craftengine.core.attribute.formula.*;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
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

    @Nullable
    AttributeContainer getContainer(UUID uuid);

    void removeContainer(UUID uuid);

    List<AttributeOperation> sortedOperations();

    DamageFormula findFormula(DamageEvent event);

    ConfigParser[] parsers();

    void processDamageEvent(DamageEvent event);

    double vanillaAttributeDefaultBaseValue(Key entityType, Key attribute, double fallback);
}
