package net.momirealms.craftengine.bukkit.plugin.proxy.packet;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.font.NetworkTagDataSerializer;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.mod.ClientCustomPacket;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public record ProxyboundNetworkTagDataPacket(int total, int index, byte[] data) implements ClientCustomPacket {
    private static final int PAGE_LENGTH = 1048576 - 8 - 4 - 4 ;
    public static final Key ID = Key.ce("tag_data");
    public static final ProxyboundNetworkTagDataPacket EMPTY = new ProxyboundNetworkTagDataPacket(1, 1, new byte[0]);
    public static final NetworkCodec<FriendlyByteBuf, ProxyboundNetworkTagDataPacket> CODEC = ClientCustomPacket.codec(
            ProxyboundNetworkTagDataPacket::encode,
            buf -> ProxyboundNetworkTagDataPacket.EMPTY
    );

    private static byte[][] dataCache = null;

    private void encode(FriendlyByteBuf buf) {
        buf.writeLong(BukkitCraftEngine.instance().proxyMessageManager().networkTagDataVersion()); // Version
        buf.writeInt(this.total);
        buf.writeInt(this.index);
        buf.writeBytes(this.data);
    }

    @Override
    public Key id() {
        return ProxyboundNetworkTagDataPacket.ID;
    }

    @Override
    public NetworkCodec<FriendlyByteBuf, ? extends ClientCustomPacket> codec() {
        return ProxyboundNetworkTagDataPacket.CODEC;
    }

    // 刷新缓存
    public static void refreshDataCache() {
        byte[] dataCache = ProxyboundNetworkTagDataPacket.buildDataCache();

        int num = dataCache.length / PAGE_LENGTH;
        int total = dataCache.length % PAGE_LENGTH == 0 ? num : num + 1;

        int index = 0;
        byte[][] data = new byte[total][];
        for (int i = 0; i < dataCache.length; i += PAGE_LENGTH) {
            int end = Math.min(dataCache.length, i + PAGE_LENGTH);
            byte[] chunk = new byte[end - i];
            System.arraycopy(dataCache, i, chunk, 0, end - i);
            data[index++] = chunk;
        }
        ProxyboundNetworkTagDataPacket.dataCache = data;
    }

    // 构建数据
    private static byte[] buildDataCache0() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        NetworkTagDataSerializer.writeOffsetFont(buf, BukkitCraftEngine.instance().fontManager().offsetFont());
        NetworkTagDataSerializer.writeImages(buf, BukkitCraftEngine.instance().fontManager().loadedImages());
        NetworkTagDataSerializer.writeL10n(buf, BukkitCraftEngine.instance().translationManager());
        NetworkTagDataSerializer.writeGlobalVariables(buf, BukkitCraftEngine.instance().globalVariableManager());
        return buf.array();
    }

    //发送数据
    public static void sendData(NetWorkUser user) {
        int total = ProxyboundNetworkTagDataPacket.dataCache.length;
        for (int i = 0; i < total; i++) {
            user.sendCustomPacket(new ProxyboundNetworkTagDataPacket(total, i, ProxyboundNetworkTagDataPacket.dataCache[i]));
        }
    }
}
