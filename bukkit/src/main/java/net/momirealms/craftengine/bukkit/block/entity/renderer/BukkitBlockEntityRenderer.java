package net.momirealms.craftengine.bukkit.block.entity.renderer;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityRenderer;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;

import java.lang.ref.WeakReference;
import java.util.List;

public class BukkitBlockEntityRenderer extends BlockEntityRenderer {
    private final BlockEntityElement[] elements;
    private final WeakReference<Object> chunkHolder;

    public BukkitBlockEntityRenderer(WeakReference<Object> chunkHolder, BlockEntityElement[] elements) {
        this.chunkHolder = chunkHolder;
        this.elements = elements;
    }

    @Override
    public void despawn() {
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.chunkHolder.get());
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
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.chunkHolder.get());
        if (players.isEmpty()) return;
        for (Object player : players) {
            org.bukkit.entity.Player bkPlayer = FastNMS.INSTANCE.method$ServerPlayer$getBukkitEntity(player);
            BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(bkPlayer);
            if (serverPlayer == null) continue;
            spawn(serverPlayer);
        }
    }

    @Override
    public void update() {
        List<Object> players = FastNMS.INSTANCE.method$ChunkHolder$getPlayers(this.chunkHolder.get());
        if (players.isEmpty()) return;
        for (Object player : players) {
            org.bukkit.entity.Player bkPlayer = FastNMS.INSTANCE.method$ServerPlayer$getBukkitEntity(player);
            BukkitServerPlayer serverPlayer = BukkitAdaptors.adapt(bkPlayer);
            if (serverPlayer == null) continue;
            update(serverPlayer);
        }
    }

    @Override
    public void update(Player player) {
        for (BlockEntityElement element : this.elements) {
            element.update(player);
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
}
