package net.momirealms.craftengine.bukkit.util;

import org.bukkit.ExplosionResult;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

@SuppressWarnings("UnstableApiUsage")
public final class ExplosionUtils {
    private ExplosionUtils() {}

    public static boolean isDroppingItems(BlockExplodeEvent event) {
        return isDroppingItems(event.getExplosionResult());
    }

    public static boolean isDroppingItems(EntityExplodeEvent event) {
        return isDroppingItems(event.getExplosionResult());
    }

    private static boolean isDroppingItems(ExplosionResult result) {
        return result == ExplosionResult.DESTROY || result == ExplosionResult.DESTROY_WITH_DECAY;
    }

    public static float getRadius(float yield, ExplosionResult result) {
        return result == ExplosionResult.DESTROY_WITH_DECAY ? 1 / yield : 1f;
    }
}
