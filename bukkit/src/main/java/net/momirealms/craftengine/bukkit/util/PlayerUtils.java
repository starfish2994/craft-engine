package net.momirealms.craftengine.bukkit.util;

import com.mojang.datafixers.util.Pair;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.network.BukkitNetworkManager;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class PlayerUtils {
    private PlayerUtils() {
    }

    public static void giveItem(Player player, int amount, Item<ItemStack> original) {
        int amountToGive = amount;
        int maxStack = original.maxStackSize();
        while (amountToGive > 0) {
            int perStackSize = Math.min(maxStack, amountToGive);
            amountToGive -= perStackSize;
            PlayerUtils.giveItem(player, original, original.copyWithCount(perStackSize));
        }
    }

    public static void giveItem(Player player, Item<ItemStack> original, Item<ItemStack> item) {
        Object serverPlayer = FastNMS.INSTANCE.method$CraftPlayer$getHandle(player);
        Object inventory = FastNMS.INSTANCE.method$Player$getInventory(serverPlayer);
        boolean flag = FastNMS.INSTANCE.method$Inventory$add(inventory, item.getLiteralObject());
        if (flag && item.isEmpty()) {
            Object droppedItem = FastNMS.INSTANCE.method$ServerPlayer$drop(serverPlayer, original.copyWithCount(1).getLiteralObject(), false, false, false, null);
            if (droppedItem != null) {
                FastNMS.INSTANCE.method$ItemEntity$makeFakeItem(droppedItem);
            }
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((RandomUtils.generateRandomFloat(0, 1) - RandomUtils.generateRandomFloat(0, 1)) * 0.7F + 1.0F) * 2.0F);
            FastNMS.INSTANCE.method$AbstractContainerMenu$broadcastChanges(FastNMS.INSTANCE.field$Player$containerMenu(serverPlayer));
        } else {
            Object droppedItem = FastNMS.INSTANCE.method$ServerPlayer$drop(serverPlayer, item.getLiteralObject(), false, false, !VersionHelper.isOrAbove1_21_5(), null);
            if (droppedItem != null) {
                FastNMS.INSTANCE.method$ItemEntity$setNoPickUpDelay(droppedItem);
                FastNMS.INSTANCE.method$ItemEntity$setTarget(droppedItem, player.getUniqueId());
            }
        }
    }

    public static void sendTotemAnimation(Player player, ItemStack totem) {
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        List<Object> packets = new ArrayList<>();
        try {
            Object previousItem = FastNMS.INSTANCE.method$CraftItemStack$asNMSCopy(offhandItem);
            Object totemItem = FastNMS.INSTANCE.method$CraftItemStack$asNMSCopy(totem);

            Object packet1 = NetworkReflections.constructor$ClientboundSetEquipmentPacket
                    .newInstance(player.getEntityId(), List.of(Pair.of(CoreReflections.instance$EquipmentSlot$OFFHAND, totemItem)));
            Object packet2 = NetworkReflections.constructor$ClientboundEntityEventPacket
                    .newInstance(FastNMS.INSTANCE.method$CraftPlayer$getHandle(player), (byte) 35);
            Object packet3 = NetworkReflections.constructor$ClientboundSetEquipmentPacket
                    .newInstance(player.getEntityId(), List.of(Pair.of(CoreReflections.instance$EquipmentSlot$OFFHAND, previousItem)));
            packets.add(packet1);
            packets.add(packet2);
            packets.add(packet3);

            Object bundlePacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets);
            BukkitNetworkManager.instance().sendPacket(BukkitAdaptors.adapt(player), bundlePacket);
        } catch (ReflectiveOperationException e) {
            BukkitCraftEngine.instance().logger().warn("Failed to send totem animation");
        }
    }
}
