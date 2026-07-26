package net.momirealms.craftengine.bukkit.loot;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.loot.source.*;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.loot.AbstractLootManager;
import net.momirealms.craftengine.core.loot.Loot;
import net.momirealms.craftengine.core.loot.LootTableReference;
import net.momirealms.craftengine.core.loot.source.LootSourceType;
import net.momirealms.craftengine.core.loot.source.LootSources;
import net.momirealms.craftengine.core.plugin.compatibility.EntityProvider;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigParser;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.StringUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.function.Supplier;

public final class BukkitLootManager extends AbstractLootManager {
    private static BukkitLootManager instance;
    private final BukkitCraftEngine plugin;
    private final Map<LootSourceType<?>, List<Supplier<Listener>>> listenerFactories = new HashMap<>();
    private final Map<LootSourceType<?>, List<Listener>> activeListeners = new HashMap<>();
    private EntityProvider[] entitySources;

    public BukkitLootManager(BukkitCraftEngine plugin) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = this;
        this.plugin = plugin;
        this.registerSourceListener(LootSources.BLOCK_BREAK, BlockBreakLootListener::new);
        if (VersionHelper.hasPaperPatch) {
            this.registerSourceListener(LootSources.BLOCK_BREAK, PaperBlockBreakLootListener::new);
        }
        this.registerSourceListener(LootSources.ENTITY_DEATH, () -> new EntityDeathLootListener(this));
        this.registerSourceListener(LootSources.FISHING, FishingLootListener::new);
        this.registerSourceListener(LootSources.CONTAINER, ContainerLootListener::new);
        this.registerSourceListener(LootSources.PIGLIN_BARTER, PiglinBarterLootListener::new);
        this.registerSourceListener(LootSources.ARCHAEOLOGY, ArchaeologyLootListener::new);
        this.registerSourceListener(LootSources.ENTITY_DROP, () -> new EntityDropLootListener(this));
        this.registerSourceListener(LootSources.HARVEST, HarvestBlockLootListener::new);
        this.registerSourceListener(LootSources.SHEAR_BLOCK, ShearBlockLootListener::new);
        this.registerSourceListener(LootSources.VAULT, VaultLootListener::new);
        this.registerSourceListener(LootSources.ADVANCEMENT, AdvancementLootListener::new);
    }

    public static BukkitLootManager instance() {
        return instance;
    }

    public void registerSourceListener(LootSourceType<?> lootSourceType, Supplier<Listener> factory) {
        this.listenerFactories.computeIfAbsent(lootSourceType, k -> new ArrayList<>()).add(factory);
    }

    @Override
    public void runDelayedSyncTasks() {
        for (Map.Entry<LootSourceType<?>, List<Supplier<Listener>>> entry : this.listenerFactories.entrySet()) {
            LootSourceType<?> type = entry.getKey();
            boolean hasSources = type.hasSources();
            List<Listener> active = this.activeListeners.get(type);
            if (hasSources && active == null) {
                List<Listener> listeners = new ArrayList<>(entry.getValue().size());
                for (Supplier<Listener> factory : entry.getValue()) {
                    Listener listener = factory.get();
                    Bukkit.getPluginManager().registerEvents(listener, this.plugin.javaPlugin());
                    listeners.add(listener);
                }
                this.activeListeners.put(type, listeners);
            } else if (!hasSources && active != null) {
                for (Listener listener : active) {
                    HandlerList.unregisterAll(listener);
                }
                this.activeListeners.remove(type);
            }
        }
    }

    @Override
    public void disable() {
        for (List<Listener> listeners : this.activeListeners.values()) {
            for (Listener listener : listeners) {
                HandlerList.unregisterAll(listener);
            }
        }
        this.activeListeners.clear();
    }

    @Override
    public void delayedLoad() {
        super.delayedLoad();
        List<EntityProvider> entityProviders = new ArrayList<>();
        for (String source : Config.lootEntitySources()) {
            Optional.ofNullable(this.plugin.compatibilityManager().getEntityProvider(source)).ifPresent(entityProviders::add);
        }
        this.entitySources = entityProviders.toArray(new EntityProvider[0]);
    }

    public Key getEntityId(BukkitEntity bukkitEntity) {
        if (this.entitySources != null && this.entitySources.length > 0) {
            for (EntityProvider entityProvider : this.entitySources) {
                String entityId = entityProvider.getEntityId(bukkitEntity);
                if (entityId != null) {
                    return Key.of(entityProvider.plugin(), StringUtils.normalizeString(entityId));
                }
            }
        }
        return bukkitEntity.type();
    }

    @Override
    public ConfigParser[] parsers() {
        return new ConfigParser[] {this.lootParser, this.lootSourceParser};
    }

    @Override
    public LootTableReference createReference(Key key) {
        LazyReference<Loot> lazyReference = LazyReference.lazyReference(() -> {
            Optional<Loot> lootTable = BukkitLootManager.instance().getLoot(key);
            return lootTable.orElseGet(() -> new DatapackLootTable(key));
        });
        return new LootTableReference(lazyReference);
    }
}
