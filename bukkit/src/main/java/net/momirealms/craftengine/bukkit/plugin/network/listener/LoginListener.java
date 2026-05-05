package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundLoginPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.CommonPlayerSpawnInfoProxy;
import net.momirealms.craftengine.proxy.minecraft.resources.ResourceKeyProxy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.util.Objects;

public class LoginListener implements NMSPacketListener {
    public static final LoginListener INSTANCE = new LoginListener();

    private LoginListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, NMSPacketEvent event, Object packet) {
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        Object dimensionKey;
        if (VersionHelper.isOrAbove1_20_2()) {
            Object commonInfo = ClientboundLoginPacketProxy.INSTANCE.getCommonPlayerSpawnInfo(packet);
            dimensionKey = CommonPlayerSpawnInfoProxy.INSTANCE.getDimension(commonInfo);
        } else {
            dimensionKey = ClientboundLoginPacketProxy.INSTANCE.getDimension(packet);
        }
        Object identifier = ResourceKeyProxy.INSTANCE.getIdentifier(dimensionKey);
        World world = Bukkit.getWorld(Objects.requireNonNull(NamespacedKey.fromString(identifier.toString())));
        if (world != null) {
            player.setClientSideWorld(BukkitAdaptor.adapt(world));
        }
        if (VersionHelper.isOrAbove1_20_5() && Config.disableChatReport()) {
            // 去除弹窗警告
            ClientboundLoginPacketProxy.INSTANCE.setEnforcesSecureChat(packet, true);
        }
    }
}
