package net.momirealms.craftengine.proxy.bungeecord.platform;

import net.md_5.bungee.api.connection.Server;
import net.momirealms.craftengine.proxy.common.platform.BackendServer;

public class BungeeBackendServer implements BackendServer {
    private final Server platform;

    private BungeeBackendServer(Server platform) {
        this.platform = platform;
    }

    public static BungeeBackendServer wrapper(Server platform) {
        return new BungeeBackendServer(platform);
    }

    @Override
    public String name() {
        return this.platform.getInfo().getName();
    }
}
