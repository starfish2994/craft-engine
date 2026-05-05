package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

// 1.21.2+
public class SyncEntityPositionListener implements ByteBufferPacketListener {
    public static final SyncEntityPositionListener INSTANCE = new SyncEntityPositionListener();

    private SyncEntityPositionListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        FriendlyByteBuf buf = event.getBuffer();
        int entityId = buf.readVarInt();
        EntityPacketHandler handler = user.entityPacketHandlers().get(entityId);
        if (handler != null) {
            handler.handleSyncEntityPosition(user, event, entityId, buf);
        }
    }
}
