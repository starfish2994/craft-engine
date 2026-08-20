package net.momirealms.craftengine.core.entity.setting;

import net.momirealms.craftengine.core.plugin.config.ConfigValue;

public interface EntitySettingsModifierFactory<M extends EntitySettingsModifier> {

    M create(ConfigValue value);
}
