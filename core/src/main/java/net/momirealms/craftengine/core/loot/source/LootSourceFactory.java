package net.momirealms.craftengine.core.loot.source;

import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

@FunctionalInterface
public interface LootSourceFactory<T extends LootSource> {

    T create(LootSourceType<?> type, Key id, ConfigSection section);
}
