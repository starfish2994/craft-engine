package net.momirealms.craftengine.bukkit.loot.source;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.loot.BlockLootContext;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.LootManager;
import net.momirealms.craftengine.core.loot.source.LootOutcome;
import net.momirealms.craftengine.core.loot.source.LootSource;
import net.momirealms.craftengine.core.loot.source.LootSources;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.List;

public final class BlockBreakLootListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        Block block = event.getBlock();
        Key blockType = BlockStateUtils.getBlockOwner(block);
        List<LootSource> sources = LootSources.BLOCK_BREAK.getSources(blockType);
        if (sources.isEmpty()) return;
        if (!event.isDropItems()) return;
        Location location = block.getLocation();
        net.momirealms.craftengine.core.world.World world = BukkitAdaptor.adapt(location.getWorld());
        WorldPosition position = new WorldPosition(world, location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
        BukkitServerPlayer serverPlayer = BukkitAdaptor.adapt(player);
        if (serverPlayer == null) return;
        BukkitExistingBlock bukkitExistingBlock = new BukkitExistingBlock(block);
        Item itemInHand = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        ContextHolder holder = ContextHolder.builder()
                .withParameter(DirectContextParameters.BLOCK, bukkitExistingBlock)
                .withParameter(DirectContextParameters.POSITION, position)
                .withParameter(DirectContextParameters.PLAYER, serverPlayer)
                .withOptionalParameter(DirectContextParameters.ITEM_IN_HAND, ItemUtils.isEmpty(itemInHand) ? null : itemInHand)
                .build();
        BlockLootContext blockLootContext = new BlockLootContext(world, serverPlayer, (float) serverPlayer.luck(), holder, bukkitExistingBlock, itemInHand, serverPlayer.minecraftPlayer());
        LootOutcome outcome = LootManager.eval(sources, blockLootContext);
        if (!outcome.matched()) return;
        if (outcome.overwriteItems()) {
            event.setDropItems(false);
        }
        if (outcome.overwriteExperience()) {
            event.setExpToDrop(0);
        }
        for (Item item : outcome.items()) {
            world.dropItemNaturally(position, item);
        }
    }
}
