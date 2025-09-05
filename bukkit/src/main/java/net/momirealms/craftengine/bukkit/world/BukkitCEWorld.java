package net.momirealms.craftengine.bukkit.world;

import net.momirealms.craftengine.bukkit.util.LightUtils;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRendererConfig;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.SectionPosUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.SectionPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;

import java.util.ArrayList;
import java.util.List;

public class BukkitCEWorld extends CEWorld {

    public BukkitCEWorld(World world, StorageAdaptor adaptor) {
        super(world, adaptor);
    }

    public BukkitCEWorld(World world, WorldDataStorage dataStorage) {
        super(world, dataStorage);
    }

    @Override
    public void updateLight() {
        List<SectionPos> poses;
        synchronized (super.updatedSectionSet) {
            poses = new ArrayList<>(super.updatedSectionSet);
            super.updatedSectionSet.clear();
        }
        if (Config.enableLightSystem()) {
            LightUtils.updateChunkLight(
                    (org.bukkit.World) this.world.platformWorld(),
                    SectionPosUtils.toMap(poses,
                            this.world.worldHeight().getMinSection() - 1,
                            this.world.worldHeight().getMaxSection() + 1
                    )
            );
        }
    }

    @Override
    public BlockEntityRenderer createBlockEntityRenderer(BlockEntityRendererConfig config, BlockPos pos) {
        return null;
    }
}
