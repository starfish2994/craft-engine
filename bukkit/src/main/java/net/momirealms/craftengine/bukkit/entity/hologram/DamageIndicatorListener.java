package net.momirealms.craftengine.bukkit.entity.hologram;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.entity.hologram.DamageVisibility;
import net.momirealms.craftengine.core.entity.hologram.ViewPointSelector;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.entity.CraftEntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.AABBProxy;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.text.DecimalFormat;
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
        String damageText = new DecimalFormat(Config.damageIndicatorNumberFormat()).format(damage);
        PlayerOptionalContext context = PlayerOptionalContext.of(attackerUser, ContextHolder.builder()
                .withParameter(ContextKey.direct("damage"), damageText));
        Component text = AdventureHelper.miniMessage().deserialize(
                Config.damageIndicatorText(),
                context.tagResolvers()
        );
        Object aabb = EntityProxy.INSTANCE.getBoundingBox(CraftEntityProxy.INSTANCE.getEntity(victim));
        double width = AABBProxy.INSTANCE.getMaxX(aabb) - AABBProxy.INSTANCE.getMinX(aabb);
        double depth = AABBProxy.INSTANCE.getMaxZ(aabb) - AABBProxy.INSTANCE.getMinZ(aabb);
        double radius = MiscUtils.sqrt((float) (width * width + depth * depth)) / 2;
        Vec3d eye = attackerUser.getEyePos();
        float yawRad = MiscUtils.toRadians(attackerUser.yRot());
        float pitchRad = MiscUtils.toRadians(attackerUser.xRot());
        ViewPointSelector.Point3D point = ViewPointSelector.findViewIntersection(
                new ViewPointSelector.Point3D(eye.x, eye.y, eye.z),
                new ViewPointSelector.Point3D(
                        -MiscUtils.sin(yawRad) * MiscUtils.cos(pitchRad),
                        -MiscUtils.sin(pitchRad),
                        MiscUtils.cos(yawRad) * MiscUtils.cos(pitchRad)
                ),
                new ViewPointSelector.Point3D(
                        (AABBProxy.INSTANCE.getMinX(aabb) + AABBProxy.INSTANCE.getMaxX(aabb)) / 2,
                        AABBProxy.INSTANCE.getMinY(aabb),
                        (AABBProxy.INSTANCE.getMinZ(aabb) + AABBProxy.INSTANCE.getMaxZ(aabb)) / 2
                ),
                radius,
                AABBProxy.INSTANCE.getMaxY(aabb) - AABBProxy.INSTANCE.getMinY(aabb),
                Config.damageIndicatorAngleSpread(),
                Config.damageIndicatorHeightSpread()
        );
        new DamageHologram(viewers, text, point.x(), point.y(), point.z()).start();
    }
}
