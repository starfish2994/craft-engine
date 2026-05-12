package net.momirealms.craftengine.proxy.velocity.platform;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.momirealms.craftengine.proxy.common.platform.BackendServer;

public class VelocityBackendServer implements BackendServer {
    private final RegisteredServer platform;

    private VelocityBackendServer(RegisteredServer platform) {
        this.platform = platform;
    }

    public static VelocityBackendServer wrapper(RegisteredServer platform) {
        return new VelocityBackendServer(platform);
    }

    @Override
    public String name() {
        return this.platform.getServerInfo().getName();
    }
}
