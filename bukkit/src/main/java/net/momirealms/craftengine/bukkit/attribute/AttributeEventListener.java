package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.attribute.AttributeContainer;
import net.momirealms.craftengine.core.attribute.AttributeContainerSnapshot;
import net.momirealms.craftengine.core.attribute.AttributeManager;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

public final class AttributeEventListener implements Listener {
    private final BukkitAttributeManager manager;

    public AttributeEventListener(BukkitAttributeManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRemove(EntityRemoveEvent event) {
        if (!Config.enableAttributeSystem()) return;
        Entity entity = event.getEntity();
        this.manager.removeContainer(entity.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!Config.enableAttributeSystem()) return;
        BukkitDamageEvent damageEvent = new BukkitDamageEvent(this.manager, event);
        this.manager.processDamageEvent(damageEvent);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Config.enableAttributeSystem()) return;
        Player player = event.getPlayer();
        BukkitServerPlayer serverPlayer = BukkitAdaptor.adapt(player);
        if (serverPlayer == null) return;
        this.manager.getOrCreateContainer(serverPlayer);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!Config.enableAttributeSystem()) return;
        this.manager.removeContainer(event.getPlayer().getUniqueId());
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShootProjectile(ProjectileLaunchEvent event) {
        if (!Config.enableAttributeSystem()) return;
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof LivingEntity livingEntity) {
            AttributeContainer container = this.manager.getContainer(livingEntity.getUniqueId());
            if (container == null) {
                return;
            }
            AttributeContainerSnapshot snapshot = container.createSnapshot();
            projectile.setMetadata(AttributeManager.META_KEY, new FixedMetadataValue(BukkitCraftEngine.instance().javaPlugin(), snapshot));
        }
    }
}
