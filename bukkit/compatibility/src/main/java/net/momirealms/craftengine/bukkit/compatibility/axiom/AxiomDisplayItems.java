package net.momirealms.craftengine.bukkit.compatibility.axiom;

import com.moulberry.axiom.paperapi.AxiomAlreadyRegisteredException;
import com.moulberry.axiom.paperapi.AxiomCustomDisplayAPI;
import com.moulberry.axiom.paperapi.display.AxiomCustomDisplayBuilder;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class AxiomDisplayItems {
    private static AxiomDisplayItems instance;
    private final BukkitCraftEngine plugin;

    private AxiomDisplayItems(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(new ReloadListener(), plugin.javaPlugin());
    }

    public static AxiomDisplayItems init(BukkitCraftEngine plugin) {
        if (instance == null) {
            instance = new AxiomDisplayItems(plugin);
        }
        return instance;
    }

    public static AxiomDisplayItems instance() {
        return instance;
    }

    void registerAllItems() {
        AxiomCustomDisplayAPI.getAPI().unregisterAll(this.plugin.javaPlugin());

        Map<Key, ItemDefinition> items = CraftEngineItems.loadedItems();
        if (items.isEmpty()) {
            return;
        }

        int successCount = 0;
        for (Map.Entry<Key, ItemDefinition> entry : items.entrySet()) {
            try {
                this.registerItem(entry.getKey(), entry.getValue());
                successCount++;
            } catch (Throwable t) {
                this.plugin.logger().warn("Failed to register Axiom display item " + entry.getKey(), t);
            }
        }

        if (successCount > 0) {
            this.plugin.logger().info("Registered " + successCount + " display items with Axiom");
        }
    }

    private void registerItem(Key itemId, ItemDefinition def) {
        if (def.isVanillaItem()) {
            return;
        }

        BukkitItem item;
        try {
            item = (BukkitItem) def.buildItem(ItemBuildContext.empty()).toClientSide(null);
        } catch (Throwable t) {
            return;
        }
        ItemStack stack = item.getBukkitItem();
        if (stack == null || stack.getType().isAir()) {
            return;
        }

        Object adventureKey = KeyUtils.toAdventureKeyNoRelocation(itemId);
        AxiomCustomDisplayBuilder builder = AxiomCustomDisplayAPIProxy.INSTANCE.create(AxiomCustomDisplayAPI.getAPI(), adventureKey, def.translationKey(), stack);
        try {
            AxiomCustomDisplayAPI.getAPI().register(this.plugin.javaPlugin(), builder);
        } catch (AxiomAlreadyRegisteredException e) {
            this.plugin.logger().warn("Display item already registered: " + itemId);
        }
    }

    private class ReloadListener implements Listener {

        @EventHandler
        public void onCraftEngineReload(CraftEngineReloadEvent event) {
            registerAllItems();
        }
    }
}
