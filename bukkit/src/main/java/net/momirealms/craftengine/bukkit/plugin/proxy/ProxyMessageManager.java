package net.momirealms.craftengine.bukkit.plugin.proxy;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.proxy.packet.ProxyboundNetworkTagDataPacket;
import net.momirealms.craftengine.bukkit.plugin.proxy.packet.ServerboundNetworkTagDataVersionPacket;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.network.mod.CustomPackets;
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
    public static final boolean ENABLE_PROXY = Bukkit.getServer().getServerConfig().isProxyEnabled();
    private final BukkitCraftEngine plugin;
    private final Map<UUID, Set<UUID>> proxyPlayers = new ConcurrentHashMap<>(); // ProxyUUID -> Set<PlayerUUID>
    private final Map<UUID, UUID> proxyByPlayer = new ConcurrentHashMap<>(); // PlayerUUID -> ProxyUUID
    private long networkTagDataVersion = System.currentTimeMillis();

    public ProxyMessageManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        if (ENABLE_PROXY) {
            Bukkit.getPluginManager().registerEvents(this, plugin.javaPlugin());
            CustomPackets.registerClientbound(ProxyboundNetworkTagDataPacket.ID, ProxyboundNetworkTagDataPacket.CODEC, CustomPackets.ALWAYS_ALLOWED, false);
            CustomPackets.registerServerbound(ServerboundNetworkTagDataVersionPacket.ID, ServerboundNetworkTagDataVersionPacket.CODEC, CustomPackets.ALWAYS_ALLOWED);
        }
    }

    // 插件重载, 让每个玩家都刷新自身链接的代理服务器.
    @EventHandler
    public void onPluginReload(CraftEngineReloadEvent event) {
        this.networkTagDataVersion = System.currentTimeMillis();
        ProxyboundNetworkTagDataPacket.refreshDataCache();
        this.proxyPlayers.values().forEach(set -> {
            for (UUID playerUUID : set) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isConnected()) {
                    BukkitServerPlayer bukkitServerPlayer = BukkitAdaptor.adapt(player);
                    if (bukkitServerPlayer != null) {
                        bukkitServerPlayer.sendCustomPacket(new ProxyboundNetworkTagDataPacket());
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

    // 记录玩家所在的代理服务器.
    public void recordPlayerBelongProxy(@NotNull BukkitServerPlayer player, UUID proxyUUID) {
        this.proxyPlayers.computeIfAbsent(proxyUUID, it -> ConcurrentHashMap.newKeySet()).add(player.uuid());
        this.proxyByPlayer.put(player.uuid(), proxyUUID);
    }

    public long networkTagDataVersion() {
        return networkTagDataVersion;
    }
}
