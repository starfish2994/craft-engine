package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.entity.seat.BukkitSeat;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.HitBox;
import net.momirealms.craftengine.core.entity.furniture.HitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.HitBoxPart;
import net.momirealms.craftengine.core.entity.seat.Seat;
import net.momirealms.craftengine.core.entity.seat.SeatConfig;
import net.momirealms.craftengine.core.world.EntityHitResult;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.Optional;

public class BukkitHitBox implements HitBox {
    private final Furniture furniture;
    private final HitBoxConfig config;
    private final HitBoxPart[] parts;
    private final Seat<HitBox>[] seats;

    public BukkitHitBox(Furniture furniture, HitBoxConfig config, HitBoxPart[] parts) {
        this.parts = parts;
        this.config = config;
        this.furniture = furniture;
        this.seats = createSeats(config);
    }

    @SuppressWarnings("unchecked")
    private Seat<HitBox>[] createSeats(HitBoxConfig config) {
        SeatConfig[] seatConfigs = config.seats();
        Seat<HitBox>[] seats = new Seat[seatConfigs.length];
        for (int i = 0; i < seatConfigs.length; i++) {
            seats[i] = new BukkitSeat<>(this, seatConfigs[i]);
        }
        return seats;
    }

    @Override
    public HitBoxPart[] parts() {
        return this.parts;
    }

    @Override
    public HitBoxConfig config() {
        return this.config;
    }

    @Override
    public Seat<HitBox>[] seats() {
        return this.seats;
    }

    @Override
    public Optional<EntityHitResult> clip(Vec3d min, Vec3d max) {
        for (HitBoxPart hbe : this.parts) {
            Optional<EntityHitResult> result = hbe.aabb().clip(min, max);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveCustomData(CompoundTag data) {
        data.putString("type", "furniture");
        data.putInt("entity_id", this.furniture.baseEntityId());
    }
}
