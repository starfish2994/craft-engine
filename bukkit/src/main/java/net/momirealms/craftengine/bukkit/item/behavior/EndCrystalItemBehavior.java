package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.world.BukkitExistingBlock;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.collision.AABB;
import org.bukkit.block.data.BlockData;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class EndCrystalItemBehavior extends ItemBehavior {
    public static final EndCrystalItemBehavior INSTANCE = new EndCrystalItemBehavior();
    public static final EndCrystalItemBehavior.Factory FACTORY = new EndCrystalItemBehavior.Factory();

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        World world = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BukkitExistingBlock block = (BukkitExistingBlock) world.getBlockAt(blockPos);
        BlockData blockData = block.block().getBlockData();
        Object blockOwner = BlockStateUtils.getBlockOwner(BlockStateUtils.blockDataToBlockState(blockData));

        if (blockOwner != MBlocks.OBSIDIAN && blockOwner != MBlocks.BEDROCK) {
            return InteractionResult.PASS;
        } else {
            BlockPos abovePos = blockPos.above();
            BukkitExistingBlock aboveBlock = (BukkitExistingBlock) world.getBlockAt(abovePos);
            BlockData aboveBlockData = aboveBlock.block().getBlockData();
            Object aboveBlockOwner = BlockStateUtils.getBlockOwner(BlockStateUtils.blockDataToBlockState(aboveBlockData));

            if (aboveBlockOwner != MBlocks.AIR) {
                return InteractionResult.PASS;
            } else {

                double x = abovePos.x();
                double y = abovePos.y();
                double z = abovePos.z();
                AABB aabb = new AABB(x, y, z, x + 1.0, y + 2.0, z + 1.0);
                List<Entity> entities = world.getEntities(null, aabb);
                if (!entities.isEmpty()) {
                    return InteractionResult.PASS;
                }

                List<AABB> aabbs = List.of(aabb);
                List<Object> nmsAABB = aabbs.stream()
                        .map(AABB -> FastNMS.INSTANCE.constructor$AABB(
                                AABB.minX, AABB.minY, AABB.minZ,
                                AABB.maxX, AABB.maxY, AABB.maxZ
                        )).toList();
                if (!FastNMS.INSTANCE.checkEntityCollision(context.getLevel().serverWorld(), nmsAABB, x, y, z)) {
                    return InteractionResult.PASS;
                }
                return InteractionResult.SUCCESS;
            }
        }
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, Key id, Map<String, Object> arguments) {
            return INSTANCE;
        }
    }
}
