package net.momirealms.craftengine.bukkit.compatibility.item;

import github.saukiya.sxitem.SXItem;
import net.momirealms.craftengine.core.item.ExternalItemSource;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SXItemSource implements ExternalItemSource<ItemStack> {

    @Override
    public String plugin() {
        return "sx-item";
    }

    @Nullable
    @Override
    public ItemStack build(String id, ItemBuildContext context) {
        return SXItem.getItemManager().getItem(id, Optional.ofNullable(context.player()).map(p -> (Player) p.platformPlayer()).orElse(null));
    }

    @Override
    public String id(ItemStack item) {
        return SXItem.getItemManager().getItemKey(item);
    }
}
