package net.momirealms.craftengine.proxy.common.network.packet;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PacketRegistration implements AutoCloseable {
    private final PacketRoute route;
    private final PacketHandler handler;
    private final Runnable unregisterAction;
    private final AtomicBoolean registered = new AtomicBoolean(true);

    PacketRegistration(PacketRoute route, PacketHandler handler, Runnable unregisterAction) {
        this.route = Objects.requireNonNull(route, "route");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.unregisterAction = Objects.requireNonNull(unregisterAction, "unregisterAction");
    }

    public PacketRoute route() {
        return this.route;
    }

    public PacketHandler handler() {
        return this.handler;
    }

    public void unregister() {
        if (this.registered.compareAndSet(true, false)) {
            this.unregisterAction.run();
        }
    }

    @Override
    public void close() {
        this.unregister();
    }
}
