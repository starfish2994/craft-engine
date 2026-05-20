package net.momirealms.craftengine.bukkit.plugin.network.listener.common;

import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.plugin.network.mod.ClientCustomPacket;
import net.momirealms.craftengine.core.plugin.network.mod.CustomPackets;
import net.momirealms.craftengine.core.plugin.network.mod.ServerCustomPacket;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public final class CustomPayloadListener implements ByteBufferPacketListener {
    public static final ByteBufferPacketListener INSTANCE = new CustomPayloadListener();
    
    private CustomPayloadListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
        FriendlyByteBuf buf = event.getBuffer();
        Key packetId = buf.readKey();
        NetworkCodec<FriendlyByteBuf, ? extends ServerCustomPacket> codec = BuiltInRegistries.SERVER_MOD_PACKET.getValue(packetId);
        if (codec == null) return;
        if (CustomPackets.checkPermission(user, packetId, false)) {
            ServerCustomPacket packet = codec.decode(buf);
            packet.handle(user, event);
        }
    }

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        FriendlyByteBuf buf = event.getBuffer();
        Key packetId = buf.readKey();
        NetworkCodec<FriendlyByteBuf, ? extends ClientCustomPacket> codec = BuiltInRegistries.CLIENT_MOD_PACKET.getValue(packetId);
        if (codec == null) return;
        if (CustomPackets.checkPermission(user, packetId, true)) {
            ClientCustomPacket packet = codec.decode(buf);
            packet.handle(user, event);
        }
    }
}
