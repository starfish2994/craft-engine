package net.momirealms.craftengine.bukkit.attribute.damage;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.attribute.BukkitAttributeManager;
import net.momirealms.craftengine.bukkit.plugin.listener.AbstractListener;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.attribute.damage.DamageIndicator;
import net.momirealms.craftengine.core.attribute.damage.DamageVisibility;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class EntityDamageListener extends AbstractListener {
    private final BukkitAttributeManager manager;

    public EntityDamageListener(BukkitAttributeManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        BukkitDamageEvent damageEvent = new BukkitDamageEvent(this.manager, event);
        if (Config.enableAttributeSystem()) {
            this.manager.processDamageEvent(damageEvent);
        }
        if (Config.enableDamageIndicator()
                && event instanceof EntityDamageByEntityEvent e
                && event.getEntity() instanceof LivingEntity victim) {
            Player attacker;
            Entity damager = e.getDamager();
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

            ContextHolder.Builder contextBuilder = ContextHolder.builder()
                    .withParameter(DirectContextParameters.ORIGINAL_DAMAGE, event.getDamage())
                    .withParameter(DirectContextParameters.DAMAGE, event.getFinalDamage());
            for (Map.Entry<String, Double> part : damageEvent.damageParts().entrySet()) {
                contextBuilder.withParameter(ContextKey.direct(part.getKey()), part.getValue());
            }
            PlayerOptionalContext context = PlayerOptionalContext.of(attackerUser, contextBuilder);
            net.momirealms.craftengine.core.entity.Entity coreVictim = BukkitAdaptor.adapt(victim);
            List<net.momirealms.craftengine.core.entity.player.Player> coreViewers = List.copyOf(viewers);
            for (DamageIndicator scheme : Config.damageIndicatorSchemes()) {
                scheme.display(attackerUser, coreVictim, coreViewers, context);
            }
        }
    }
}
