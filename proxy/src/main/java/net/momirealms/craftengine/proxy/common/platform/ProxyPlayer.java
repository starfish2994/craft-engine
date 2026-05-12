package net.momirealms.craftengine.proxy.common.platform;

import net.momirealms.craftengine.proxy.common.network.ChannelConnection;

import java.util.Locale;
import java.util.UUID;

public interface ProxyPlayer {

    UUID uuid();

    Object platform();

    BackendServer server();

    ChannelConnection connection();

    boolean sendServerPluginMessage(String channel, byte[] data);

    Locale locale();

    void kick(String reason);
}
