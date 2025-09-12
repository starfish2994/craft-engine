package net.momirealms.craftengine.bukkit.block.entity;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.LegacyAttributeUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("DuplicatedCode")
public class SeatBlockEntity extends BlockEntity {
    private final Map<Entity, Player> seatEntities = new Reference2ObjectArrayMap<>(1);

    public SeatBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BukkitBlockEntityTypes.SEAT, pos, blockState);
    }

    public Map<Entity, Player> seatEntities() {
        return this.seatEntities;
    }

    public static void tick(CEWorld world, BlockPos pos, ImmutableBlockState state, SeatBlockEntity seat) {
        if (seat.seatEntities.isEmpty()) return;
        for (Map.Entry<Entity, Player> entry : seat.seatEntities.entrySet()) {
            Entity entity = entry.getKey();
            if (!entity.getPassengers().isEmpty()) continue;
            Player player = entry.getValue();
            seat.tryLeavingSeat(player, entity);
            seat.seatEntities.remove(entity);
        }
    }

    @Override
    public void preRemove() {
        if (this.seatEntities.isEmpty()) return;
        for (Map.Entry<Entity, Player> entry : this.seatEntities.entrySet()) {
            Entity entity = entry.getKey();
            entity.remove();
            this.seatEntities.remove(entity);
        }
        this.seatEntities.clear();
    }

    public void spawnSeatEntityForPlayer(@NotNull Player player, @NotNull Vector3f offset, float yaw, boolean limitPlayerRotation) {
        if (!this.seatEntities.isEmpty() || !this.isValid()) return;
        Location location = calculateSeatLocation(player, this.pos, this.blockState, offset, yaw);
        Entity seatEntity = limitPlayerRotation ?
                EntityUtils.spawnEntity(player.getWorld(),
                        VersionHelper.isOrAbove1_20_2() ? location.subtract(0, 0.9875, 0) : location.subtract(0, 0.990625, 0),
                        EntityType.ARMOR_STAND,
                        entity -> {
                            ArmorStand armorStand = (ArmorStand) entity;
                            if (VersionHelper.isOrAbove1_21_3()) {
                                Objects.requireNonNull(armorStand.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(0.01);
                            } else {
                                LegacyAttributeUtils.setMaxHealth(armorStand);
                            }
                            armorStand.setSmall(true);
                            armorStand.setInvisible(true);
                            armorStand.setSilent(true);
                            armorStand.setInvulnerable(true);
                            armorStand.setArms(false);
                            armorStand.setCanTick(false);
                            armorStand.setAI(false);
                            armorStand.setGravity(false);
                            armorStand.setPersistent(false);
                        }) :
                EntityUtils.spawnEntity(player.getWorld(),
                        VersionHelper.isOrAbove1_20_2() ? location : location.subtract(0, 0.25, 0),
                        EntityType.ITEM_DISPLAY,
                        entity -> {
                            ItemDisplay itemDisplay = (ItemDisplay) entity;
                            itemDisplay.setPersistent(false);
                        });
        if (!seatEntity.addPassenger(player)) {
            seatEntity.remove();
            return;
        }
        this.seatEntities.put(seatEntity, player);
    }

    private Location calculateSeatLocation(Player player, BlockPos pos, ImmutableBlockState state, Vector3f offset, float yaw) {
        Location location = new Location(player.getWorld(), pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5);
        for (Property<?> property : state.getProperties()) {
            if (property.name().equals("facing") && property.valueClass() == HorizontalDirection.class) {
                switch ((HorizontalDirection) state.get(property)) {
                    case NORTH -> location.setYaw(0);
                    case SOUTH -> location.setYaw(180);
                    case WEST -> location.setYaw(270);
                    case EAST -> location.setYaw(90);
                }
                break;
            }
            if (property.name().equals("facing_clockwise") && property.valueClass() == HorizontalDirection.class) {
                switch ((HorizontalDirection) state.get(property)) {
                    case NORTH -> location.setYaw(90);
                    case SOUTH -> location.setYaw(270);
                    case WEST -> location.setYaw(0);
                    case EAST -> location.setYaw(180);
                }
                break;
            }
        }
        Vector3f newOffset = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - location.getYaw()), 0)
                .conjugate()
                .transform(new Vector3f(offset));
        double newYaw = yaw + location.getYaw();
        if (newYaw < -180) newYaw += 360;
        Location newLocation = location.clone();
        newLocation.setYaw((float) newYaw);
        newLocation.add(newOffset.x, newOffset.y + 0.6, -newOffset.z);
        return newLocation;
    }

    private void tryLeavingSeat(@NotNull Player player, @NotNull Entity vehicle) {
        vehicle.remove();
        if (player.getVehicle() != null) return;
        Location vehicleLocation = vehicle.getLocation();
        Location originalLocation = vehicleLocation.clone();
        originalLocation.setY(this.pos.y());
        Location targetLocation = originalLocation.clone().add(vehicleLocation.getDirection().multiply(1.1));
        if (!isSafeLocation(targetLocation)) {
            targetLocation = findSafeLocationNearby(originalLocation);
            if (targetLocation == null) return;
        }
        targetLocation.setYaw(player.getLocation().getYaw());
        targetLocation.setPitch(player.getLocation().getPitch());
        if (VersionHelper.isFolia()) {
            player.teleportAsync(targetLocation);
        } else {
            player.teleport(targetLocation);
        }
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (!world.getBlockAt(x, y - 1, z).getType().isSolid()) return false;
        if (!world.getBlockAt(x, y, z).isPassable()) return false;
        return world.getBlockAt(x, y + 1, z).isPassable();
    }

    @Nullable
    private Location findSafeLocationNearby(Location center) {
        World world = center.getWorld();
        if (world == null) return null;
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int x = centerX + dx;
                int z = centerZ + dz;
                Location nearbyLocation = new Location(world, x + 0.5, centerY, z + 0.5);
                if (isSafeLocation(nearbyLocation)) return nearbyLocation;
            }
        }
        return null;
    }
}
