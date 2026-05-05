package net.momirealms.craftengine.bukkit.plugin.network.listener;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.RegistryOps;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.context.NetworkTextReplaceContext;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.plugin.text.component.ComponentProvider;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.GsonHelper;
import net.momirealms.craftengine.core.util.Pair;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Map;
import java.util.UUID;

public class PlayerChatListener_1_20 implements ByteBufferPacketListener {
    public static final PlayerChatListener_1_20 INSTANCE = new PlayerChatListener_1_20();

    private PlayerChatListener_1_20() {}

    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (!Config.interceptPlayerChat() || Config.disableChatReport()) return;
        FriendlyByteBuf buf = event.getBuffer();
        boolean changed = false;
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
        @Nullable String unsignedContent = buf.readNullable(FriendlyByteBuf::readUtf);
        BukkitNetworkManager networkManager = BukkitNetworkManager.instance();
        if (unsignedContent != null) {
            Map<String, ComponentProvider> unsignedContentTokens = networkManager.matchNetworkTags(unsignedContent);
            if (!unsignedContentTokens.isEmpty()) {
                Tag tag = RegistryOps.JSON.convertTo(RegistryOps.SPARROW_NBT, GsonHelper.get().fromJson(unsignedContent, JsonElement.class));
                Component component = AdventureHelper.nbtToComponent(tag);
                component = AdventureHelper.replaceText(component, unsignedContentTokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
                unsignedContent = RegistryOps.SPARROW_NBT.convertTo(RegistryOps.JSON, AdventureHelper.componentToNbt(component)).toString();
                changed = true;
            }
        }
        // FilterMask start
        int type = buf.readVarInt();
        BitSet mask = type == 2 /* PARTIALLY_FILTERED */ ? buf.readBitSet() : null;
        // FilterMask end
        // ChatType.BoundNetwork start
        int chatType = buf.readVarInt();
        String name = buf.readUtf();
        Map<String, ComponentProvider> nameTokens = networkManager.matchNetworkTags(name);
        if (!nameTokens.isEmpty()) {
            Tag tag = RegistryOps.JSON.convertTo(RegistryOps.SPARROW_NBT, GsonHelper.get().fromJson(name, JsonElement.class));
            Component component = AdventureHelper.nbtToComponent(tag);
            component = AdventureHelper.replaceText(component, nameTokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
            name = RegistryOps.SPARROW_NBT.convertTo(RegistryOps.JSON, AdventureHelper.componentToNbt(component)).toString();
            changed = true;
        }
        @Nullable String targetName = buf.readNullable(FriendlyByteBuf::readUtf);
        if (targetName != null) {
            Map<String, ComponentProvider> targetNameTokens = networkManager.matchNetworkTags(targetName);
            if (!targetNameTokens.isEmpty()) {
                Tag tag = RegistryOps.JSON.convertTo(RegistryOps.SPARROW_NBT, GsonHelper.get().fromJson(targetName, JsonElement.class));
                Component component = AdventureHelper.nbtToComponent(tag);
                component = AdventureHelper.replaceText(component, targetNameTokens, NetworkTextReplaceContext.of((BukkitServerPlayer) user));
                targetName = RegistryOps.SPARROW_NBT.convertTo(RegistryOps.JSON, AdventureHelper.componentToNbt(component)).toString();
                changed = true;
            }
        }
        // ChatType.BoundNetwork end
        if (changed) {
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
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
            buf.writeNullable(unsignedContent, FriendlyByteBuf::writeUtf);
            buf.writeVarInt(type);
            if (type == 2) buf.writeBitSet(mask);
            buf.writeVarInt(chatType);
            buf.writeUtf(name);
            buf.writeNullable(targetName, FriendlyByteBuf::writeUtf);
        }
    }
}
