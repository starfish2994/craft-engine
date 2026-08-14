package net.momirealms.craftengine.core.entity.setting;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Registries;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class EntitySettingsModifiers {
    public static final EntitySettingsModifierType<EntitySettingsModifier> ATTRIBUTES = register(Key.ce("attributes"), value -> {
        ConfigSection section = value.getAsSection();
        Map<Key, Double> attributes = new LinkedHashMap<>();
        for (String attribute : section.keySet()) {
            attributes.put(Key.of(attribute), section.getDouble(attribute));
        }
        return settings -> settings.attributeValues(Map.copyOf(attributes));
    });
    public static final EntitySettingsModifierType<EntitySettingsModifier> TAGS = register(Key.ce("tags"), value -> {
        return settings -> settings.tags(Set.copyOf(value.getAsList(ConfigValue::getAsIdentifier)));
    });

    private EntitySettingsModifiers() {}

    public static void init() {
    }

    public static <M extends EntitySettingsModifier> EntitySettingsModifierType<M> register(Key id, EntitySettingsModifierFactory<M> factory) {
        EntitySettingsModifierType<M> type = new EntitySettingsModifierType<>(id, factory);
        ((WritableRegistry<EntitySettingsModifierType<? extends EntitySettingsModifier>>) BuiltInRegistries.ENTITY_SETTINGS_TYPE)
                .register(ResourceKey.create(Registries.ENTITY_SETTINGS_TYPE.location(), id), type);
        return type;
    }
}
