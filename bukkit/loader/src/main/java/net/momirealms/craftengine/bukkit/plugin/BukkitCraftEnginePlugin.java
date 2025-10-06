package net.momirealms.craftengine.bukkit.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BukkitCraftEnginePlugin extends JavaPlugin {
    private final BukkitCraftEngine plugin;

    public BukkitCraftEnginePlugin() {
        this.plugin = new BukkitCraftEngine(this);
        this.plugin.applyDependencies();
        this.plugin.setUpConfigAndLocale();
    }

    @Override
    public void onLoad() {
        this.plugin.onPluginLoad();
    }

    @Override
    public void onEnable() {
        this.plugin.scheduler().asyncRepeating(() -> {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            if (players.size() > 30) {
                this.plugin.logger().warn("Glad to see that your server is growing!");
                this.plugin.logger().warn("The Community Edition supports up to 30 players. Unlock limitless potential with CraftEngine Premium:");
                this.plugin.logger().warn("► Unlimited player capacity");
                this.plugin.logger().warn("► Priority support");
                this.plugin.logger().warn("► Exclusive features");
            }
        }, 1, 1, TimeUnit.MINUTES);
        this.plugin.onPluginEnable();
        this.plugin.logger().warn("You're using the CraftEngine Community Edition.");
        this.plugin.logger().warn("This version is completely free for servers with up to 30 players.");
        this.plugin.logger().warn("Please consider purchasing the premium version to support CraftEngine's development.");
    }

    @Override
    public void onDisable() {
        this.plugin.onPluginDisable();
    }
}
