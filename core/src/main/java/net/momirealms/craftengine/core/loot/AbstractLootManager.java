package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.loot.source.LootSource;
import net.momirealms.craftengine.core.loot.source.LootSourceType;
import net.momirealms.craftengine.core.loot.source.LootSources;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.IdSectionConfigParser;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLootManager implements LootManager {
    protected final LootSourceParser lootSourceParser = new LootSourceParser();
    protected final Map<Key, Loot> lootTables = new ConcurrentHashMap<>();
    protected final Map<LootSourceType<?>, List<LootSource>> loadingSources = new ConcurrentHashMap<>();
    protected final Set<LootSourceType<?>> knownTypes = ConcurrentHashMap.newKeySet();
    protected final LootParser lootParser = new LootParser();

    @Override
    public void unload() {
        this.loadingSources.clear();
        for (LootSourceType<?> type : this.knownTypes) {
            type.clearSources();
        }
    }

    @Override
    public void delayedLoad() {
        this.loadingSources.forEach(LootSourceType::updateSources);
        this.loadingSources.clear();
    }

    @Override
    public Optional<Loot> getLoot(Key key) {
        return Optional.ofNullable(this.lootTables.get(key));
    }

    protected void addLootSource(LootSource source) {
        this.knownTypes.add(source.type());
        this.loadingSources
                .computeIfAbsent(source.type(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(source);
    }

    protected final class LootParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = ConfigKeys.of("loot(s)");
        private int count;

        @Override
        public Key type() {
            return Key.ce("loot");
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.LOOT_TABLE;
        }

        @Override
        public int count() {
            return count;
        }

        @Override
        public boolean async() {
            return Config.multiThreadedConfigLoad();
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            Loot loot = section.toValue().getAsLoot();
            lootTables.put(id, loot);
            this.count++;
        }
    }

    protected final class LootSourceParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = ConfigKeys.of("loot_source(s)|vanilla_loot(s)");
        private int count;

        @Override
        public Key type() {
            return Key.ce("loot_source");
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.LOOT_SOURCE;
        }

        @Override
        public List<LoadingStage> dependencies() {
            return List.of(LoadingStages.LOOT_TABLE);
        }

        @Override
        public int count() {
            return this.count;
        }

        @Override
        public boolean async() {
            return Config.multiThreadedConfigLoad();
        }

        @Override
        public void preProcess() {
            this.count = 0;
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            addLootSource(LootSources.fromConfig(id, section));
            this.count++;
        }
    }
}
