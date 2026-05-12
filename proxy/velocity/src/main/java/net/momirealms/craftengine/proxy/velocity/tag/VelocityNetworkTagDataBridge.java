package net.momirealms.craftengine.proxy.velocity.tag;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.configuration.PlayerConfigurationEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.proxy.common.network.protocol.player.ClientVersion;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagData;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagDataSyncService;
import net.momirealms.craftengine.proxy.common.util.ProxyByteBuf;
import net.momirealms.craftengine.proxy.velocity.VelocityCraftEngine;

public class VelocityNetworkTagDataBridge {
    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from(NetworkTagDataSyncService.TAG_DATA_CHANNEL);
    private final VelocityCraftEngine plugin;
    private final NetworkTagDataSyncService networkTagDataSyncService;

    public VelocityNetworkTagDataBridge(VelocityCraftEngine plugin) {
        this.plugin = plugin;
        this.networkTagDataSyncService = new NetworkTagDataSyncService(plugin);
        this.load();
    }

    public NetworkTagDataSyncService networkTagDataSyncService() {
        return this.networkTagDataSyncService;
    }

    public void load() {
        this.plugin.server.getEventManager().register(this.plugin, this);
    }

    public void disable() {
        this.plugin.server.getEventManager().unregisterListener(this.plugin, this);
        this.networkTagDataSyncService.clear();
    }

    @Subscribe
    public void onPlayerStartConfiguration(PlayerConfigurationEvent event) {
        String serverName = event.server().getServerInfo().getName();
        NetworkTagData networkTagData = this.networkTagDataSyncService.getTagData(serverName);
        byte[] data = this.networkTagDataSyncService.buildTagDataBytes(networkTagData);
        event.server().sendPluginMessage(IDENTIFIER, data);
    }

    @Subscribe
    public void onPlayerPostConnected(ServerPostConnectEvent event) {
        if (event.getPlayer().getProtocolVersion().getProtocol() <= ClientVersion.V_1_20_3.getProtocolVersion()) {
            ServerConnection serverConnection = event.getPlayer().getCurrentServer().orElse(null);
            if (serverConnection == null) {
                return;
            }
            NetworkTagData networkTagData = this.networkTagDataSyncService.getTagData(serverConnection.getServerInfo().getName());
            byte[] data = this.networkTagDataSyncService.buildTagDataBytes(networkTagData);
            serverConnection.sendPluginMessage(IDENTIFIER, data);
        }
    }

    @Subscribe
    public void receiveTagData(PluginMessageEvent event) {
        if (!IDENTIFIER.equals(event.getIdentifier())) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return; // 忽略客户端发的消息

        ByteBuf buffer = Unpooled.buffer(event.getData().length);
        buffer.writeBytes(event.getData());
        ProxyByteBuf in = new ProxyByteBuf(buffer);
        String serverName = serverConnection.getServer().getServerInfo().getName();
        if (serverName == null) return;

        this.networkTagDataSyncService.receiveTagData(serverName, in);
    }
}
