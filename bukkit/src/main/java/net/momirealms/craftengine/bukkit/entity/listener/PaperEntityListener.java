package net.momirealms.craftengine.bukkit.entity.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.entity.BukkitEntityManager;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.plugin.listener.AbstractListener;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.EquipmentSlotUtils;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.attribute.equipment.EquipmentSetSlot;
import net.momirealms.craftengine.core.entity.LivingEntityHolder;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

public final class PaperEntityListener extends AbstractListener {
    private final BukkitEntityManager manager;

    public PaperEntityListener(BukkitEntityManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        LivingEntityHolder holder = this.manager.getEntityHolder(event.getEntity().getUniqueId());
        if (holder == null) return;
        Map<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> changes = event.getEquipmentChanges();
        Map<EquipmentSetSlot, BukkitItem> replacements = new HashMap<>();
        for (Map.Entry<EquipmentSlot, EntityEquipmentChangedEvent.EquipmentChange> entry : changes.entrySet()) {
            BukkitItem item = ItemStackUtils.wrap(entry.getValue().newItem());
            replacements.put(EquipmentSlotUtils.toEquipmentSetSlot(entry.getKey()), item);
        }
        holder.applyEquipmentChanges(replacements);
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
