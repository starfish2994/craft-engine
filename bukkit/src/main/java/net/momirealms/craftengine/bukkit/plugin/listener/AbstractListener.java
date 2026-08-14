package net.momirealms.craftengine.bukkit.plugin.listener;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class AbstractListener implements Listener {
    private boolean registered;

    protected AbstractListener() {
    }

    public void register() {
        if (!this.registered) {
            Bukkit.getPluginManager().registerEvents(this, BukkitCraftEngine.instance().javaPlugin());
            this.registered = true;
        }
    }

    public void unregister() {
        if (this.registered) {
            HandlerList.unregisterAll(this);
            this.registered = false;
        }
    }

    public void setActive(boolean active) {
        if (active == this.registered) return;
        if (active) {
            register();
        } else {
            unregister();
        }
    }
}
