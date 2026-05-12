package net.momirealms.craftengine.proxy.common.tag;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.proxy.common.ProxyCraftEngine;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import net.momirealms.craftengine.proxy.common.util.Key;
import net.momirealms.craftengine.proxy.common.util.ProxyByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class NetworkTagDataSyncService {
    public static final String TAG_DATA_CHANNEL = "craftengine:tag_data";
    public static final Key TAG_DATA_CHANNEL_KEY = Key.ce("tag_data");
    public static final UUID PROXY_UUID = UUID.randomUUID();
    private final ProxyCraftEngine plugin;
    private final NetworkTagDataRegistry registry;

    public NetworkTagDataSyncService(ProxyCraftEngine plugin) {
        this.plugin = plugin;
        this.registry = new NetworkTagDataRegistry();
        this.plugin.registerChannel(TAG_DATA_CHANNEL); // 必须注册, 否则会在 CustomPayloadListener 处理之前就被 Proxy 拦截.
    }

    public NetworkTagDataRegistry registry() {
        return this.registry;
    }

    @Nullable
    public NetworkTagData getTagData(ProxyPlayer player) {
        return this.registry.get(player);
    }

    @Nullable
    public NetworkTagData getTagData(String serverName) {
        return this.registry.get(serverName);
    }

    public byte[] buildTagDataBytes(@Nullable NetworkTagData networkTagData) {
        long version = networkTagData != null ? networkTagData.version() : -1L;
        ProxyByteBuf buf = new ProxyByteBuf(Unpooled.buffer());
        buf.writeLong(version);
        buf.writeUUID(PROXY_UUID);
        return buf.array();
    }

    public void receiveTagData(String serverName, ProxyByteBuf in) {
        this.registry.put(serverName, NetworkTagDataDeserializer.read(in, this.registry, serverName));
    }

    public void clear() {
        this.registry.clear();
    }
}
