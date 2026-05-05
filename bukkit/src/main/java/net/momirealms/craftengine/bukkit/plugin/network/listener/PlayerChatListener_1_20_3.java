package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.kyori.adventure.text.Component;
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
import net.momirealms.craftengine.core.util.Pair;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.UUID;

public class PlayerChatListener_1_20_3 implements ByteBufferPacketListener {
    public static final PlayerChatListener_1_20_3 INSTANCE = new PlayerChatListener_1_20_3();

    private PlayerChatListener_1_20_3() {}

    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptPlayerChat() || Config.disableChatReport()) return;
        FriendlyByteBuf buf = event.getBuffer();
        boolean changed = false;
        int globalIndex = VersionHelper.isOrAbove1_21_5() ? buf.readVarInt() : -1;
        UUID sender = buf.readUUID();
        int index = buf.readVarInt();
        byte @Nullable [] messageSignature = buf.readNullable(b -> {
            byte[] bs = new byte[256];
            buf.readBytes(bs);
            return bs;
        });
        // SignedMessageBody.Packed start
        String content = buf.readUtf(256);
        Instant timeStamp = buf.readInstant();
        long salt = buf.readLong();
        // LastSeenMessages.Packed start
        ArrayList<Pair<Integer, byte[]>> lastSeen = buf.readCollection(FriendlyByteBuf.limitValue(ArrayList::new, 20), b -> {
            int i = b.readVarInt() - 1;
            if (i == -1) {
                byte[] bs = new byte[256];
                buf.readBytes(bs);
                return Pair.of(-1, bs);
            } else {
                return Pair.of(i, null);
            }
        });
        // LastSeenMessages.Packed end
        // SignedMessageBody.Packed end
        @Nullable Tag unsignedContent = buf.readNullable(b -> b.readNbt(false));
        BukkitNetworkManager networkManager = BukkitNetworkManager.instance();
        if (unsignedContent != null) {
            Map<String, ComponentProvider> tokens = networkManager.matchNetworkTags(unsignedContent);
            if (!tokens.isEmpty()) {
                Component component = AdventureHelper.tagToComponent(unsignedContent);
                component = AdventureHelper.replaceText(component, tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
                unsignedContent = AdventureHelper.componentToTag(component);
                changed = true;
            }
        }
        // FilterMask start
        int type = buf.readVarInt();
        BitSet mask = type == 2 /* PARTIALLY_FILTERED */ ? buf.readBitSet() : null;
        // FilterMask end
        // ChatType.Bound start
        int chatType = buf.readVarInt();
        Tag name = buf.readNbt(false);
        if (name != null) {
            Map<String, ComponentProvider> tokens = networkManager.matchNetworkTags(name);
            if (!tokens.isEmpty()) {
                Component component = AdventureHelper.tagToComponent(name);
                component = AdventureHelper.replaceText(component, tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
                name = AdventureHelper.componentToTag(component);
                changed = true;
            }
        }
        @Nullable Tag targetName = buf.readNullable(b -> b.readNbt(false));
        if (targetName != null) {
            Map<String, ComponentProvider> tokens = networkManager.matchNetworkTags(targetName);
            if (!tokens.isEmpty()) {
                Component component = AdventureHelper.tagToComponent(targetName);
                component = AdventureHelper.replaceText(component, tokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
                targetName = AdventureHelper.componentToTag(component);
                changed = true;
            }
        }
        // ChatType.Bound end
        if (changed) {
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            if (VersionHelper.isOrAbove1_21_5()) buf.writeVarInt(globalIndex);
            buf.writeUUID(sender);
            buf.writeVarInt(index);
            buf.writeNullable(messageSignature, (b, bs) -> buf.writeBytes(bs));
            buf.writeUtf(content);
            buf.writeInstant(timeStamp);
            buf.writeLong(salt);
            buf.writeCollection(lastSeen, (b, pair) -> {
                b.writeVarInt(pair.left() + 1);
                if (pair.right() != null) {
                    b.writeBytes(pair.right());
                }
            });
            buf.writeNullable(unsignedContent, (b, tag) -> b.writeNbt(tag, false));
            buf.writeVarInt(type);
            if (type == 2) buf.writeBitSet(mask);
            buf.writeVarInt(chatType);
            buf.writeNbt(name, false);
            buf.writeNullable(targetName, (b, tag) -> b.writeNbt(tag, false));
        }
    }
}
