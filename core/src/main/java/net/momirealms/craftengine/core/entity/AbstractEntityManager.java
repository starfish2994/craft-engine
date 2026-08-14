package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public abstract class AbstractEntityManager implements EntityManager {
    protected final CraftEngine plugin;
    protected final Map<Key, List<Key>> customEntityTags = new HashMap<>();
    private final EntityParser entityParser = new EntityParser();

    protected AbstractEntityManager(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        this.customEntityTags.clear();
    }

    @Override
    public List<Key> customEntityIdsByTag(Key tag) {
        return Collections.unmodifiableList(this.customEntityTags.getOrDefault(tag, List.of()));
    }

    public void registerCustomEntityTag(Key tag, Key entityId) {
        this.customEntityTags.computeIfAbsent(tag, k -> new ArrayList<>()).add(entityId);
    }

    @Override
    public ConfigParser[] parsers() {
        return new ConfigParser[]{this.entityParser};
    }

    private final class EntityParser extends IdSectionConfigParser {
        public static final String[] CONFIG_SECTION_NAME = ConfigKeys.of("entit(y|ies)");

        @Override
        public Key type() {
            return Key.ce("entity");
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public LoadingStage loadingStage() {
            return LoadingStages.ENTITY;
        }

        @Override
        public int count() {
            return AbstractEntityManager.this.customEntityTags.size();
        }

        @Override
        public boolean async() {
            return Config.multiThreadedConfigLoad();
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            for (String tag : section.getStringList("tags")) {
                if (tag.isEmpty()) continue;
                AbstractEntityManager.this.registerCustomEntityTag(Key.of(tag), id);
            }
        }
    }
}
