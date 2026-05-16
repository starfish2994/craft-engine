package net.momirealms.craftengine.core.plugin.network.mod.protocol;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemDefinition;
import net.momirealms.craftengine.core.item.ItemManager;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.plugin.network.PacketFlow;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.mod.ModPacket;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record CreativeModeTabItemsPacket(Action action, List<Item> itemStacks) implements ModPacket {
    public static final ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> TYPE = ResourceKey.create(
            BuiltInRegistries.MOD_PACKET.key().location(), Key.of("craftengine", "creative_mode_tab_items")
    );
    public static final NetworkCodec<FriendlyByteBuf, CreativeModeTabItemsPacket> CODEC = ModPacket.codec(
            CreativeModeTabItemsPacket::encode,
            CreativeModeTabItemsPacket::decode
    );
    public static final String PERMISSION = "ce.mod.clientbound.creative_mode_tab_items";

    private static CreativeModeTabItemsPacket decode(FriendlyByteBuf buf) {
        Action action = buf.readEnumConstant(Action.class);
        if (action == Action.CLEAR) {
            return new CreativeModeTabItemsPacket(Action.CLEAR, List.of());
        } else {
            List<Item> list = buf.readCollection(ArrayList::new, CraftEngine.instance().platform()::readItem);
            return new CreativeModeTabItemsPacket(action, list);
        }
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeEnumConstant(this.action);
        if (this.action == Action.CLEAR) return;
        buf.writeCollection(this.itemStacks, CraftEngine.instance().platform()::writeItem);
    }

    @Override
    public String permission(PacketFlow flow) {
        return PERMISSION;
    }

    @Override
    public ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> type() {
        return TYPE;
    }

    public enum Action {
        ADD,
        CLEAR,
        SET
    }

    public static List<CreativeModeTabItemsPacket> create(@NotNull Player player) {
        if (Config.modChannelRequiresPermission() && !CraftEngine.instance().compatibilityManager().hasPermission(player, PERMISSION)) {
            Debugger.COMMON.debug(() -> "Player " + player.name() + " does not have " + PERMISSION + " permission to send " + TYPE.location());
            return List.of();
        }
        List<Item> itemStacks = new ArrayList<>();
        ItemManager itemManager = CraftEngine.instance().itemManager();
        Collection<ItemDefinition> definitions = itemManager.loadedItems().values();
        for (ItemDefinition definition : definitions) {
            if (definition.isVanillaItem()) continue;
            Item item = definition.buildItem(player);
            itemStacks.add(itemManager.s2c(item, player).orElse(item));
        }
        List<CreativeModeTabItemsPacket> packets = new ArrayList<>();
        boolean first = true;
        int singletonSize = Config.modChannelCreativeTabMaxItemsPerPacket();
        for (int i = 0; i < itemStacks.size(); i += singletonSize) {
            List<Item> chunk = itemStacks.subList(i, Math.min(i + singletonSize, itemStacks.size()));
            packets.add(new CreativeModeTabItemsPacket(first ? Action.SET : Action.ADD, new ArrayList<>(chunk)));
            first = false;
        }
        if (packets.isEmpty()) {
            packets.add(new CreativeModeTabItemsPacket(Action.CLEAR, List.of()));
        }
        return packets;
    }
}
