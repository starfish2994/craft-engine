package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.ParticleBlockBehavior;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public class WallParticleBlockEntity extends BaseParticleBlockEntity {

    public WallParticleBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BukkitBlockEntityTypes.WALL_PARTICLE, pos, blockState);
    }

    @Override
    public void animateTick(ImmutableBlockState state, World level, BlockPos pos) {
        Direction direction = null;
        for (Property<?> property : state.getProperties()) {
            if (!property.name().equals("facing")) continue;
            if (property.valueClass() == Direction.class) {
                direction = (Direction) state.get(property);
                break;
            } else if (property.valueClass() == HorizontalDirection.class) {
                direction = ((HorizontalDirection) state.get(property)).toDirection();
                break;
            }
        }
        if (direction != null) {
            direction = direction.opposite();
        }
        for (ParticleBlockBehavior.ParticleData particle : behavior.particles()) {
            Vec3d location = particle.locationOffset().add(pos.x(), pos.y(), pos.z());
            if (direction != null) {
                location = location.add(0.27 * direction.stepX(), 0.22, 0.27 * direction.stepZ());
            }
            level.spawnParticle(
                    location,
                    particle.particle(),
                    particle.count(),
                    particle.offset().x(),
                    particle.offset().y(),
                    particle.offset().z(),
                    particle.speed(),
                    null, null
            );
        }
    }
}
