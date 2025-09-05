package net.momirealms.craftengine.core.world.chunk;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRendererConfig;
import net.momirealms.craftengine.core.block.entity.tick.*;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.world.*;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultBlockEntityRendererSerializer;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultBlockEntitySerializer;
import net.momirealms.sparrow.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CEChunk {
    public final CEWorld world;
    public final ChunkPos chunkPos;
    public final CESection[] sections;
    public final WorldHeight worldHeightAccessor;
    public final Map<BlockPos, BlockEntity> blockEntities;
    public final Map<BlockPos, BlockEntityRenderer> blockEntityRenderers;
    private final ReentrantReadWriteLock renderLock = new ReentrantReadWriteLock();
    private volatile boolean dirty;
    private volatile boolean loaded;
    protected final Map<BlockPos, ReplaceableTickingBlockEntity> tickingBlockEntitiesByPos = new Object2ObjectOpenHashMap<>(10, 0.5f);

    public CEChunk(CEWorld world, ChunkPos chunkPos) {
        this.world = world;
        this.chunkPos = chunkPos;
        this.worldHeightAccessor = world.worldHeight();
        this.sections = new CESection[this.worldHeightAccessor.getSectionsCount()];
        this.blockEntities = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.blockEntityRenderers = new Object2ObjectOpenHashMap<>(10, 0.5f);
        this.fillEmptySection();
    }

    public CEChunk(CEWorld world, ChunkPos chunkPos, CESection[] sections, @Nullable ListTag blockEntitiesTag, @Nullable ListTag itemDisplayBlockRenders) {
        this.world = world;
        this.chunkPos = chunkPos;
        this.worldHeightAccessor = world.worldHeight();
        int sectionCount = this.worldHeightAccessor.getSectionsCount();
        this.sections = new CESection[sectionCount];
        if (sections != null) {
            for (CESection section : sections) {
                if (section != null) {
                    int index = sectionIndex(section.sectionY());
                    this.sections[index] = section;
                }
            }
        }
        this.fillEmptySection();
        if (blockEntitiesTag != null) {
            this.blockEntities = new Object2ObjectOpenHashMap<>(Math.max(blockEntitiesTag.size(), 10), 0.5f);
            List<BlockEntity> blockEntities = DefaultBlockEntitySerializer.deserialize(this, blockEntitiesTag);
            for (BlockEntity blockEntity : blockEntities) {
                this.setBlockEntity(blockEntity);
            }
        } else {
            this.blockEntities = new Object2ObjectOpenHashMap<>(10, 0.5f);
        }
        if (itemDisplayBlockRenders != null) {
            this.blockEntityRenderers = new Object2ObjectOpenHashMap<>(Math.max(itemDisplayBlockRenders.size(), 10), 0.5f);
            List<BlockPos> blockEntityRendererPoses = DefaultBlockEntityRendererSerializer.deserialize(this.chunkPos, itemDisplayBlockRenders);
            for (BlockPos pos : blockEntityRendererPoses) {
                this.addBlockEntityRenderer(pos);
            }
        } else {
            this.blockEntityRenderers = new Object2ObjectOpenHashMap<>(10, 0.5f);
        }
    }

    public void spawnBlockEntities(Player player) {
        try {
            this.renderLock.readLock().lock();
            for (BlockEntityRenderer renderer : this.blockEntityRenderers.values()) {
                renderer.spawn(player);
            }
        } finally {
            this.renderLock.readLock().unlock();
        }
    }

    public void despawnBlockEntities(Player player) {
        try {
            this.renderLock.readLock().lock();
            for (BlockEntityRenderer renderer : this.blockEntityRenderers.values()) {
                renderer.despawn(player);
            }
        } finally {
            this.renderLock.readLock().unlock();
        }
    }

    public void addBlockEntityRenderer(BlockPos pos) {
        this.addBlockEntityRenderer(pos, this.getBlockState(pos));
    }

    public void addBlockEntityRenderer(BlockPos pos, ImmutableBlockState state) {
        BlockEntityRendererConfig config = state.entityRenderer();
        if (config != null) {
            BlockEntityRenderer renderer = this.world.createBlockEntityRenderer(config, pos);
            renderer.spawn();
            try {
                this.renderLock.writeLock().lock();
                this.blockEntityRenderers.put(pos, renderer);
            } finally {
                this.renderLock.writeLock().unlock();
            }
        }
    }

    public void removeBlockEntityRenderer(BlockPos pos) {
        try {
            this.renderLock.writeLock().lock();
            BlockEntityRenderer removed = this.blockEntityRenderers.remove(pos);
            if (removed != null) {
                removed.despawn();
            }
        } finally {
            this.renderLock.writeLock().unlock();
        }
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
        this.replaceOrCreateTickingBlockEntity(blockEntity);
    }

    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity removedBlockEntity = this.blockEntities.remove(blockPos);
        if (removedBlockEntity != null) {
            removedBlockEntity.setValid(false);
        }
        this.removeBlockEntityTicker(blockPos);
    }

    public void activateAllBlockEntities() {
        for (BlockEntity blockEntity : this.blockEntities.values()) {
            blockEntity.setValid(true);
            replaceOrCreateTickingBlockEntity(blockEntity);
        }
    }

    public void deactivateAllBlockEntities() {
        this.blockEntities.values().forEach(e -> e.setValid(false));
        this.tickingBlockEntitiesByPos.values().forEach((ticker) -> ticker.setTicker(DummyTickingBlockEntity.INSTANCE));
        this.tickingBlockEntitiesByPos.clear();
    }

    public <T extends BlockEntity> void replaceOrCreateTickingBlockEntity(T blockEntity) {
        ImmutableBlockState blockState = blockEntity.blockState();
        BlockEntityTicker<T> ticker = blockState.createBlockEntityTicker(this.world, blockEntity.type());
        if (ticker != null) {
            this.tickingBlockEntitiesByPos.compute(blockEntity.pos(), ((pos, previousTicker) -> {
                TickingBlockEntity newTicker = new TickingBlockEntityImpl<>(this, blockEntity, ticker);
                if (previousTicker != null) {
                    previousTicker.setTicker(newTicker);
                    return previousTicker;
                } else {
                    ReplaceableTickingBlockEntity replaceableTicker = new ReplaceableTickingBlockEntity(newTicker);
                    this.world.addBlockEntityTicker(replaceableTicker);
                    return replaceableTicker;
                }
            }));
        } else {
            this.removeBlockEntityTicker(blockEntity.pos());
        }
    }

    private void removeBlockEntityTicker(BlockPos pos) {
        ReplaceableTickingBlockEntity blockEntity = this.tickingBlockEntitiesByPos.remove(pos);
        if (blockEntity != null) {
            blockEntity.setTicker(DummyTickingBlockEntity.INSTANCE);
        }
    }

    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.pos();
        ImmutableBlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            Debugger.BLOCK_ENTITY.debug(() -> "Failed to add invalid block entity " + blockEntity.saveAsTag() + " at " + pos);
            return;
        }
        // 设置方块实体所在世界
        blockEntity.setWorld(this.world);
        blockEntity.setValid(true);
        BlockEntity previous = this.blockEntities.put(pos, blockEntity);
        // 标记旧的方块实体无效
        if (previous != null && previous != blockEntity) {
            previous.setValid(false);
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos, boolean create) {
        BlockEntity blockEntity = this.blockEntities.get(pos);
        if (blockEntity == null) {
            if (create) {
                blockEntity = createBlockEntity(pos);
                if (blockEntity != null) {
                    this.addBlockEntity(blockEntity);
                }
            }
        } else {
            if (!blockEntity.isValid()) {
                this.blockEntities.remove(pos);
                return null;
            }
        }
        return blockEntity;
    }

    private BlockEntity createBlockEntity(BlockPos pos) {
        ImmutableBlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            return null;
        }
        return Objects.requireNonNull(blockState.behavior().getEntityBehavior()).createBlockEntity(pos, blockState);
    }

    public Collection<BlockEntity> blockEntities() {
        return Collections.unmodifiableCollection(this.blockEntities.values());
    }

    public List<BlockPos> blockEntityRenderers() {
        try {
            this.renderLock.readLock().lock();
            return new ArrayList<>(this.blockEntityRenderers.keySet());
        } finally {
            this.renderLock.readLock().unlock();
        }
    }

    public boolean dirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isEmpty() {
        if (!this.blockEntities.isEmpty()) return false;
        for (CESection section : this.sections) {
            if (section != null && !section.statesContainer().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void fillEmptySection() {
        for (int i = 0; i < this.sections.length; ++i) {
            if (this.sections[i] == null) {
                this.sections[i] = new CESection(this.world.worldHeight().getSectionYFromSectionIndex(i),
                        new PalettedContainer<>(null, EmptyBlock.STATE, PalettedContainer.PaletteProvider.CUSTOM_BLOCK_STATE));
            }
        }
    }

    public void setBlockState(BlockPos pos, ImmutableBlockState state) {
        this.setBlockState(pos.x(), pos.y(), pos.z(), state);
    }

    public void setBlockState(int x, int y, int z, ImmutableBlockState state) {
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        CESection section = this.sections[index];
        if (section == null) {
            return;
        }
        ImmutableBlockState previous = section.setBlockState((y & 15) << 8 | (z & 15) << 4 | x & 15, state);
        if (previous != state) {
            setDirty(true);
        }
    }

    @NotNull
    public ImmutableBlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.x(), pos.y(), pos.z());
    }

    @NotNull
    public ImmutableBlockState getBlockState(int x, int y, int z) {
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        CESection section = this.sections[index];
        if (section == null) {
            return EmptyBlock.STATE;
        }
        return section.getBlockState((y & 15) << 8 | (z & 15) << 4 | x & 15);
    }

    @Nullable
    public CESection sectionByIndex(int index) {
        return this.sections[index];
    }

    @NotNull
    public CESection sectionById(int sectionId) {
        return this.sections[sectionIndex(sectionId)];
    }

    public int sectionIndex(int sectionId) {
        return sectionId - this.worldHeightAccessor.getMinSection();
    }

    public int sectionY(int sectionIndex) {
        return sectionIndex + this.worldHeightAccessor.getMinSection();
    }

    @NotNull
    public CEWorld world() {
        return this.world;
    }

    @NotNull
    public ChunkPos chunkPos() {
        return this.chunkPos;
    }

    @NotNull
    public CESection[] sections() {
        return this.sections;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void load() {
        if (this.loaded) return;
        this.world.addLoadedChunk(this);
        this.activateAllBlockEntities();
        this.loaded = true;
    }

    public void unload() {
        if (!this.loaded) return;
        this.world.removeLoadedChunk(this);
        this.deactivateAllBlockEntities();
        this.loaded = false;
    }
}
