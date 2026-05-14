package net.momirealms.craftengine.bukkit.plugin.scheduler;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.BukkitExecutor;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.FoliaExecutor;
import net.momirealms.craftengine.core.plugin.scheduler.AbstractJavaScheduler;
import net.momirealms.craftengine.core.plugin.scheduler.RegionExecutor;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.World;

public final class BukkitSchedulerAdapter extends AbstractJavaScheduler<World> {
    private final RegionExecutor<World> sync;

    public BukkitSchedulerAdapter(BukkitCraftEngine plugin) {
        super(plugin);
        if (VersionHelper.isFolia) {
            this.sync = new FoliaExecutor(plugin);
        } else {
            this.sync = new BukkitExecutor(plugin);
        }
    }

    @Override
    public RegionExecutor<World> sync() {
        return this.sync;
    }
}
