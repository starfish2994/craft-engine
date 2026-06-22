package net.momirealms.craftengine.bukkit.block.display;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.craftengine.bukkit.entity.data.DisplayData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.setting.DestroyStageDisplay;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundAddEntityPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundSetEntityDataPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypesProxy;
import net.momirealms.craftengine.proxy.minecraft.world.item.ItemStackProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;

import java.util.*;
import java.util.function.Consumer;

public final class DestroyStageDisplayEntity {
    private final DestroyStageDisplay config;
    private final int entityId;
    private final Object spawnPacket;
    private final Object removePacket;
    private final Set<UUID> spawnedViewers = new HashSet<>();

    public DestroyStageDisplayEntity(DestroyStageDisplay config, int entityId, BlockPos pos) {
        this.config = config;
        this.entityId = entityId;
        double x = pos.x() + config.position().x;
        double y = pos.y() + config.position().y;
        double z = pos.z() + config.position().z;
        this.spawnPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                entityId, UUID.randomUUID(),
                x, y, z, config.pitch(), config.yaw(),
                EntityTypesProxy.ITEM_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        this.removePacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(
                MiscUtils.init(new IntArrayList(), a -> a.add(entityId))
        );
    }

    public Object spawnPacket() {
        return this.spawnPacket;
    }

    public Object removePacket() {
        return this.removePacket;
    }

    public Object metadataPacket(Player player, int index) {
        return ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(entityId, buildMetadata(player, index));
    }

    public boolean ensureSpawned(Player viewer, int index, Consumer<Object> packetSender) {
        boolean firstSpawn = this.spawnedViewers.add(viewer.uuid());
        if (firstSpawn) {
            packetSender.accept(this.spawnPacket);
        }
        packetSender.accept(metadataPacket(viewer, index));
        return firstSpawn;
    }

    public void removeLeftViewers(Collection<UUID> currentViewers, Consumer<UUID> onRemove) {
        this.spawnedViewers.removeIf(uuid -> {
            if (!currentViewers.contains(uuid)) {
                onRemove.accept(uuid);
                return true;
            }
            return false;
        });
    }

    public Set<UUID> spawnedViewers() {
        return this.spawnedViewers;
    }

    public boolean isEmpty() {
        return this.spawnedViewers.isEmpty();
    }

    private List<Object> buildMetadata(Player player, int index) {
        List<Object> data = new ArrayList<>();
        DisplayData.ItemDisplayData.ItemStack.addEntityData(resolveItem(index, player), data);
        DisplayData.ItemDisplayData.Scale.addEntityDataIfNotDefaultValue(this.config.scale(), data);
        DisplayData.ItemDisplayData.LeftRotation.addEntityDataIfNotDefaultValue(this.config.rotation(), data);
        DisplayData.ItemDisplayData.Translation.addEntityDataIfNotDefaultValue(this.config.translation(), data);
        DisplayData.ItemDisplayData.ItemTransform.addEntityDataIfNotDefaultValue(this.config.displayContext().id(), data);
        DisplayData.ItemDisplayData.BillboardConstraints.addEntityDataIfNotDefaultValue(this.config.billboard().id(), data);
        int blockLight = this.config.blockLight();
        int skyLight = this.config.skyLight();
        if (blockLight != -1 && skyLight != -1) {
            DisplayData.ItemDisplayData.BrightnessOverride.addEntityData(blockLight << 4 | skyLight << 20, data);
        }
        DisplayData.ItemDisplayData.ViewRange.addEntityDataIfNotDefaultValue((float) (this.config.viewRange() * player.displayEntityViewDistance()), data);
        return data;
    }

    private Object resolveItem(int index, Player player) {
        Key itemKey = this.config.itemForIndex(index);
        if (itemKey == null) return ItemStackProxy.EMPTY;
        Item wrapped = BukkitItemManager.instance().createWrappedItem(itemKey, player);
        if (wrapped == null) {
            wrapped = BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, player);
        }
        return wrapped == null ? ItemStackProxy.EMPTY : wrapped.minecraftItem();
    }
}
