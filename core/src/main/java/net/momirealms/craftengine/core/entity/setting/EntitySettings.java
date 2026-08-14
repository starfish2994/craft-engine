package net.momirealms.craftengine.core.entity.setting;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.plugin.config.KnownResourceException;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.ExceptionCollector;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class EntitySettings {
    Map<Key, Double> attributeValues = Map.of();
    Set<Key> tags = Set.of();

    private EntitySettings() {}

    public static EntitySettings of() {
        return new EntitySettings();
    }

    public static EntitySettings fromConfig(@Nullable ConfigSection section) {
        EntitySettings settings = EntitySettings.of();
        if (section == null) return settings;
        applyModifiers(settings, section);
        return settings;
    }

    public static void applyModifiers(EntitySettings settings, ConfigSection section) {
        ExceptionCollector<KnownResourceException> collector = new ExceptionCollector<>(KnownResourceException.class);
        for (String type : section.keySet()) {
            ConfigValue value = section.getValue(type);
            if (value == null) continue;
            String key = StringUtils.normalizeSettingsType(type);
            collector.runCatching(() -> {
                Optional.ofNullable(BuiltInRegistries.ENTITY_SETTINGS_TYPE.getValue(Key.ce(key)))
                        .ifPresent(modifierType ->
                                modifierType.factory().create(value).apply(settings));
            });
        }
        collector.throwIfPresent();
    }

    public Map<Key, Double> attributeValues() {
        return this.attributeValues;
    }

    @Nullable
    public Double attributeValue(Key attribute) {
        return this.attributeValues.get(attribute);
    }

    public EntitySettings attributeValues(Map<Key, Double> attributeValues) {
        this.attributeValues = attributeValues;
        return this;
    }

    // 实体归属的自定义 tag（供属性 entities 限制、伤害公式 target 等按 #tag 匹配）
    public Set<Key> tags() {
        return this.tags;
    }

    public EntitySettings tags(Set<Key> tags) {
        this.tags = tags;
        return this;
    }
}
