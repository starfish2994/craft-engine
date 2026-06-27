package net.momirealms.craftengine.bukkit.compatibility.axiom;

import com.moulberry.axiom.AxiomPaper;
import com.moulberry.axiom.paperapi.AxiomCustomDisplayAPI;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import org.bukkit.entity.Player;

public final class AxiomIntegration {

    private AxiomIntegration() {
    }

    public static void init(BukkitCraftEngine plugin) {
        try {
            AxiomDisplayItems.init(plugin);
        } catch (Throwable t) {
            plugin.logger().warn("Failed to initialize Axiom integration", t);
        }
    }

    public static boolean isAxiomPlayer(Player player) {
        try {
            return AxiomPaper.PLUGIN != null && AxiomPaper.PLUGIN.canUseAxiom(player);
        } catch (Throwable t) {
            return false;
        }
    }
}
