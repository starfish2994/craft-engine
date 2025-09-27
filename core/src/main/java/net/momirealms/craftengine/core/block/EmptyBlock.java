package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.block.behavior.EmptyBlockBehavior;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.registry.WritableRegistry;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

import java.util.Map;

public final class EmptyBlock extends AbstractCustomBlock {
    public static final EmptyBlock INSTANCE;
    public static final ImmutableBlockState STATE;

    static {
        Holder.Reference<CustomBlock> holder = ((WritableRegistry<CustomBlock>) BuiltInRegistries.BLOCK)
                .registerForHolder(ResourceKey.create(BuiltInRegistries.BLOCK.key().location(), Key.withDefaultNamespace("empty")));
        INSTANCE = new EmptyBlock(holder);
        holder.bindValue(INSTANCE);
        STATE = INSTANCE.defaultState();
        STATE.setSettings(BlockSettings.of());
        STATE.setBehavior(EmptyBlockBehavior.INSTANCE);
    }

    private EmptyBlock(Holder.Reference<CustomBlock> holder) {
        super(holder, new BlockStateVariantProvider(holder, ImmutableBlockState::new, Map.of()), Map.of(), null);
    }

    public static void initialize() {
    }
}
