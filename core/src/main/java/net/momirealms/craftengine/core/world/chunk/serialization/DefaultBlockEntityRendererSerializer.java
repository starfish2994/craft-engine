package net.momirealms.craftengine.core.world.chunk.serialization;

import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;

public final class DefaultBlockEntityRendererSerializer {

    private DefaultBlockEntityRendererSerializer() {}

    public static List<BlockPos> deserialize(ChunkPos chunkPos, ListTag blockEntitiesTag) {
        List<BlockPos> blockEntities = new ArrayList<>(blockEntitiesTag.size());
        for (int i = 0; i < blockEntitiesTag.size(); i++) {
            CompoundTag tag = blockEntitiesTag.getCompound(i);
            BlockPos blockPos = BlockEntity.readPosAndVerify(tag, chunkPos);
            blockEntities.add(blockPos);
        }
        return blockEntities;
    }

    public static ListTag serialize(List<BlockPos> poses) {
        ListTag listTag = new ListTag();
        for (BlockPos pos : poses) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.x());
            tag.putInt("y", pos.y());
            tag.putInt("z", pos.z());
            listTag.add(tag);
        }
        return listTag;
    }
}
