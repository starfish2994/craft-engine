package net.momirealms.craftengine.bukkit.world.chunk.serialization;

import net.momirealms.craftengine.bukkit.world.FoliaCEChunk;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.CESection;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultSectionSerializer;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import org.jetbrains.annotations.NotNull;

public final class FoliaChunkSerializer {
    private FoliaChunkSerializer() {}

    @NotNull
    public static CEChunk deserialize(@NotNull CEWorld world, @NotNull ChunkPos pos, @NotNull CompoundTag chunkNbt) {
        ListTag sections = chunkNbt.getList("sections");
        CESection[] sectionArray = new CESection[world.worldHeight().getSectionsCount()];
        for (int i = 0, size = sections.size(); i < size; ++i) {
            CompoundTag sectionTag = sections.getCompound(i);
            CESection ceSection = DefaultSectionSerializer.deserialize(sectionTag);
            if (ceSection != null) {
                int sectionIndex = world.worldHeight().getSectionIndexFromSectionY(ceSection.sectionY());
                if (sectionIndex >= 0 && sectionIndex < sectionArray.length) {
                    sectionArray[sectionIndex] = ceSection;
                }
            }
        }
        ListTag blockEntities = chunkNbt.getList("block_entities");
        ListTag blockEntityRenders = chunkNbt.getList("block_entity_renderers");
        return new FoliaCEChunk(world, pos, sectionArray, blockEntities, blockEntityRenders, null);
    }
}
