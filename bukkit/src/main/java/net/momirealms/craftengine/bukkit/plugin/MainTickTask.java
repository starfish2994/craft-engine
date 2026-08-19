package net.momirealms.craftengine.bukkit.plugin;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;

public final class MainTickTask implements Runnable {
    private final BukkitCraftEngine plugin;

    MainTickTask(BukkitCraftEngine plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (BukkitServerPlayer serverPlayer : this.plugin.networkManager().onlineUsers()) {
            serverPlayer.tick();
        }
        if (Config.enableEntityTracking()) {
            this.plugin.entityManager().tickLivingEntities();
        }
    }
}
