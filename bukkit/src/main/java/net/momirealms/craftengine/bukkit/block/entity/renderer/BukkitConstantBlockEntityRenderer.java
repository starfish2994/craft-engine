package net.momirealms.craftengine.bukkit.block.entity.renderer;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.entity.render.ConstantBlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.World;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class BukkitConstantBlockEntityRenderer extends ConstantBlockEntityRenderer {
    private final BlockEntityElement[] elements;
    private final World world;
    private final ChunkPos chunkPos;

    public BukkitConstantBlockEntityRenderer(World world, ChunkPos pos, BlockEntityElement[] elements) {
        this.world = world;
        this.chunkPos = pos;
        this.elements = elements;
    }

    private Object getChunkHolder() {
        Object serverLevel = this.world.serverWorld();
        Object chunkSource = FastNMS.INSTANCE.method$ServerLevel$getChunkSource(serverLevel);
        return FastNMS.INSTANCE.method$ServerChunkCache$getVisibleChunkIfPresent(chunkSource, this.chunkPos.longKey);
    }

    @Override
    public void despawn() {
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.getChunkHolder());
        if (players.isEmpty()) return;
        for (Object player : players) {
            org.bukkit.entity.Player bkPlayer = FastNMS.INSTANCE.method$ServerPlayer$getBukkitEntity(player);
            BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(bkPlayer);
            if (serverPlayer == null) continue;
            despawn(serverPlayer);
        }
    }

    @Override
    public void spawn() {
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.getChunkHolder());
        if (players.isEmpty()) return;
        for (Object player : players) {
            org.bukkit.entity.Player bkPlayer = FastNMS.INSTANCE.method$ServerPlayer$getBukkitEntity(player);
            BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(bkPlayer);
            if (serverPlayer == null) continue;
            spawn(serverPlayer);
        }
    }

    @Override
    public void spawn(Player player) {
        for (BlockEntityElement element : this.elements) {
            element.spawn(player);
        }
    }

    @Override
    public void despawn(Player player) {
        for (BlockEntityElement element : this.elements) {
            element.despawn(player);
        }
    }

    @Override
    public void deactivate() {
        for (BlockEntityElement element : this.elements) {
            element.deactivate();
        }
    }

    @Override
    public void activate() {
        for (BlockEntityElement element : this.elements) {
            element.activate();
        }
    }
}
