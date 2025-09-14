package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.ParticleBlockBehavior;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.World;

public abstract class BaseParticleBlockEntity extends BlockEntity {
    protected final ParticleBlockBehavior behavior;
    protected int tickCount;

    public BaseParticleBlockEntity(BlockEntityType<? extends BlockEntity> type, BlockPos pos, ImmutableBlockState blockState) {
        super(type, pos, blockState);
        this.behavior = super.blockState.behavior().getAs(ParticleBlockBehavior.class).orElseThrow();
    }

    public static void tick(CEWorld ceWorld, BlockPos blockPos, ImmutableBlockState state, BaseParticleBlockEntity particle) {
        if (true) return;
        particle.tickCount++;
        if (particle.tickCount % 10 != 0) return;
        particle.animateTick(state, ceWorld.world(), blockPos);
    }

    public abstract void animateTick(ImmutableBlockState state, World level, BlockPos pos);
}
