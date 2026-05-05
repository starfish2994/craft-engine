package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.VersionHelper;

public class ByteBufferLoginListener implements ByteBufferPacketListener {
    public static final ByteBufferLoginListener INSTANCE = new ByteBufferLoginListener();

    private ByteBufferLoginListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (VersionHelper.isOrAbove1_20_2()) {
            /*
            1.20.2+
            1. send ClientboundLoginPacket to client

            1.20.5+
            1. set inbound(decode|c2s) to play
            2. send ClientboundLoginPacket to client
             */
            user.setDecoderState(ConnectionState.PLAY);
        }
    }
}
