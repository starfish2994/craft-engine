package net.momirealms.craftengine.bukkit.compatibility.item;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import net.momirealms.craftengine.core.item.ExternalItemSource;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class NexoItemSource implements ExternalItemSource<ItemStack> {

    @Override
    public String plugin() {
        return "nexo";
    }

    @Nullable
    @Override
    public ItemStack build(String id, ItemBuildContext context) {
        ItemBuilder itemBuilder = NexoItems.itemFromId(id);
        if (itemBuilder == null) return null;
        return itemBuilder.build();
    }

    @Override
    public String id(ItemStack item) {
        return NexoItems.idFromItem(item);
    }
}
