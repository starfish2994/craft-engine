package net.momirealms.craftengine.core.loot.source;

import net.momirealms.craftengine.core.loot.Loot;
import net.momirealms.craftengine.core.loot.LootContext;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class TargetedLootSource extends LootSource {
    private final List<Key> targets;

    public TargetedLootSource(LootSourceType<?> type, List<Key> targets, Predicate<LootContext> condition, Loot loot, EnumSet<OverwriteMode> overwrite) {
        super(type, condition, loot, overwrite);
        this.targets = targets;
    }

    @Override
    public List<Key> targets() {
        return this.targets;
    }

    public static final class Factory implements LootSourceFactory<TargetedLootSource> {

        @Override
        public TargetedLootSource create(LootSourceType<?> type, Key id, ConfigSection section) {
            return new TargetedLootSource(type, parseTargets(section), parseConditions(section), parseLoot(section), parseOverwrite(section));
        }
    }
}
