package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.setting.EntitySettings;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.*;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStage;
import net.momirealms.craftengine.core.plugin.config.lifecycle.LoadingStages;
import net.momirealms.craftengine.core.util.ConcurrentChainedUUID2ReferenceHashTable;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.SwapList;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractEntityManager implements EntityManager {
    protected final CraftEngine plugin;
    protected final Map<Key, List<Key>> customEntityTags = new HashMap<>();
    protected final Map<Key, EntityDefinition> entityDefinitions = new ConcurrentHashMap<>();
    protected final ConcurrentChainedUUID2ReferenceHashTable<LivingEntityHolder> livingEntities = ConcurrentChainedUUID2ReferenceHashTable.createWithCapacity(512);
    protected final SwapList<LivingEntityHolder> tickingEntities = new SwapList<>();
    private final EntityParser entityParser = new EntityParser();
    private int livingEntityTick;

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

    public LivingEntityHolder trackLivingEntity(LivingEntity entity) {
        LivingEntityHolder holder = new LivingEntityHolder(entity);
        if (Config.enableEntityTick() || entity instanceof Player) {
            if (VersionHelper.hasFoliaPatch) {

            } else {
                this.tickingEntities.add(holder);
            }
        }
        return this.livingEntities.put(entity.uuid(), holder);
    }

    public void untrackLivingEntity(UUID uuid, boolean death) {
        LivingEntityHolder removed = this.livingEntities.remove(uuid);
        if (removed != null) {
            if (!VersionHelper.hasFoliaPatch) {
                this.tickingEntities.swapRemove(removed);
            }
            removed.close(death);
        }
    }

    public void tickLivingEntities() {
        int tick = ++this.livingEntityTick;
        SwapList<LivingEntityHolder> holders = this.tickingEntities;
        boolean tickAttribute = Config.enableEntityTick();
        for (int i = 0, size = holders.size(); i < size; i++) {
            holders.get(i).tick(tick, tickAttribute);
        }
    }

    @Override
    public void disable() {
        unload();
    }

    @Override
    public LivingEntityHolder getEntityHolder(UUID uuid) {
        return this.livingEntities.get(uuid);
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
