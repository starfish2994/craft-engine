package net.momirealms.craftengine.core.plugin.network.mod.protocol;

import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodecs;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.mod.ServerCustomPacket;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.IntIdentityList;
import net.momirealms.craftengine.core.util.Key;

public record ServerboundHandshakePacket(int protocolVersion, int blockListSize) implements ServerCustomPacket {
    public static final Key ID = Key.ce("handshake");
    public static final NetworkCodec<FriendlyByteBuf, ServerboundHandshakePacket> CODEC = ServerCustomPacket.codec(
            (packet, buf) -> {
                NetworkCodecs.VAR_INTEGER.encode(buf, packet.protocolVersion);
                NetworkCodecs.VAR_INTEGER.encode(buf, packet.blockListSize);
            },
            buf -> new ServerboundHandshakePacket(
                    NetworkCodecs.VAR_INTEGER.decode(buf),
                    NetworkCodecs.VAR_INTEGER.decode(buf)
            )
    );

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ServerboundHandshakePacket> codec() {
        return CODEC;
    }

    @Override
    public void handle(NetWorkUser user, ByteBufPacketEvent event) {
        user.setClientModProtocol(this.protocolVersion);
        user.setClientBlockList(new IntIdentityList(this.blockListSize));
    }
}
