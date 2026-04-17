package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.LiquidBlockContainer;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.world.WorldEvents;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelAccessorProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelWriterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.WorldGenLevelProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.WorldGenRegionProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.block.BlockProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidStateProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidsProxy;

public final class LiquidFlowableBlockBehavior extends BukkitBlockBehavior implements LiquidBlockContainer {
    public static final BlockBehaviorFactory<LiquidFlowableBlockBehavior> FACTORY = new Factory();

    private LiquidFlowableBlockBehavior(BlockDefinition blockDefinition) {
        super(blockDefinition);
    }

    @Override
    public boolean canPlaceLiquid(Object thisBlock, Object[] args) {
        return true;
    }

    @Override
    public boolean placeLiquid(Object thisBlock, Object[] args) {
        Object level = args[0];
        Object pos = args[1];
        Object blockState = args[2];
        Object fluidState = args[3];
        Object fluidType = FluidStateProxy.INSTANCE.getType(fluidState);
        if (fluidType == FluidsProxy.LAVA || fluidType == FluidsProxy.FLOWING_LAVA) {
            LevelAccessorProxy.INSTANCE.levelEvent(level, WorldEvents.LAVA_CONVERTS_BLOCK, pos, 0);
        } else {
            if (!WorldGenRegionProxy.CLASS.isInstance(level)) {
                BlockProxy.INSTANCE.dropResources(blockState, level, pos);
            }
        }
        LevelWriterProxy.INSTANCE.setBlock(level, pos, FluidStateProxy.INSTANCE.createLegacyBlock(fluidState), UpdateFlags.UPDATE_ALL);
        return true;
    }

    private static class Factory implements BlockBehaviorFactory<LiquidFlowableBlockBehavior> {

        @Override
        public LiquidFlowableBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new LiquidFlowableBlockBehavior(block);
        }
    }
}
