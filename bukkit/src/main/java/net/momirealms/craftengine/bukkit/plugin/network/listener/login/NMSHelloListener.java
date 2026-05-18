package net.momirealms.craftengine.bukkit.plugin.network.listener.login;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.login.ServerboundHelloPacketProxy;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class NMSHelloListener implements NMSPacketListener {
    public static final NMSPacketListener INSTANCE = new NMSHelloListener();

    private NMSHelloListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, NMSPacketEvent event, Object packet) {
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        Object buf = event.getPacket();
        String name = ServerboundHelloPacketProxy.INSTANCE.getName(buf);
        player.setUnverifiedName(name);
        if (VersionHelper.isOrAbove1_20_2) {
            player.setUnverifiedUUID(ServerboundHelloPacketProxy.INSTANCE.getProfileId(packet));
        } else {
            Optional<UUID> uuid = ServerboundHelloPacketProxy.INSTANCE.getProfileId$legacy(packet);
            if (uuid.isPresent()) {
                player.setUnverifiedUUID(uuid.get());
            } else {
                player.setUnverifiedUUID(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8)));
            }
        }
    }
}
