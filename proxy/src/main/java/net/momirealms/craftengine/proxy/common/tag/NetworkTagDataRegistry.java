package net.momirealms.craftengine.proxy.common.tag;

import net.momirealms.craftengine.proxy.common.platform.BackendServer;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class NetworkTagDataRegistry {
    private final Map<String, NetworkTagData> serverFonts = new ConcurrentHashMap<>();

    @Nullable
    public NetworkTagData get(String serverName) {
        return this.serverFonts.get(serverName);
    }

    @Nullable
    public NetworkTagData get(ProxyPlayer player) {
        return Optional.ofNullable(player.server())
                .map(BackendServer::name)
                .map(this::get)
                .orElse(null);
    }

    public void put(String serverName, NetworkTagData netWorkTagData) {
        this.serverFonts.put(serverName, netWorkTagData);
    }

    public void clear() {
        this.serverFonts.clear();
    }
}
