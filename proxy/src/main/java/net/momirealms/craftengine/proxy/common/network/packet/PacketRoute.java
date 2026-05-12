package net.momirealms.craftengine.proxy.common.network.packet;

import net.momirealms.craftengine.proxy.common.network.protocol.ConnectionState;
import net.momirealms.craftengine.proxy.common.network.protocol.PacketSide;
import net.momirealms.craftengine.proxy.common.network.protocol.packettype.PacketTypeCommon;
import net.momirealms.craftengine.proxy.common.network.protocol.player.ClientVersion;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record PacketRoute(
        PacketSide side,
        ConnectionState state,
        PacketTypeCommon packetType
) {

    public PacketRoute {
        Objects.requireNonNull(side, "side");
        Objects.requireNonNull(state, "state");
        if (packetType == null) {
            throw new IllegalArgumentException("Raw packet routes require a non-negative packet id");
        }
    }

    public static PacketRoute typed(ConnectionState state, PacketTypeCommon packetType) {
        Objects.requireNonNull(packetType, "packetType");
        return new PacketRoute(packetType.getSide(), state, packetType);
    }

    public boolean typed() {
        return this.packetType != null;
    }

    public int packetId(ClientVersion version) {
        return this.packetType.getId(version);
    }
}
