package net.momirealms.craftengine.core.block.behavior;

import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.EmptyBlockDefinition;

public final class EmptyBlockBehavior extends BlockBehavior {
    public static final EmptyBlockBehavior INSTANCE = new EmptyBlockBehavior(EmptyBlockDefinition.INSTANCE);

    public EmptyBlockBehavior(BlockDefinition block) {
        super(block);
    }
}
