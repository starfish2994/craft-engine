package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.font.BukkitFontManager;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.font.EmojiTextProcessResult;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.NMSPacketListener;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.chat.ChatTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.ComponentProxy;
import net.momirealms.craftengine.proxy.minecraft.network.chat.SignedMessageBodyProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundPlayerChatPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundSystemChatPacketProxy;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PlayerChatListener implements NMSPacketListener {
    public static final PlayerChatListener INSTANCE = new PlayerChatListener();

    private PlayerChatListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, NMSPacketEvent event, Object packet) {
        if (!Config.disableChatReport()) {
            return;
        }
        event.setCancelled(true);
        Object content = ClientboundPlayerChatPacketProxy.INSTANCE.getUnsignedContent(packet);
        if (content == null) {
            content = ComponentProxy.INSTANCE.literal(
                    SignedMessageBodyProxy.PackedProxy.INSTANCE.getContent(
                            ClientboundPlayerChatPacketProxy.INSTANCE.getBody(packet)
                    )
            );
        }
        Object chatType = ClientboundPlayerChatPacketProxy.INSTANCE.getChatType(packet);
        if (!VersionHelper.isOrAbove1_20_5()) {
            Object registryAccess = RegistryUtils.getRegistryAccess();
            chatType = ChatTypeProxy.BoundNetworkProxy.INSTANCE.resolve(chatType, registryAccess).orElseThrow();
        }
        Object decorate = ChatTypeProxy.BoundProxy.INSTANCE.decorate(chatType, content);
        if (Config.allowEmojiChat()) {
            String rawJsonMessage = ComponentUtils.minecraftToJson(decorate);
            UUID sender = ClientboundPlayerChatPacketProxy.INSTANCE.getSender(packet);
            @Nullable Player chatSender = CraftEngine.instance().platform().getPlayer(sender);
            EmojiTextProcessResult result = BukkitFontManager.instance().replaceJsonEmoji(rawJsonMessage, chatSender);
            if (result.replaced()) {
                decorate = ComponentUtils.jsonToMinecraft(result.text());
            }
        }
        Object systemChatPacket = ClientboundSystemChatPacketProxy.INSTANCE.newInstance(decorate, false);
        user.sendPacket(systemChatPacket, false);
    }
}
