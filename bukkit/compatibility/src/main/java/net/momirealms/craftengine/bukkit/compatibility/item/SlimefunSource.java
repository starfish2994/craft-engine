package net.momirealms.craftengine.bukkit.compatibility.item;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import net.momirealms.craftengine.core.item.ExternalItemSource;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SlimefunSource implements ExternalItemSource<ItemStack> {

    @Override
    public String plugin() {
        return "slimefun";
    }

    @Nullable
    @Override
    public ItemStack build(String id, ItemBuildContext context) {
        return Optional.ofNullable(SlimefunItem.getById(id)).map(SlimefunItem::getItem).orElse(null);
    }

    @Override
    public String id(ItemStack item) {
        return Optional.ofNullable(SlimefunItem.getByItem(item)).map(SlimefunItem::getId).orElse(null);
    }
}
