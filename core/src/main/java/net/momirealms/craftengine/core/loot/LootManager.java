package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.Optional;

public interface LootManager extends Manageable {

    ConfigParser vanillaParser();

    ConfigParser lootTableParser();

    Optional<VanillaLoot> getBlockLoot(int blockState);

    Optional<VanillaLoot> getEntityLoot(Key entity);

    Optional<Lootable> getLootable(Key key);

    LootTableReference createReference(Key key);
}
