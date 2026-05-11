package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LevelUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.property.IntegerProperty;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.number.NumberProvider;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.random.ThreadLocalRandomSource;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelReaderProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;

import java.util.Objects;

public final class DecayBlockBehavior extends BukkitBlockBehavior {
    public static final BlockBehaviorFactory<DecayBlockBehavior> FACTORY = new Factory();
    public final IntegerProperty ageProperty;
    public final NumberProvider delay;
    public final int requiredLight;
    public final boolean hasRequiredLight;
    public final LazyReference<Object> intoBlock;

    public DecayBlockBehavior(BlockDefinition blockDefinition, IntegerProperty ageProperty, NumberProvider delay, int requiredLight, String intoBlock) {
        super(blockDefinition);
        this.ageProperty = ageProperty;
        this.delay = delay;
        this.requiredLight = requiredLight;
        this.hasRequiredLight = requiredLight > 0;
        this.intoBlock = LazyReference.lazyReference(() -> Objects.requireNonNull(CraftEngine.instance().blockManager().createBlockState(intoBlock)).minecraftState());
    }

    @Override
    public void onPlace(Object thisBlock, Object[] args) {
        LevelUtils.scheduleBlockTick(args[1], args[2], thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
    }

    @Override
    public void tick(Object thisBlock, Object[] args) {
        Object level = args[1];
        Object pos = args[2];
        ImmutableBlockState blockState = BlockStateUtils.getNullableCustomBlockState(args[0]);
        if (blockState == null || blockState.isEmpty()) {
            LevelUtils.scheduleBlockTick(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
            return;
        }
        DecayBlockBehavior behavior = blockState.behavior().getFirst(DecayBlockBehavior.class);
        if (behavior == null) {
            LevelUtils.scheduleBlockTick(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
            return;
        }
        if (this.hasRequiredLight && LevelReaderProxy.INSTANCE.getMaxLocalRawBrightness(level, pos) < this.requiredLight) {
            LevelUtils.scheduleBlockTick(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
            return;
        }
        int age = blockState.get(behavior.ageProperty);
        if (age < behavior.ageProperty.max) {
            LevelWriterProxy.INSTANCE.setBlock(level, pos, blockState.with(behavior.ageProperty, age + 1).customBlockState().minecraftState(), UpdateFlags.UPDATE_CLIENTS);
        } else {
            LevelWriterProxy.INSTANCE.setBlock(level, pos, behavior.intoBlock.get(), UpdateFlags.UPDATE_ALL);
        }
        LevelUtils.scheduleBlockTick(level, pos, thisBlock, this.delay.getInt(ThreadLocalRandomSource.INSTANCE));
    }

    private static class Factory implements BlockBehaviorFactory<DecayBlockBehavior> {
        private static final String[] REQUIRED_LIGHT = new String[]{"required_light", "required-light"};
        private static final String[] INTO_BLOCK = new String[]{"into_block", "into-block"};

        @Override
        public DecayBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new DecayBlockBehavior(
                    block,
                    (IntegerProperty) BlockBehaviorFactory.getProperty(section.path(), block, "age", Integer.class),
                    section.getNonNullNumber("delay"),
                    section.getInt(REQUIRED_LIGHT, 0),
                    section.getString(INTO_BLOCK, "air")
            );
        }
    }
}
