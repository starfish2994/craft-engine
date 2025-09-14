package net.momirealms.craftengine.core.block.entity.tick;

import net.momirealms.craftengine.core.world.BlockPos;

public class DummyTickingBlockEntity implements TickingBlockEntity {
    public static final DummyTickingBlockEntity INSTANCE = new DummyTickingBlockEntity();

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public BlockPos pos() {
        return BlockPos.ZERO;
    }
}
