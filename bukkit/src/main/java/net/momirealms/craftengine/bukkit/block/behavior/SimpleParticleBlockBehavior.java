package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.SimpleParticleBlockEntity;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.tick.BlockEntityTicker;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.particle.ParticleConfig;

public final class SimpleParticleBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final BlockBehaviorFactory<SimpleParticleBlockBehavior> FACTORY = new Factory();
    public final ParticleConfig[] particles;
    public final int tickInterval;

    private SimpleParticleBlockBehavior(BlockDefinition blockDefinition,
                                        ParticleConfig[] particles,
                                        int tickInterval) {
        super(blockDefinition);
        this.particles = particles;
        this.tickInterval = tickInterval;
    }

    @Override
    public <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state) {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.SIMPLE_PARTICLE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new SimpleParticleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> createAsyncBlockEntityTicker(CEWorld level, ImmutableBlockState state, BlockEntityType<T> blockEntityType) {
        if (this.particles.length == 0) return null;
        return EntityBlockBehavior.createTickerHelper(SimpleParticleBlockEntity::tick);
    }

    private static class Factory implements BlockBehaviorFactory<SimpleParticleBlockBehavior> {
        private static final String[] PARTICLES = new String[] {"particles", "particle"};
        private static final String[] TICK_INTERVAL = new String[] {"tick_interval", "tick-interval"};

        @Override
        public SimpleParticleBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new SimpleParticleBlockBehavior(
                    block,
                    section.getSectionList(PARTICLES, ParticleConfig::fromConfig$blockEntity).toArray(new ParticleConfig[0]),
                    section.getInt(TICK_INTERVAL, 10)
            );
        }
    }
}
