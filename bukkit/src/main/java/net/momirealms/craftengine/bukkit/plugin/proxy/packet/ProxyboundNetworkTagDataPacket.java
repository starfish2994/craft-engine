package net.momirealms.craftengine.bukkit.plugin.proxy.packet;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.font.NetworkTagDataSerializer;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.mod.ClientCustomPacket;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record ProxyboundNetworkTagDataPacket() implements ClientCustomPacket {
    public static final Key ID = Key.ce("tag_data");
    public static final ProxyboundNetworkTagDataPacket EMPTY = new ProxyboundNetworkTagDataPacket();
    public static final NetworkCodec<FriendlyByteBuf, ProxyboundNetworkTagDataPacket> CODEC = ClientCustomPacket.codec(
            ProxyboundNetworkTagDataPacket::encode,
            buf -> ProxyboundNetworkTagDataPacket.EMPTY
    );
    private static byte[] CACHED_BYTES = null;

    private void encode(FriendlyByteBuf buf) {
        if (CACHED_BYTES == null) {
            CACHED_BYTES = buildDataCache0();
        }
        buf.writeBytes(CACHED_BYTES);
    }

    @Override
    public Key id() {
        return ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ? extends ClientCustomPacket> codec() {
        return CODEC;
    }

    // 刷新缓存
    public static void buildDataCache() {
        CACHED_BYTES = buildDataCache0();
    }

    // 构建数据
    private static byte[] buildDataCache0() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeLong(BukkitCraftEngine.instance().proxyMessageManager().networkTagDataVersion()); // Version
        NetworkTagDataSerializer.writeOffsetFont(buf, BukkitCraftEngine.instance().fontManager().offsetFont());
        NetworkTagDataSerializer.writeImages(buf, BukkitCraftEngine.instance().fontManager().loadedImages());
        NetworkTagDataSerializer.writeL10n(buf, BukkitCraftEngine.instance().translationManager());
        NetworkTagDataSerializer.writeGlobalVariables(buf, BukkitCraftEngine.instance().globalVariableManager());
        return buf.array();
    }
}
