package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.BaseParticleBlockEntity;
import net.momirealms.craftengine.bukkit.block.entity.ParticleBlockEntity;
import net.momirealms.craftengine.bukkit.block.entity.WallParticleBlockEntity;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.Vec3d;

import java.util.List;
import java.util.Map;

public class ParticleBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final List<ParticleData> particles;
    private final boolean inWall;

    public ParticleBlockBehavior(CustomBlock customBlock, List<ParticleData> particles, boolean inWall) {
        super(customBlock);
        this.particles = particles;
        this.inWall = inWall;
    }

    public List<ParticleData> particles() {
        return particles;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType() {
        return EntityBlockBehavior.blockEntityTypeHelper(this.inWall ? BukkitBlockEntityTypes.WALL_PARTICLE : BukkitBlockEntityTypes.PARTICLE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return this.inWall ? new WallParticleBlockEntity(pos, state) : new ParticleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        if (this.particles().isEmpty()) return null;
        return EntityBlockBehavior.createTickerHelper(BaseParticleBlockEntity::tick);
    }

    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            List<ParticleData> particles = ResourceConfigUtils.parseConfigAsList(arguments.getOrDefault("particles", List.of()), ParticleData::fromMap);
            boolean inWall = ResourceConfigUtils.getAsBoolean(arguments.getOrDefault("in-wall", false), "in-wall");
            return new ParticleBlockBehavior(block, particles, inWall);
        }
    }

    public record ParticleData(Key particle, Vec3d locationOffset, Vec3d offset, int count, double speed) {

        public static ParticleData fromMap(Map<String, Object> arguments) {
            Key particle = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("type"), "warning.config.block.behavior.particle.missing_type"));
            Vec3d locationOffset = ResourceConfigUtils.getAsVec3d(arguments.get("location-offset"), "location-offset");
            Vec3d offset = ResourceConfigUtils.getAsVec3d(arguments.get("offset"), "offset");
            int count = ResourceConfigUtils.getAsInt(arguments.getOrDefault("count", 1), "count");
            double speed = ResourceConfigUtils.getAsDouble(arguments.get("speed"), "speed");
            return new ParticleData(particle, locationOffset, offset, count, speed);
        }
    }
}
