package net.momirealms.craftengine.core.entity.hologram;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface DamageIndicatorFactory<T extends DamageIndicator> {

    T create(ConfigSection args);
}
