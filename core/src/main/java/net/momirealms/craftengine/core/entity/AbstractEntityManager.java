package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.entity.setting.EntitySettings;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractEntityManager implements EntityManager {
    protected final CraftEngine plugin;
    protected final Map<Key, List<Key>> customEntityTags = new HashMap<>();
    protected final Map<Key, EntityDefinition> entityDefinitions = new ConcurrentHashMap<>();
    private final EntityParser entityParser = new EntityParser();

    protected AbstractEntityManager(CraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        this.customEntityTags.clear();
        this.entityDefinitions.clear();
    }

    @Override
    public List<Key> customEntityIdsByTag(Key tag) {
        return Collections.unmodifiableList(this.customEntityTags.getOrDefault(tag, List.of()));
    }

    @Override
    @Nullable
    public EntityDefinition entityDefinition(Key entityType) {
        return this.entityDefinitions.get(entityType);
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
            return AbstractEntityManager.this.entityDefinitions.size();
        }

        @Override
        public boolean async() {
            return Config.multiThreadedConfigLoad();
        }

        @Override
        protected void parseSection(@NotNull Pack pack, @NotNull Path path, @NotNull Key id, @NotNull ConfigSection section) {
            AbstractEntityManager.this.entityDefinitions.put(id, new EntityDefinition(id, EntitySettings.fromConfig(section.getSection("settings"))));
        }

        @Override
        public void postProcess() {
            AbstractEntityManager.this.customEntityTags.clear();
            for (EntityDefinition definition : AbstractEntityManager.this.entityDefinitions.values()) {
                for (Key tag : definition.settings().tags()) {
                    AbstractEntityManager.this.registerCustomEntityTag(tag, definition.id());
                }
            }
        }
    }
}
