package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.PacketUtils;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.modern.RecipeBookEntry;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddRecipeBookListener implements ByteBufferPacketListener {
    public static final AddRecipeBookListener INSTANCE = new AddRecipeBookListener();

    private AddRecipeBookListener() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (Config.disableItemOperations()) return;
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        if (!player.isOnline()) return;
        MutableBoolean changed = new MutableBoolean(false);
        FriendlyByteBuf buf = event.getBuffer();
        BukkitItemManager itemManager = BukkitItemManager.instance();
        List<RecipeBookEntry> entries = buf.readCollection(ArrayList::new, byteBuf -> {
            RecipeBookEntry entry = RecipeBookEntry.read(byteBuf, $ -> PacketUtils.readItem(buf));
            entry.applyClientboundData(item -> {
                Optional<Item> remapped = itemManager.s2c(item, player);
                if (remapped.isEmpty()) {
                    return item;
                }
                changed.set(true);
                return remapped.get();
            });
            return entry;
        });
        boolean replace = buf.readBoolean();
        if (changed.booleanValue()) {
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeCollection(entries, ((byteBuf, recipeBookEntry) -> recipeBookEntry.write(byteBuf,
                    ($, item) -> PacketUtils.writeItem(buf, item))));
            buf.writeBoolean(replace);
        }
    }
}
