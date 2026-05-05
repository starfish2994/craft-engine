package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.common.ServerboundClientInformationPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.server.level.ClientInformationProxy;

public class ClientInformationListener implements NMSPacketListener {
    public static final ClientInformationListener INSTANCE = new ClientInformationListener();

    private ClientInformationListener() {}

    @Override
    public void onPacketReceive(NetWorkUser user, NMSPacketEvent event, Object packet) {
        if (VersionHelper.isOrAbove1_20_2()) {
            Object clientInfo = ServerboundClientInformationPacketProxy.INSTANCE.getInformation(packet);
            if (clientInfo == null) return;
            String locale = ClientInformationProxy.INSTANCE.getLanguage(clientInfo);
            if (locale == null) return;
            ((BukkitServerPlayer) user).setClientLocale(TranslationManager.parseLocale(locale));
        } else {
            String locale = ServerboundClientInformationPacketProxy.INSTANCE.getLanguage(packet);
            if (locale == null) return;
            ((BukkitServerPlayer) user).setClientLocale(TranslationManager.parseLocale(locale));
        }
    }
}
