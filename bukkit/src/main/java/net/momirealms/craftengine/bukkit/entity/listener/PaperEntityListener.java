package net.momirealms.craftengine.bukkit.entity.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.entity.BukkitEntityManager;
import net.momirealms.craftengine.bukkit.plugin.listener.AbstractListener;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public final class PaperEntityListener extends AbstractListener {
    private final BukkitEntityManager manager;

    public PaperEntityListener(BukkitEntityManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityAdd(EntityAddToWorldEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player) return;
            if (Config.shouldTrackEntity(EntityUtils.getEntityType(livingEntity))) {
                this.manager.trackLivingEntity((net.momirealms.craftengine.core.entity.LivingEntity) BukkitAdaptor.adapt(livingEntity));
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRemove(EntityRemoveFromWorldEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player) return;
            this.manager.untrackLivingEntity(livingEntity.getUniqueId(), false);
        }
    }
}
