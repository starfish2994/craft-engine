package net.momirealms.craftengine.bukkit.plugin.scheduler;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.AbstractBukkitExecutor;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.BukkitExecutor;
import net.momirealms.craftengine.bukkit.plugin.scheduler.impl.FoliaExecutor;
import net.momirealms.craftengine.core.plugin.scheduler.AbstractJavaScheduler;
import net.momirealms.craftengine.core.util.VersionHelper;


public final class BukkitSchedulerAdapter extends AbstractJavaScheduler {
    private final BukkitCraftEngine plugin;
    private final AbstractBukkitExecutor sync;

    public BukkitSchedulerAdapter(BukkitCraftEngine plugin) {
        super(plugin);
        this.plugin = plugin;
        if (VersionHelper.hasFoliaPatch) {
            this.sync = new FoliaExecutor(plugin);
        } else {
            this.sync = new BukkitExecutor(plugin);
        }
    }

    @Override
    public AbstractBukkitExecutor platform() {
        return this.sync;
    }
}
