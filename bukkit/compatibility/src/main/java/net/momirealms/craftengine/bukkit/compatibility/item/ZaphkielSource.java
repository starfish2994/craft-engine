package net.momirealms.craftengine.bukkit.compatibility.item;

import ink.ptms.zaphkiel.Zaphkiel;
import net.momirealms.craftengine.core.item.ExternalItemSource;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author iiabc
 * @since 2025/8/30 09:39
 */
public class ZaphkielSource implements ExternalItemSource<ItemStack> {

    @Override
    public String plugin() {
        return "zaphkiel";
    }

    @Override
    public @Nullable ItemStack build(String id, ItemBuildContext context) {
        Player player = Optional.ofNullable(context.player()).map(it -> (Player) it.platformPlayer()).orElse(null);
        return Zaphkiel.INSTANCE.api().getItemManager().generateItemStack(id, player);
    }

    @Override
    public String id(ItemStack item) {
        return Zaphkiel.INSTANCE.api().getItemHandler().getItemId(item);
    }
}
