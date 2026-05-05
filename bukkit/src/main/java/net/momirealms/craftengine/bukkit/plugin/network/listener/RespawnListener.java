package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundRespawnPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.CommonPlayerSpawnInfoProxy;
import net.momirealms.craftengine.proxy.minecraft.resources.ResourceKeyProxy;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.util.Objects;

public class RespawnListener implements NMSPacketListener {
    public static final RespawnListener INSTANCE = new RespawnListener();

    private RespawnListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, NMSPacketEvent event, Object packet) {
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        player.clearView();
        Object dimensionKey;
        if (VersionHelper.isOrAbove1_20_2()) {
            Object commonInfo = ClientboundRespawnPacketProxy.INSTANCE.getCommonPlayerSpawnInfo(packet);
            dimensionKey = CommonPlayerSpawnInfoProxy.INSTANCE.getDimension(commonInfo);
        } else {
            dimensionKey = ClientboundRespawnPacketProxy.INSTANCE.getDimension(packet);
        }
        Object identifier = ResourceKeyProxy.INSTANCE.getIdentifier(dimensionKey);
        World world = Bukkit.getWorld(Objects.requireNonNull(NamespacedKey.fromString(identifier.toString())));
        if (world != null) {
            player.setClientSideWorld(BukkitAdaptor.adapt(world));
            player.clearTrackedChunks();
            player.furnitureLightData().clearLightData();
            player.clearTrackedBlockEntities();
            player.clearTrackedEntities();
        }
    }
}
