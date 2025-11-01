package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.collision.AABB;

public record HitBoxPart(int entityId, AABB aabb, Vec3d pos) {
}
