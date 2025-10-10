package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.block.data.BlockData;

import java.nio.file.Path;
import java.util.Map;

public class CompassItemBehavior extends ItemBehavior {
    public static final CompassItemBehavior INSTANCE = new CompassItemBehavior();
    public static final CompassItemBehavior.Factory FACTORY = new CompassItemBehavior.Factory();

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        BukkitExistingBlock block = (BukkitExistingBlock) context.getLevel().getBlockAt(context.getClickedPos());
        BlockData blockData = block.block().getBlockData();
        Object blockOwner = BlockStateUtils.getBlockOwner(BlockStateUtils.blockDataToBlockState(blockData));

        if (blockOwner != MBlocks.LODESTONE) {
            return InteractionResult.PASS;
        } else {
            return InteractionResult.SUCCESS;
        }
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
