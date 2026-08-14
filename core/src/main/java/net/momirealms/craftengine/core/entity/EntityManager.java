package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface EntityManager extends Manageable {

    ConfigParser[] parsers();

    List<Key> vanillaEntityIdsByTag(Key tag);

    List<Key> customEntityIdsByTag(Key tag);

    @Nullable
    EntityDefinition entityDefinition(Key entityType);

    default List<Key> entityIdsByTag(Key tag) {
        List<Key> entities = new ArrayList<>();
        entities.addAll(vanillaEntityIdsByTag(tag));
        entities.addAll(customEntityIdsByTag(tag));
        return entities;
    }
}
