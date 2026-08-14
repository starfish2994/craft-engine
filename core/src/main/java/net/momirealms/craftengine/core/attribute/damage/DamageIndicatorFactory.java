package net.momirealms.craftengine.core.attribute.damage;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;

public interface DamageIndicatorFactory<T extends DamageIndicator> {

    T create(ConfigSection args);
}
