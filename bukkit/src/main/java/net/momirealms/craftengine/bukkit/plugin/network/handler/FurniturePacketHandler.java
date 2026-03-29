package net.momirealms.craftengine.bukkit.plugin.network.handler;

import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureSnapshotState;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;

public final class FurniturePacketHandler implements EntityPacketHandler {
    private final int entityId;
    private final FurnitureSnapshotState snapshotState;

    public FurniturePacketHandler(Furniture furniture) {
        this.entityId = furniture.entityId();
        this.snapshotState = furniture.snapshotState();
    }

    @Override
    public boolean handleEntitiesRemove(NetWorkUser user, IntList entityIds) {
        Player player = (Player) user;
        player.removeTrackedEntity(this.entityId);
        this.snapshotState.hide(player);
        return true;
    }

    @Override
    public void handleSyncEntityPosition(NetWorkUser user, NMSPacketEvent event, Object packet) {
        event.setCancelled(true);
    }

    @Override
    public void handleMove(NetWorkUser user, NMSPacketEvent event, Object packet) {
        event.setCancelled(true);
    }
}
