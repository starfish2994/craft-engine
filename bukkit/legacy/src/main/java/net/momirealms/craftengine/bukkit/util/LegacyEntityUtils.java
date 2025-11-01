package net.momirealms.craftengine.bukkit.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.function.Consumer;

public final class LegacyEntityUtils {
    private LegacyEntityUtils() {}

    public static Entity spawnEntity(World world, Location loc, EntityType type, Consumer<Entity> function) {
        return world.spawnEntity(loc, type, CreatureSpawnEvent.SpawnReason.CUSTOM, function::accept);
    }
}
