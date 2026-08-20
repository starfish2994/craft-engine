package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.source.LootOutcome;
import net.momirealms.craftengine.core.loot.source.LootSource;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.Manageable;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface LootManager extends Manageable {

    ConfigParser[] parsers();

    Optional<Loot> getLoot(Key key);

    LootTableReference createReference(Key key);

    static LootOutcome eval(List<LootSource> sources, LootContext context) {
        if (sources.isEmpty()) return LootOutcome.EMPTY;
        boolean matched = false;
        boolean overwriteItems = false;
        boolean overwriteExperience = false;
        List<Item> items = new ArrayList<>();
        for (LootSource source : sources) {
            try {
                if (!source.matches(context)) continue;
            } catch (Throwable t) {
                CraftEngine.instance().logger().warn("Failed to test loot source conditions for type '" + source.type().id() + "'", t);
                continue;
            }
            matched = true;
            if (source.overwriteItems()) overwriteItems = true;
            if (source.overwriteExperience()) overwriteExperience = true;
            try {
                items.addAll(source.loot().getRandomItems(context));
            } catch (Throwable t) {
                CraftEngine.instance().logger().warn("Failed to generate loot for source type '" + source.type().id() + "'", t);
            }
        }
        if (!matched) return LootOutcome.EMPTY;
        return new LootOutcome(true, overwriteItems, overwriteExperience, items);
    }
}
