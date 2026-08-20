package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.LiquidBlockContainer;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.BlockGetterProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelAccessorProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidStateProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.material.FluidsProxy;

import java.util.Optional;

public class SeagrassLikeBlockBehavior extends BukkitBlockBehavior implements LiquidBlockContainer {
    public static final BlockBehaviorFactory<SeagrassLikeBlockBehavior> FACTORY = new Factory();

    public SeagrassLikeBlockBehavior(BlockDefinition blockDefinition) {
        super(blockDefinition);
    }

    private static class Factory implements BlockBehaviorFactory<SeagrassLikeBlockBehavior> {
        @Override
        public SeagrassLikeBlockBehavior create(BlockDefinition block, ConfigSection section) {
            return new SeagrassLikeBlockBehavior(block);
        }
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Object level = context.getLevel().minecraftWorld();
        Object clickedPos = LocationUtils.toBlockPos(context.getClickedPos());
        Object fluidState = BlockGetterProxy.INSTANCE.getFluidState(level, clickedPos);
        if (FluidStateProxy.INSTANCE.getType(fluidState) == FluidsProxy.WATER && FluidStateProxy.INSTANCE.getAmount(fluidState) == 8) {
            return state;
        }
        return null;
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args) {
        Object blockState = args[0];
        Optional<ImmutableBlockState> optionalCustomState = BlockStateUtils.getOptionalCustomBlockState(blockState);
        if (optionalCustomState.isEmpty()) return blockState;
        LevelAccessorProxy.INSTANCE.scheduleTick$1(args[updateShape$level], args[updateShape$blockPos], FluidsProxy.WATER, 5);
        return blockState;
    }

    @Override
    public boolean canPlaceLiquid(Object thisBlock, Object[] args) {
        return false;
}

    @Override
    public boolean placeLiquid(Object thisBlock, Object[] args) {
        return false;
    }
}
