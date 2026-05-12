package net.momirealms.craftengine.proxy.common.network.packet;

import net.momirealms.craftengine.proxy.common.network.protocol.ConnectionState;
import net.momirealms.craftengine.proxy.common.network.protocol.PacketSide;
import net.momirealms.craftengine.proxy.common.network.protocol.player.ClientVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public final class PacketHandlerRegistry {
    private final PacketHandler[][][][] handlers =
            new PacketHandler[PacketSide.values().length][ConnectionState.values().length][ClientVersion.values().length][];

    private PacketHandlerRegistry() {}

    public static PacketHandlerRegistry create() {
        return new PacketHandlerRegistry();
    }

    // 对全部支持的客户端版本注册包监听器.
    public PacketRegistration register(@NotNull PacketRoute route, @NotNull PacketHandler handler) {
        return this.register(route, handler, version -> true);
    }

    // 对单个支持的客户端版本注册包监听器.
    public PacketRegistration register(@NotNull PacketRoute route, @NotNull ClientVersion version, @NotNull PacketHandler handler) {
        Objects.requireNonNull(version, "version");
        this.ensureReleaseVersion(version, "version");
        return this.register(route, handler, candidate -> candidate == version);
    }

    // 对某个特定及以上的版本注册包监听器.
    public PacketRegistration registerSince(@NotNull PacketRoute route, @NotNull ClientVersion minInclusive, @NotNull PacketHandler handler) {
        Objects.requireNonNull(minInclusive, "minInclusive");
        this.ensureReleaseVersion(minInclusive, "minInclusive");
        return this.register(route, handler, version -> version.isNewerThanOrEquals(minInclusive));
    }

    // 对特定区间的版本注册包监听器.
    public PacketRegistration registerBetween(@NotNull PacketRoute route, @NotNull ClientVersion minInclusive, @NotNull ClientVersion maxInclusive, @NotNull PacketHandler handler) {
        Objects.requireNonNull(minInclusive, "minInclusive");
        Objects.requireNonNull(maxInclusive, "maxInclusive");
        this.ensureReleaseVersion(minInclusive, "minInclusive");
        this.ensureReleaseVersion(maxInclusive, "maxInclusive");
        if (minInclusive.isNewerThan(maxInclusive)) {
            throw new IllegalArgumentException("minInclusive must be older than or equal to maxInclusive");
        }
        return this.register(route, handler, version -> version.isNewerThanOrEquals(minInclusive) && version.isOlderThanOrEquals(maxInclusive));
    }

    private PacketRegistration register(PacketRoute route, PacketHandler handler, Predicate<ClientVersion> versionPredicate) {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(handler, "handler");
        Objects.requireNonNull(versionPredicate, "versionPredicate");

        synchronized (this) {
            this.ensureAvailable(route, versionPredicate);
            this.setPacketHandlers(route, handler, versionPredicate);
        }

        return new PacketRegistration(route, handler, () -> this.unregister(route, handler, versionPredicate));
    }

    private void ensureReleaseVersion(ClientVersion version, String parameterName) {
        if (!version.isRelease()) {
            throw new IllegalArgumentException(parameterName + " must be a release client version");
        }
    }

    private void ensureAvailable(PacketRoute route, Predicate<ClientVersion> versionPredicate) {
        for (ClientVersion version : ClientVersion.values()) {
            if (!version.isRelease() || !versionPredicate.test(version)) {
                continue;
            }
            int packetId = route.packetId(version);
            if (packetId >= 0) {
                PacketHandler existing = this.getPacketHandler(route.side(), route.state(), version, packetId);
                if (existing != null) {
                    throw new IllegalStateException("Packet handler already registered for " + route.side() + "/" + route.state() + "/" + version + "/" + packetId);
                }
            }
        }
    }

    private void setPacketHandlers(PacketRoute route, PacketHandler handler, Predicate<ClientVersion> versionPredicate) {
        for (ClientVersion version : ClientVersion.values()) {
            if (!version.isRelease() || !versionPredicate.test(version)) {
                continue;
            }
            int packetId = route.packetId(version);
            if (packetId >= 0) {
                this.setPacketHandler(route.side(), route.state(), version, packetId, handler);
            }
        }
    }

    private synchronized void unregister(PacketRoute route, PacketHandler handler, Predicate<ClientVersion> versionPredicate) {
        for (ClientVersion version : ClientVersion.values()) {
            if (!version.isRelease() || !versionPredicate.test(version)) {
                continue;
            }
            int packetId = route.packetId(version);
            if (packetId >= 0 && this.getPacketHandler(route.side(), route.state(), version, packetId) == handler) {
                this.clearPacketHandler(route.side(), route.state(), version, packetId);
            }
        }
    }

    public @Nullable PacketHandler getPacketHandler(PacketSide side, ConnectionState state, ClientVersion version, int packetId) {
        if (side == null || state == null || packetId < 0 || version == null) {
            return null;
        }
        ClientVersion mappedVersion = version == ClientVersion.UNKNOWN ? ClientVersion.getLatest() : version;
        if (mappedVersion.isUnsupported()) {
            return null;
        }
        PacketHandler[] packetHandlers = this.handlers[side.ordinal()][state.ordinal()][mappedVersion.ordinal()];
        if (packetHandlers == null || packetId >= packetHandlers.length) {
            return null;
        }
        return packetHandlers[packetId];
    }

    private synchronized void setPacketHandler(PacketSide side, ConnectionState state, ClientVersion version, int packetId, PacketHandler handler) {
        PacketHandler[][][] sideHandlers = this.handlers[side.ordinal()];
        PacketHandler[][] stateHandlers = sideHandlers[state.ordinal()];
        PacketHandler[] versionHandlers = stateHandlers[version.ordinal()];

        if (versionHandlers == null) {
            versionHandlers = new PacketHandler[Math.max(packetId + 1, 4)];
            stateHandlers[version.ordinal()] = versionHandlers;
        } else if (packetId >= versionHandlers.length) {
            versionHandlers = Arrays.copyOf(versionHandlers, packetId + 1);
            stateHandlers[version.ordinal()] = versionHandlers;
        }

        versionHandlers[packetId] = handler;
    }

    private synchronized void clearPacketHandler(PacketSide side, ConnectionState state, ClientVersion version, int packetId) {
        if (side == null || state == null || packetId < 0) {
            return;
        }
        ClientVersion mappedVersion = version == null || !version.isRelease() ? ClientVersion.getLatest() : version;
        PacketHandler[] versionHandlers = this.handlers[side.ordinal()][state.ordinal()][mappedVersion.ordinal()];
        if (versionHandlers == null || packetId >= versionHandlers.length) {
            return;
        }
        versionHandlers[packetId] = null;
    }
}
