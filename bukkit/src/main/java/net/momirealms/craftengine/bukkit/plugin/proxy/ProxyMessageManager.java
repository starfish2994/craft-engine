package net.momirealms.craftengine.bukkit.plugin.proxy;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.font.NetworkTagDataSerializer;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ProxyMessageManager implements Listener {
    public static final Key TAG_DATA_IDENTIFIER = Key.ce("tag_data");
    public static final boolean ENABLE_PROXY = Bukkit.getServer().getServerConfig().isProxyEnabled();
    private final BukkitCraftEngine plugin;
    private final Map<UUID, Set<UUID>> proxyPlayers = new ConcurrentHashMap<>(); // ProxyUUID -> Set<PlayerUUID>
    private final Map<UUID, UUID> proxyByPlayer = new ConcurrentHashMap<>(); // PlayerUUID -> ProxyUUID
    private long networkTagDataVersion = System.currentTimeMillis();
    private FriendlyByteBuf tagDataBufCache;

    public ProxyMessageManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        if (ENABLE_PROXY) {
            Bukkit.getPluginManager().registerEvents(this, plugin.javaPlugin());
        }
    }

    // 插件重载, 让每个玩家都刷新自身链接的代理服务器.
    @EventHandler
    public void onPluginReload(CraftEngineReloadEvent event) {
        this.networkTagDataVersion = System.currentTimeMillis();
        this.tagDataBufCache = this.buildNetworkTagDataBuf();
        this.proxyPlayers.values().forEach(set -> {
            for (UUID playerUUID : set) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isConnected()) {
                    BukkitServerPlayer bukkitServerPlayer = BukkitAdaptor.adapt(player);
                    if (bukkitServerPlayer != null) {
                        this.updateNetworkTagData(bukkitServerPlayer);
                        break;
                    }
                }
            }
        });
    }

    // 玩家离开服务器, 清理缓存数据
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        Optional.ofNullable(this.proxyByPlayer.remove(playerUUID))
                .map(this.proxyPlayers::get)
                .map(it -> it.remove(playerUUID));
    }

    /**
     * NetworkTagData
     */
    // 当收到玩家进服后的数据版本号, 决定是否要重发包回去.
    public void handleTagDataVersionFromProxy(@NotNull BukkitServerPlayer player, FriendlyByteBuf buf) {
        long dataVersion = buf.readLong();
        UUID proxyUUID = buf.readUUID();
        // 记录玩家所在的代理服务器.
        proxyPlayers.computeIfAbsent(proxyUUID, it -> ConcurrentHashMap.newKeySet()).add(player.uuid());
        proxyByPlayer.put(player.uuid(), proxyUUID);
        // 更新字体数据.
        if (dataVersion != this.networkTagDataVersion) {
            this.updateNetworkTagData(player);
        }
    }

    private void updateNetworkTagData(BukkitServerPlayer player) {
        if (this.tagDataBufCache == null) {
            this.tagDataBufCache = this.buildNetworkTagDataBuf();
        }
        player.sendCustomPayload(TAG_DATA_IDENTIFIER, this.tagDataBufCache.array());
    }

    private FriendlyByteBuf buildNetworkTagDataBuf() {
        FriendlyByteBuf byteBuf = new FriendlyByteBuf(Unpooled.buffer());
        byteBuf.writeLong(System.currentTimeMillis()); // Version
        NetworkTagDataSerializer.writeOffsetFont(byteBuf, this.plugin.fontManager().offsetFont());
        NetworkTagDataSerializer.writeImages(byteBuf, this.plugin.fontManager().loadedImages());
        NetworkTagDataSerializer.writeL10n(byteBuf, this.plugin.translationManager());
        NetworkTagDataSerializer.writeGlobalVariables(byteBuf, this.plugin.globalVariableManager());
        return byteBuf;
    }
}
