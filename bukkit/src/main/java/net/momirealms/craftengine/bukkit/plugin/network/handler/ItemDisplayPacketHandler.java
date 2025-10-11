package net.momirealms.craftengine.bukkit.plugin.network.handler;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.network.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemDisplayPacketHandler implements EntityPacketHandler {
    public static final ItemDisplayPacketHandler INSTANCE = new ItemDisplayPacketHandler();

    @Override
    public void handleSetEntityData(Player user, ByteBufPacketEvent event) {
        if (Config.disableItemOperations()) return;
        FriendlyByteBuf buf = event.getBuffer();
        int id = buf.readVarInt();
        List<Object> packedItems = FastNMS.INSTANCE.method$ClientboundSetEntityDataPacket$unpack(buf);
        for (int i = 0; i < packedItems.size(); i++) {
            Object packedItem = packedItems.get(i);
            int entityDataId = FastNMS.INSTANCE.field$SynchedEntityData$DataValue$id(packedItem);
            if (entityDataId != ItemDisplayEntityData.DisplayedItem.id()) continue;
            Object nmsItemStack = FastNMS.INSTANCE.field$SynchedEntityData$DataValue$value(packedItem);
            ItemStack itemStack = BukkitItemManager.instance().s2c(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(nmsItemStack), user);
            Object serializer = FastNMS.INSTANCE.field$SynchedEntityData$DataValue$serializer(packedItem);
            packedItems.set(i, FastNMS.INSTANCE.constructor$SynchedEntityData$DataValue(
                    entityDataId, serializer, FastNMS.INSTANCE.method$CraftItemStack$asNMSCopy(itemStack)
            ));
            break;
        }

        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeVarInt(id);
        FastNMS.INSTANCE.method$ClientboundSetEntityDataPacket$pack(packedItems, buf);
    }
}
