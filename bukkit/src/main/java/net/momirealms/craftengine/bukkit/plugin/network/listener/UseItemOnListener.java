package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;

public class UseItemOnListener implements ByteBufferPacketListener {
    public static final UseItemOnListener INSTANCE = new UseItemOnListener();

    private UseItemOnListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, ByteBufPacketEvent event) {
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        player.stopMiningBlock();
    }
}
