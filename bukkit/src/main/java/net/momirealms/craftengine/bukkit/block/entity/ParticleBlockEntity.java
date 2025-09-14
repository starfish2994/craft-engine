package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.ParticleBlockBehavior;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public class ParticleBlockEntity extends BaseParticleBlockEntity {

    public ParticleBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BukkitBlockEntityTypes.PARTICLE, pos, blockState);
    }

    @Override
    public void animateTick(ImmutableBlockState state, World level, BlockPos pos) {
        for (ParticleBlockBehavior.ParticleData particle : behavior.particles()) {
            Vec3d location = particle.locationOffset().add(pos.x(), pos.y(), pos.z());
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
