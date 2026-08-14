package net.momirealms.craftengine.bukkit.entity.hologram;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.entity.hologram.DamageIndicator;
import net.momirealms.craftengine.core.entity.hologram.DamageVisibility;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;

public final class DamageIndicatorListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!Config.enableDamageIndicator()) return;
        double damage = event.getFinalDamage();
        if (damage <= 0) return;
        Entity victim = event.getEntity();
        if (victim instanceof ArmorStand || victim instanceof ItemFrame) return;
        Player attacker;
        Entity damager = event.getDamager();
        if (damager instanceof Player player) {
            attacker = player;
        } else if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        } else {
            return;
        }
        BukkitServerPlayer attackerUser = BukkitAdaptor.adapt(attacker);
        if (attackerUser == null) return;
        List<BukkitServerPlayer> viewers = new ArrayList<>();
        if (attackerUser.damageVisibility() != DamageVisibility.NONE) {
            viewers.add(attackerUser);
        }
        for (BukkitServerPlayer user : EntityUtils.getTrackedBy(victim, BukkitAdaptor::adapt)) {
            if (user == null || user.damageVisibility() != DamageVisibility.ALL) continue;
            viewers.add(user);
        }
        if (viewers.isEmpty()) return;
        net.momirealms.craftengine.core.entity.Entity coreVictim = BukkitAdaptor.adapt(victim);
        List<net.momirealms.craftengine.core.entity.player.Player> coreViewers = List.copyOf(viewers);
        for (DamageIndicator scheme : Config.damageIndicatorSchemes()) {
            scheme.display(attackerUser, coreVictim, damage, coreViewers);
        }
    }
}
