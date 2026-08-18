package net.momirealms.craftengine.bukkit.compatibility.protection;

import net.coreprotect.config.Config;
import net.coreprotect.listener.player.PlayerInteractEntityListener;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.compatibility.ProtectionLogger;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class CoreProtectProtectionLogger implements ProtectionLogger {
    private static final String CORE_PROTECT = "CoreProtect";

    public CoreProtectProtectionLogger() {
    }

    @Override
    public String plugin() {
        return CORE_PROTECT;
    }

    @Override
    public void logItemFrameTransaction(Player player,
                                        WorldPosition position,
                                        Direction direction,
                                        @Nullable Item oldItem,
                                        @Nullable Item newItem) {
        World world = (World) position.world().platformWorld();
        if (!Config.getConfig(world).ITEM_TRANSACTIONS) {
            return;
        }
        ItemStack[] oldState = new ItemStack[] {toBukkitItem(oldItem)};
        ItemStack[] newState = new ItemStack[] {toBukkitItem(newItem)};
        Object container = new Object[] {oldState, newState, DirectionUtils.toBlockFace(direction)};
        Location location = new Location(world, position.x(), position.y(), position.z());
        PlayerInteractEntityListener.queueContainerSpecifiedItems(
                player.name(),
                Material.ITEM_FRAME,
                container,
                location,
                false
        );
    }

    private static ItemStack toBukkitItem(@Nullable Item item) {
        if (item == null || item.isEmpty()) {
            return new ItemStack(Material.AIR);
        }
        return ItemStackUtils.getBukkitStack(item).clone();
    }
}
