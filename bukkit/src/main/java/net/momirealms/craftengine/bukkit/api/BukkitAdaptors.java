package net.momirealms.craftengine.bukkit.api;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.item.Item;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true)
public final class BukkitAdaptors {
    private BukkitAdaptors() {}

    @Nullable
    public static BukkitServerPlayer adapt(final Player player) {
        return BukkitAdaptor.adapt(player);
    }

    @NotNull
    public static BukkitWorld adapt(@NotNull final World world) {
        return BukkitAdaptor.adapt(world);
    }

    @NotNull
    public static BukkitEntity adapt(@NotNull final Entity entity) {
        return new BukkitEntity(entity);
    }

    @NotNull
    public static BukkitExistingBlock adapt(@NotNull final Block block) {
        return new BukkitExistingBlock(block);
    }

    @NotNull
    public static Item adapt(@NotNull final ItemStack item) {
        return BukkitItemManager.instance().wrap(item);
    }
}
