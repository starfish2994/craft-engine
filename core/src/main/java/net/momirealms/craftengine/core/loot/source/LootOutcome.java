package net.momirealms.craftengine.core.loot.source;

import net.momirealms.craftengine.core.item.Item;

import java.util.List;

public record LootOutcome(boolean matched, boolean overwriteItems, boolean overwriteExperience, List<Item> items) {
    public static final LootOutcome EMPTY = new LootOutcome(false, false, false, List.of());
}
