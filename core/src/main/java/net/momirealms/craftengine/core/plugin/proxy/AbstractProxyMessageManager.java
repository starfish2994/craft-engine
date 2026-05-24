package net.momirealms.craftengine.core.plugin.proxy;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.mod.CustomPackets;
import net.momirealms.craftengine.core.plugin.proxy.packet.ProxyboundNetworkTagDataPacket;
import net.momirealms.craftengine.core.plugin.proxy.packet.ServerboundNetworkTagDataVersionPacket;
import net.momirealms.craftengine.core.util.ConcurrentUUID2ReferenceChainedHashTable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class AbstractProxyMessageManager implements ProxyMessageManager {
    protected final CraftEngine plugin;
    protected final ConcurrentUUID2ReferenceChainedHashTable<ConcurrentUUID2ReferenceChainedHashTable<Object>> proxyPlayers;
    protected final ConcurrentUUID2ReferenceChainedHashTable<UUID> proxyByPlayer;
    protected long networkTagDataVersion = System.currentTimeMillis();

    public AbstractProxyMessageManager(CraftEngine plugin) {
        this.plugin = plugin;
        this.proxyPlayers = new ConcurrentUUID2ReferenceChainedHashTable<>(); // ProxyUUID -> Set<PlayerUUID>
        this.proxyByPlayer = new ConcurrentUUID2ReferenceChainedHashTable<>(); // PlayerUUID -> ProxyUUID
    }

    @Override
    public void delayedInit() {
        CustomPackets.registerClientbound(ProxyboundNetworkTagDataPacket.ID, ProxyboundNetworkTagDataPacket.CODEC, CustomPackets.ALWAYS_ALLOWED, false);
        CustomPackets.registerServerbound(ServerboundNetworkTagDataVersionPacket.ID, ServerboundNetworkTagDataVersionPacket.CODEC, CustomPackets.ALWAYS_ALLOWED);
    }

    @Override
    public void load() {
        this.networkTagDataVersion = System.currentTimeMillis();
        ProxyboundNetworkTagDataPacket.rebuildDataCache();
        for (ConcurrentUUID2ReferenceChainedHashTable<Object> set : this.proxyPlayers.values()) {
            for (ConcurrentUUID2ReferenceChainedHashTable.TableEntry<Object> entry : set) {
                NetWorkUser user = CraftEngine.instance().networkManager().getOnlineUser(entry.getKey());
                if (user == null) continue;
                ProxyboundNetworkTagDataPacket.sendData(user);
            }
        }
    }

    @Override
    public void disable() {
        this.proxyPlayers.clear();
        this.proxyByPlayer.clear();
    }

    @Override
    public void addUser(@NotNull UUID userUUID, UUID proxyUUID) {
        this.proxyPlayers.computeIfAbsent(proxyUUID, it -> new ConcurrentUUID2ReferenceChainedHashTable<>()).put(userUUID, Boolean.TRUE);
        this.proxyByPlayer.put(userUUID, proxyUUID);
    }

    @Override
    public void removeUser(@NotNull UUID userUUID) {
        UUID removed = this.proxyByPlayer.remove(userUUID);
        if (removed == null) return;
        var entries = this.proxyPlayers.get(removed);
        if (entries == null) return;
        entries.remove(userUUID);
    }

    @Override
    public long networkTagDataVersion() {
        return this.networkTagDataVersion;
    }
}
