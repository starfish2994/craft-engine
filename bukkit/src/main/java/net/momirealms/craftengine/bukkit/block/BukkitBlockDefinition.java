package net.momirealms.craftengine.bukkit.block;

import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.AbstractBlockDefinition;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.BlockStateVariantProvider;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.loot.Loot;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.plugin.context.EventTrigger;
import net.momirealms.craftengine.core.plugin.context.function.Function;
import net.momirealms.craftengine.core.registry.Holder;
import net.momirealms.craftengine.core.world.context.BlockPlaceContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BukkitBlockDefinition extends AbstractBlockDefinition {

    public BukkitBlockDefinition(
            @NotNull Holder.Reference<BlockDefinition> holder,
            @NotNull BlockStateVariantProvider variantProvider,
            @NotNull Map<EventTrigger, List<Function<Context>>> events,
            @Nullable Loot loot
    ) {
        super(holder, variantProvider, events, loot);
    }

    @Override
    public void setPlacedBy(BlockPlaceContext context, ImmutableBlockState state) {
        try {
            this.behavior.placeMultiState(BlockStateUtils.getBlockOwner(state.customBlockState().minecraftState()), new Object[]{
                    context.getLevel().minecraftWorld(),
                    LocationUtils.toBlockPos(context.getClickedPos()),
                    state.customBlockState().minecraftState(),
                    Optional.ofNullable(context.getPlayer()).map(Player::serverPlayer).orElse(null),
                    context.getItem().minecraftItem()
            });
        } catch (Throwable t) {
            CraftEngine.instance().logger().warn("Failed to run setPlacedBy ", t);
        }
    }
}
