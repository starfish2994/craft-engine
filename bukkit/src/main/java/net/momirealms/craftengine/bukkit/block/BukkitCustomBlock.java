package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.core.block.AbstractCustomBlock;
import net.momirealms.craftengine.core.block.BlockStateVariantProvider;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.event.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.registry.Holder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class BukkitCustomBlock extends AbstractCustomBlock {

    public BukkitCustomBlock(
            @NotNull Holder.Reference<CustomBlock> holder,
            @NotNull BlockStateVariantProvider variantProvider,
            @NotNull Map<EventTrigger, List<Function<Context>>> events,
            @Nullable LootTable<?> lootTable
    ) {
        super(holder, variantProvider, events, lootTable);
    }
}
