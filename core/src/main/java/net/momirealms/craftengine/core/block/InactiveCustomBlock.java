package net.momirealms.craftengine.core.block;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.momirealms.craftengine.core.block.behavior.EmptyBlockBehavior;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.sparrow.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

public final class InactiveCustomBlock extends AbstractCustomBlock {
    private final Map<CompoundTag, ImmutableBlockState> cachedData = new HashMap<>();

    public InactiveCustomBlock(Holder.Reference<CustomBlock> holder) {
        super(holder, new BlockStateVariantProvider(holder, ImmutableBlockState::new, Map.of()), Map.of(), null);
    }

    @Override
    public ImmutableBlockState getBlockState(CompoundTag nbt) {
        return this.cachedData.computeIfAbsent(nbt, k -> {
            ImmutableBlockState state = new ImmutableBlockState(super.holder, new Reference2ObjectArrayMap<>());
            state.setBehavior(EmptyBlockBehavior.INSTANCE);
            state.setNbtToSave(state.toNbtToSave(nbt));
            return state;
        });
    }
}