package net.momirealms.craftengine.bukkit.compatibility.item;

import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.momirealms.craftengine.core.item.ExternalItemSource;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class HeadDatabaseSource implements ExternalItemSource<ItemStack> {
    private HeadDatabaseAPI api;

    @Override
    public String plugin() {
        return "headdatabase";
    }

    @Nullable
    @Override
    public ItemStack build(String id, ItemBuildContext context) {
        if (api == null) {
            api = new HeadDatabaseAPI();
        }
        return api.getItemHead(id);
    }

    @Override
    public String id(ItemStack item) {
        if (api == null) {
            api = new HeadDatabaseAPI();
        }
        return api.getItemID(item);
    }
}
