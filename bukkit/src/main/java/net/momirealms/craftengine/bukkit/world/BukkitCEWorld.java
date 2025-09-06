package net.momirealms.craftengine.bukkit.world;

import net.momirealms.craftengine.bukkit.block.entity.renderer.BukkitBlockEntityRenderer;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.LightUtils;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRendererConfig;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.SectionPosUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;

import java.lang.ref.WeakReference;

public class BukkitCEWorld extends CEWorld {

    public BukkitCEWorld(World world, StorageAdaptor adaptor) {
        super(world, adaptor);
    }

    public BukkitCEWorld(World world, WorldDataStorage dataStorage) {
        super(world, dataStorage);
    }

    @Override
    public void updateLight() {
        if (Config.enableLightSystem()) {
            super.isUpdatingLights = true;
            LightUtils.updateChunkLight(
                    (org.bukkit.World) this.world.platformWorld(),
                    SectionPosUtils.toMap(super.lightSections,
                            this.world.worldHeight().getMinSection() - 1,
                            this.world.worldHeight().getMaxSection() + 1
                    )
            );
            super.lightSections.clear();
            super.isUpdatingLights = false;
            super.lightSections.addAll(super.pendingLightSections);
            super.pendingLightSections.clear();
        }
    }

    @Override
    public BlockEntityRenderer createBlockEntityRenderer(BlockEntityRendererConfig config, BlockPos pos) {
        Object serverLevel = this.world.serverWorld();
        Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
        long chunkKey = ChunkPos.asLong(pos.x() >> 4, pos.z() >> 4);
        Object chunkHolder = FastNMS.INSTANCE.method$ServerChunkCache$getVisibleChunkIfPresent(chunkSource, chunkKey);
        return new BukkitBlockEntityRenderer(new WeakReference<>(chunkHolder), config, pos);
    }
}
