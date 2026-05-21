package net.momirealms.craftengine.bukkit.plugin.proxy.packet;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.proxy.ProxyMessageManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.mod.ServerCustomPacket;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.util.UUID;

public record ServerboundNetworkTagDataVersionPacket(long proxyTagDataVersion, UUID proxyUuid) implements ServerCustomPacket {
    public static final Key ID = Key.ce("tag_data");
    public static final NetworkCodec<FriendlyByteBuf, ServerboundNetworkTagDataVersionPacket> CODEC = ServerCustomPacket.codec(
            (buf, packet) -> {},
            ServerboundNetworkTagDataVersionPacket::decode
    );

    private static ServerboundNetworkTagDataVersionPacket decode(FriendlyByteBuf buf) {
        long dataVersion = buf.readLong();
        UUID proxyUUID = buf.readUUID();
        return new ServerboundNetworkTagDataVersionPacket(dataVersion, proxyUUID);
    }

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ? extends ServerCustomPacket> codec() {
        return CODEC;
    }

    /**
     * 当收到玩家进服后的数据版本号和代理UUID, 记录玩家所在的代理UUID, 并根据版本号决定是否要重发TagData包回去.
     */
    @Override
    public void handle(NetWorkUser user, ByteBufPacketEvent event) {
        ProxyMessageManager manager = BukkitCraftEngine.instance().proxyMessageManager();
        manager.recordPlayerBelongProxy((BukkitServerPlayer) user, this.proxyUuid);
        if (this.proxyTagDataVersion != manager.networkTagDataVersion()) {
            user.sendCustomPacket(new ProxyboundNetworkTagDataPacket());
        }
    }
}
