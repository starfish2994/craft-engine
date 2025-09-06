package net.momirealms.craftengine.bukkit.block.entity.renderer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MEntityTypes;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRendererConfig;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.BlockPos;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BukkitBlockEntityRenderer extends BlockEntityRenderer {
    private final Object cachedSpawnPacket;
    private final Object cachedDespawnPacket;
    private final WeakReference<Object> chunkHolder;

    public BukkitBlockEntityRenderer(WeakReference<Object> chunkHolder,
                                     BlockEntityRendererConfig config,
                                     BlockPos pos) {
        this.chunkHolder = chunkHolder;
        BlockEntityElement[] elements = config.elements();
        IntList ids = new IntArrayList(elements.length);
        List<Object> spawnPackets = new ArrayList<>(elements.length);
        for (BlockEntityElement element : elements) {
            int entityId = CoreReflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
            Vector3f position = element.position();
            spawnPackets.add(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                    entityId, UUID.randomUUID(), pos.x() + position.x, pos.y() + position.y, pos.z() + position.z,
                    element.xRot(), element.yRot(), MEntityTypes.ITEM_DISPLAY, 0, CoreReflections.instance$Vec3$Zero, 0
            ));
            spawnPackets.add(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(
                    entityId, element.metadataValues().get()
            ));
            ids.add(entityId);
        }
        this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(spawnPackets);
        this.cachedDespawnPacket = FastNMS.INSTANCE.constructor$ClientboundRemoveEntitiesPacket(ids);
    }

    @Override
    public void despawn() {
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.chunkHolder.get());
        if (players.isEmpty()) return;
        for (Object player : players) {
            FastNMS.INSTANCE.method$ServerPlayerConnection$send(
                    FastNMS.INSTANCE.field$Player$connection(player),
                    this.cachedDespawnPacket
            );
        }
    }

    @Override
    public void spawn() {
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.chunkHolder.get());
        if (players.isEmpty()) return;
        for (Object player : players) {
            FastNMS.INSTANCE.method$ServerPlayerConnection$send(
                    FastNMS.INSTANCE.field$Player$connection(player),
                    this.cachedSpawnPacket
            );
        }
    }

    @Override
    public void spawn(Player player) {
        player.sendPacket(this.cachedSpawnPacket, false);
    }

    @Override
    public void despawn(Player player) {
        player.sendPacket(this.cachedDespawnPacket, false);
    }
}
