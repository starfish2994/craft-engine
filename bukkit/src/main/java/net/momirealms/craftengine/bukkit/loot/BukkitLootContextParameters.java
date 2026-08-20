package net.momirealms.craftengine.bukkit.loot;

import net.momirealms.craftengine.core.plugin.context.ContextKey;

public final class BukkitLootContextParameters {
    private BukkitLootContextParameters() {}

    public static final ContextKey<Object> DAMAGE_SOURCE = ContextKey.direct("damage_source");
}
