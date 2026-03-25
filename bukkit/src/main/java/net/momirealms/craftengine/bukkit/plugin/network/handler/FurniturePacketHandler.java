package net.momirealms.craftengine.bukkit.plugin.network.handler;

import it.unimi.dsi.fastutil.ints.IntList;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;

public final class FurniturePacketHandler implements EntityPacketHandler {
    private final Furniture furniture;

    public FurniturePacketHandler(Furniture furniture) {
        this.furniture = furniture;
    }

    @Override
    public boolean handleEntitiesRemove(NetWorkUser user, IntList entityIds) {
        Player player = (Player) user;
        player.removeTrackedEntity(this.furniture.entityId());
        for (FurnitureElement element : this.furniture.elements()) {
            element.hide(player);
        }
        for (FurnitureHitBox hitBox : this.furniture.hitboxes()) {
            hitBox.hide(player);
        }
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
