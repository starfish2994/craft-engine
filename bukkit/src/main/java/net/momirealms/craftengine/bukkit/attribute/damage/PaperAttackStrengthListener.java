package net.momirealms.craftengine.bukkit.attribute.damage;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.listener.AbstractListener;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.proxy.minecraft.world.entity.player.PlayerProxy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public final class PaperAttackStrengthListener extends AbstractListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPreAttack(PrePlayerAttackEntityEvent event) {
        if (!event.willAttack()) return;
        BukkitServerPlayer player = BukkitAdaptor.adapt(event.getPlayer());
        if (player == null) return;
        Object minecraftPlayer = player.minecraftPlayer();
        if (minecraftPlayer == null) return;
        float strength = PlayerProxy.INSTANCE.getAttackStrengthScale(minecraftPlayer, 0.5F);
        player.captureAttackStrength(strength);
    }
}
