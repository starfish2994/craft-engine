package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.core.plugin.network.ConnectionState;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;

public class StartConfigurationListener implements ByteBufferPacketListener {
    public static final StartConfigurationListener INSTANCE = new StartConfigurationListener();

    private StartConfigurationListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        /*
        1.20.2+
        1. send ClientboundStartConfigurationPacket

        1.20.5+
        1. send ClientboundStartConfigurationPacket
        2. set outbound(encode|s2c) to configuration
         */
        user.setEncoderState(ConnectionState.CONFIGURATION); // out
    }
}
