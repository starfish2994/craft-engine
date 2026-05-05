package net.momirealms.craftengine.bukkit.plugin.network.listener;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.PacketUtils;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.recipe.network.legacy.LegacyRecipeHolder;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.event.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.listener.ByteBufferPacketListener;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UpdateRecipesListener1_20 implements ByteBufferPacketListener {
    public static final UpdateRecipesListener1_20 INSTANCE = new UpdateRecipesListener1_20();

    private UpdateRecipesListener1_20() {}

    @Override
    public void onPacketSend(NetWorkUser user, ByteBufPacketEvent event) {
        if (Config.disableItemOperations()) return;
        MutableBoolean changed = new MutableBoolean(false);
        FriendlyByteBuf buf = event.getBuffer();
        BukkitItemManager itemManager = BukkitItemManager.instance();
        BukkitServerPlayer player = (BukkitServerPlayer) user;
        List<LegacyRecipeHolder> holders = buf.readCollection(ArrayList::new, byteBuf -> {
            LegacyRecipeHolder holder = LegacyRecipeHolder.read(byteBuf, $ -> PacketUtils.readItem(buf));
            holder.recipe().applyClientboundData(item -> {
                Optional<Item> remapped = itemManager.s2c(item, player);
                if (remapped.isEmpty()) {
                    return item;
                }
                changed.set(true);
                return remapped.get();
            });
            return holder;
        });
        if (changed.booleanValue()) {
            event.setChanged(true);
            buf.clear();
            buf.writeVarInt(event.packetID());
            buf.writeCollection(holders, ((byteBuf, recipeHolder)
                    -> recipeHolder.write(byteBuf,
                    ($, item) -> PacketUtils.writeItem(buf, item))));
        }
    }
}
