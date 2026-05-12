package net.momirealms.craftengine.proxy.common.network.packet;

import net.momirealms.craftengine.proxy.common.network.ChannelConnection;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface PacketHandler {
    void handle(ChannelConnection connection, @Nullable ProxyPlayer player, PacketContext packet);
}
