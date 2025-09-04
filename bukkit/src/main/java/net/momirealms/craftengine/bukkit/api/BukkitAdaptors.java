package net.momirealms.craftengine.bukkit.api;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class BukkitAdaptors {

    private BukkitAdaptors() {}

    public static BukkitServerPlayer adapt(final Player player) {
        return BukkitCraftEngine.instance().adapt(player);
    }

    public static BukkitWorld adapt(final World world) {
        return new BukkitWorld(world);
    }

    public static BukkitEntity adapt(final Entity entity) {
        return new BukkitEntity(entity);
    }

    public static BukkitExistingBlock adapt(final Block block) {
        return new BukkitExistingBlock(block);
    }

    public static Location toLocation(WorldPosition position) {
        return LocationUtils.toLocation(position);
    }
}
