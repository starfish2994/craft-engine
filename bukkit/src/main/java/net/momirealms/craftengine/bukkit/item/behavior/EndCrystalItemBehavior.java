package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
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
import org.bukkit.GameMode;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

import java.nio.file.Path;
import java.util.ArrayList;
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
                Object level = world.serverWorld();
                org.bukkit.World bukkitWorld = (org.bukkit.World) world.platformWorld();

                AABB aabb = new AABB(x, y, z, x + 1.0, y + 2.0, z + 1.0);
                BoundingBox bukkitAABB = new BoundingBox(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
                // 忽略旁观者
                List<Entity> entities = new ArrayList<>();
                for (org.bukkit.entity.Entity bukkitEntity : bukkitWorld.getNearbyEntities(bukkitAABB)) {
                    if (bukkitEntity instanceof Player player && player.getGameMode().equals(GameMode.SPECTATOR)) {
                        continue;
                    }
                    Entity entity = new BukkitEntity(bukkitEntity);
                    entities.add(entity);
                }
                // 检测家具实体
                List<AABB> detectionAABBs = List.of(aabb);
                List<Object> nmsAABBs = detectionAABBs.stream()
                        .map(aabbs -> FastNMS.INSTANCE.constructor$AABB(aabbs.minX, aabbs.minY, aabbs.minZ, aabbs.maxX, aabbs.maxY, aabbs.maxZ))
                        .toList();
                boolean hasEntities = FastNMS.INSTANCE.checkEntityCollision(level, nmsAABBs, x + 0.5, y, z + 0.5);

                if (!entities.isEmpty() || !hasEntities) {
                    return InteractionResult.PASS;
                } else {
                    return InteractionResult.SUCCESS;
                }
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
