package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.sparrow.nbt.Tag;

import java.util.Map;

public class SetSubtitleListener1_20_3 implements ByteBufferPacketListener {
    public static final SetSubtitleListener1_20_3 INSTANCE = new SetSubtitleListener1_20_3();

    private SetSubtitleListener1_20_3() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptTitle()) return;
        FriendlyByteBuf buf = event.getBuffer();
        Tag nbt = buf.readNbt(false);
        if (nbt == null) return;
        Map<String, ComponentProvider> tokens = BukkitNetworkManager.instance().matchNetworkTags(nbt);
        if (tokens.isEmpty()) return;
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeNbt(AdventureHelper.componentToTag(AdventureHelper.replaceText(AdventureHelper.tagToComponent(nbt), tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user))), false);
    }
}
