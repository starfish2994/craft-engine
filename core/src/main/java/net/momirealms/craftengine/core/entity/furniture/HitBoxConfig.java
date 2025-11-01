package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface HitBoxConfig {

    Key type();

    void initPacketsAndColliders(int[] entityId, WorldPosition position, Quaternionf conjugated,
                                 BiConsumer<Object, Boolean> packets, Consumer<Collider> collider, Consumer<HitBoxPart> aabb);

    void initShapeForPlacement(double x, double y, double z, float yaw, Quaternionf conjugated, Consumer<AABB> aabbs);

    int[] acquireEntityIds(Supplier<Integer> entityIdSupplier);

    SeatConfig[] seats();

    Vector3f position();

    boolean blocksBuilding();

    boolean canBeHitByProjectile();

    boolean canUseItemOn();
}
