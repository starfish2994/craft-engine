package net.momirealms.craftengine.proxy.common;

import net.momirealms.craftengine.proxy.common.network.listener.PacketListenerManager;
import net.momirealms.craftengine.proxy.common.platform.ProxyPlayer;
import net.momirealms.craftengine.proxy.common.tag.NetworkTagDataSyncService;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public interface ProxyCraftEngine {

    @Nullable
    ProxyPlayer getPlayer(UUID uuid);

    void registerChannel(String channel);

    File dataFolderFile();

    Path dataFolderPath();

    PacketListenerManager packetListenerManager();

    NetworkTagDataSyncService networkTagDataSyncService();
}
