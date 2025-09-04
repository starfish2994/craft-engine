package net.momirealms.craftengine.core.world.chunk.serialization;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DefaultBlockEntitySerializer {

    public static ListTag serialize(Map<BlockPos, BlockEntity> tiles) {
        ListTag result = new ListTag();
        for (Map.Entry<BlockPos, BlockEntity> entry : tiles.entrySet()) {
            BlockEntity entity = entry.getValue();
            if (entity.isValid()) {
                result.add(entity.saveAsTag());
            }
        }
        return result;
    }

    public static List<BlockEntity> deserialize(CEChunk chunk, ListTag tag) {
        List<BlockEntity> blockEntities = new ArrayList<>(tag.size());
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag data = tag.getCompound(i);
            Key id = Key.of(data.getString("id"));
            BlockEntityType<?> type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(id);
            if (type == null) {
                Debugger.BLOCK_ENTITY.debug(() -> "Unknown block entity type: " + id);
            } else {
                BlockPos pos = BlockEntity.readPosAndVerify(data, chunk.chunkPos());
                ImmutableBlockState blockState = chunk.getBlockState(pos);
                BlockEntity blockEntity = type.factory().create(pos, blockState);
                blockEntity.loadCustomData(data);
                blockEntities.add(blockEntity);
            }
        }
        return blockEntities;
    }
}
