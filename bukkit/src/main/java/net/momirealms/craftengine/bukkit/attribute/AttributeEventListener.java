package net.momirealms.craftengine.bukkit.attribute;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.listener.AbstractListener;
import net.momirealms.craftengine.core.attribute.AttributeManager;
import net.momirealms.craftengine.core.attribute.EntityAttributesSnapshot;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

public final class AttributeEventListener extends AbstractListener {
    public static final String PROJECTILE_WEAPON = "ce:weapon";

    public AttributeEventListener() {
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onShootProjectile(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof LivingEntity livingEntity) {
            LivingEntityHolder holder = BukkitCraftEngine.instance().entityManager().getEntityHolder(livingEntity.getUniqueId());
            if (holder == null) {
                return;
            }
            holder.ifAttributesExist(attributes -> {
                EntityAttributesSnapshot snapshot = attributes.createSnapshot();
                projectile.setMetadata(AttributeManager.META_KEY, new FixedMetadataValue(BukkitCraftEngine.instance().javaPlugin(), snapshot));
            });
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (VersionHelper.isOrAbove1_21) return;
        ItemStack bow = event.getBow();
        if (bow == null) return;
        Entity projectile = event.getProjectile();
        projectile.setMetadata(PROJECTILE_WEAPON, new FixedMetadataValue(BukkitCraftEngine.instance().javaPlugin(), projectile));
    }
}
