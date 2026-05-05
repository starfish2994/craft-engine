package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacketProxy;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class PlayerInfoUpdateListener implements NMSPacketListener {
    public static final PlayerInfoUpdateListener INSTANCE = new PlayerInfoUpdateListener();

    private PlayerInfoUpdateListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, NMSPacketEvent event, Object packet) {
        if (!Config.interceptPlayerInfo()) return;
        List<Object> entries = ClientboundPlayerInfoUpdatePacketProxy.INSTANCE.getEntries(packet);
        EnumSet<? extends Enum<?>> enums = ClientboundPlayerInfoUpdatePacketProxy.INSTANCE.getActions(packet);
        if (!enums.contains(ClientboundPlayerInfoUpdatePacketProxy.ActionProxy.UPDATE_DISPLAY_NAME)) return;
        for (Object entry : entries) {
            Object mcComponent = ClientboundPlayerInfoUpdatePacketProxy.EntryProxy.INSTANCE.getDisplayName(entry);
            if (mcComponent == null) continue;
            String json = ComponentUtils.minecraftToJson(mcComponent);
            Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(json);
            if (tokens.isEmpty()) continue;
            ClientboundPlayerInfoUpdatePacketProxy.EntryProxy.INSTANCE.setDisplayName(
                    entry,
                    ComponentUtils.adventureToMinecraft(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(json), tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user)))
            );
        }
    }
}
