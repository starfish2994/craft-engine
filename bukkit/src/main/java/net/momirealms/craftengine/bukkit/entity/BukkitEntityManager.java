package net.momirealms.craftengine.bukkit.entity;

import net.momirealms.craftengine.bukkit.entity.listener.BukkitEntityListener;
import net.momirealms.craftengine.bukkit.entity.listener.PaperEntityListener;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.entity.AbstractEntityManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public final class BukkitEntityManager extends AbstractEntityManager {
    private final BukkitEntityListener entityListener;
    private final PaperEntityListener paperEntityListener;

    public BukkitEntityManager(BukkitCraftEngine plugin) {
        super(plugin);
        this.entityListener = new BukkitEntityListener(this);
        this.paperEntityListener = VersionHelper.hasPaperPatch ? new PaperEntityListener(this) : null;
    }

    @Override
    public void runDelayedSyncTasks() {
        boolean active = Config.enableAttributeSystem();
        this.entityListener.setActive(active);
        if (this.paperEntityListener != null) {
            this.paperEntityListener.setActive(active);
        }
    }

    @Override
    public void disable() {
        this.entityListener.setActive(false);
        if (this.paperEntityListener != null) {
            this.paperEntityListener.setActive(false);
        }
        super.disable();
    }


    @Override
    public List<Key> vanillaEntityIdsByTag(Key tag) {
        Tag<EntityType> bukkitTag = Bukkit.getTag(Tag.REGISTRY_ENTITY_TYPES, KeyUtils.toNamespacedKey(tag), EntityType.class);
        if (bukkitTag == null) return List.of();
        List<Key> result = new ArrayList<>();
        for (EntityType type : bukkitTag.getValues()) {
            result.add(KeyUtils.namespacedKeyToKey(type.getKey()));
        }
        return result;
    }
}
