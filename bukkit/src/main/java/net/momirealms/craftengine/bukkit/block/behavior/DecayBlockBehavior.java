package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LevelUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.RandomTickBlock;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.random.ThreadLocalRandomSource;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelAccessorProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelReaderProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LightLayerProxy;

import java.util.Objects;

public final class DecayBlockBehavior extends BukkitBlockBehavior implements RandomTickBlock {
    public static final BlockBehaviorFactory<DecayBlockBehavior> FACTORY = new Factory();
    public final IntegerProperty ageProperty;
    public final NumberProvider delay;
    public final int requiredLight;
    public final boolean hasRequiredLight;
    public final LazyReference<BlockStateWrapper> decayInto;

    public DecayBlockBehavior(BlockDefinition blockDefinition, IntegerProperty ageProperty, NumberProvider delay, int requiredLight, LazyReference<BlockStateWrapper> decayInto) {
        super(blockDefinition);
        this.ageProperty = ageProperty;
        this.delay = delay;
        this.requiredLight = requiredLight;
        this.hasRequiredLight = requiredLight > 0;
        this.decayInto = decayInto;
    }

    @Override
    public void onPlace(Object thisBlock, Object[] args) {
        LevelAccessorProxy.INSTANCE.scheduleTick$0(args[1], args[2], thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
    }

    @Override
    public void tick(Object thisBlock, Object[] args) {
        Object level = args[1];
        Object pos = args[2];
        ImmutableBlockState blockState = BlockStateUtils.getNullableCustomBlockState(args[0]);
        if (blockState == null || blockState.isEmpty()) {
            LevelAccessorProxy.INSTANCE.scheduleTick$0(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
            return;
        }
        DecayBlockBehavior behavior = blockState.behavior().getFirst(DecayBlockBehavior.class);
        if (behavior == null) {
            LevelAccessorProxy.INSTANCE.scheduleTick$0(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
            return;
        }
        if (this.hasRequiredLight && LevelReaderProxy.INSTANCE.getMaxLocalRawBrightness(level, pos) < this.requiredLight) {
            LevelAccessorProxy.INSTANCE.scheduleTick$0(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
            return;
        }
        int age = blockState.get(behavior.ageProperty);
        if (age < behavior.ageProperty.max) {
            LevelWriterProxy.INSTANCE.setBlock(level, pos, blockState.with(behavior.ageProperty, age + 1).customBlockState().minecraftState(), UpdateFlags.UPDATE_CLIENTS);
        } else {
            LevelWriterProxy.INSTANCE.setBlock(level, pos, behavior.decayInto.get().minecraftState(), UpdateFlags.UPDATE_ALL);
        }
        LevelAccessorProxy.INSTANCE.scheduleTick$0(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args) {
        Object level = args[1];
        Object pos = args[2];
        ImmutableBlockState blockState = BlockStateUtils.getNullableCustomBlockState(args[0]);
        if (blockState == null || blockState.isEmpty()) return;
        DecayBlockBehavior behavior = blockState.behavior().getFirst(DecayBlockBehavior.class);
        if (behavior == null) return;
        if (this.hasRequiredLight && LevelReaderProxy.INSTANCE.getBrightness(level, LightLayerProxy.BLOCK, pos) < this.requiredLight) return;
        LevelWriterProxy.INSTANCE.setBlock(level, pos, behavior.decayInto.get().minecraftState(), UpdateFlags.UPDATE_ALL);
    }

    @Override
    public boolean canRandomlyTick(ImmutableBlockState state) {
        return true;
    }

    private static class Factory implements BlockBehaviorFactory<DecayBlockBehavior> {
        private static final String[] REQUIRED_LIGHT = new String[]{"required_light", "required-light"};
        private static final String[] DECAY_INTO = new String[]{"decay_into", "decay-into"};

        @Override
        public DecayBlockBehavior create(BlockDefinition block, ConfigSection section) {
            String decayInto = section.getString(DECAY_INTO, "air");
            return new DecayBlockBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(section.path(), block, "age", Integer.class),
                    section.getNonNullNumber("delay"),
                    section.getInt(REQUIRED_LIGHT, 0),
                    LazyReference.lazyReference(() -> Objects.requireNonNull(CraftEngine.instance().blockManager().createBlockState(decayInto), decayInto))
            );
        }
    }
}
