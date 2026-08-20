package net.momirealms.craftengine.core.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.entity.seat.SeatOwner;
import net.momirealms.craftengine.core.world.EntityHitResult;
import net.momirealms.craftengine.core.world.Vec3d;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface FurnitureHitBox {

    @SuppressWarnings("unchecked")
    default Seat<SeatOwner>[] seats() {
        return new Seat[0];
    }

    default List<Collider> colliders() {
        return List.of();
    }

    List<FurnitureHitboxPart> parts();

    void show(Player player);

    void hide(Player player);

    void collectInteractableEntityId(Consumer<Integer> collector);

    default boolean canUseItemOn() {
        return false;
    }

    default Optional<EntityHitResult> clip(Vec3d min, Vec3d max) {
        for (FurnitureHitboxPart value : parts()) {
            Optional<EntityHitResult> clip = value.aabb().clip(min, max);
            if (clip.isPresent()) {
                return clip;
            }
        }
        return Optional.empty();
    }
}
