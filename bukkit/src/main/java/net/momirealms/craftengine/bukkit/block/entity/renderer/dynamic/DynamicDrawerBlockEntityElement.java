package net.momirealms.craftengine.bukkit.block.entity.renderer.dynamic;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.block.entity.DrawerBlockEntityController;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.entity.data.TextDisplayEntityData;
import net.momirealms.craftengine.bukkit.util.ComponentUtils;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundAddEntityPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundSetEntityDataPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DynamicDrawerBlockEntityElement implements BlockEntityElement {
    public final DrawerBlockEntityController controller;
    @NotNull
    private Item lastUpdateItem; // 最后一次发送的更新掉落物品
    private int lastUpdateContent; // 最后一次发送的更新掉落物品
    private boolean positionDirty; // 坐标脏位
    public final int itemId;
    public final int textId;
    public final UUID itemUUID = UUID.randomUUID();
    public final UUID textUUID = UUID.randomUUID();
    public final Object despawnItemPacket;
    public final Object despawnTextPacket;
    public final Object despawnAllPacket;
    @NotNull private Object spawnItemPacket;
    @NotNull private Object spawnTextPacket;
    @NotNull private Object changeItemDataPacket;
    @NotNull private Object changeTextContentDataPacket;
    @NotNull private Object updateItemPosPacket;
    @NotNull private Object updateTextPosPacket;

    public DynamicDrawerBlockEntityElement(@NotNull DrawerBlockEntityController controller,
                                           @NotNull WorldPosition itemPosition, @NotNull WorldPosition textPosition,
                                           float entityYRot
    ) {
        this.controller = controller;
        this.itemId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
        this.textId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
        // 包缓存
        this.despawnItemPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(itemId)));
        this.despawnTextPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(textId)));
        this.despawnAllPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> {
                    a.add(itemId);
                    a.add(textId);
        }));
        this.refreshChangeDisplayItemPacket(controller.storedItem());
        this.refreshChangeTextContentPacket(controller.storedItem().count());
        this.refreshSpawnItemAndTextPacket(itemPosition, textPosition, entityYRot);
    }

    // 更新展示的物品包
    public void refreshChangeDisplayItemPacket(Item item) {
        this.lastUpdateItem = item; // 更新缓存
        this.changeItemDataPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.itemId, new ArrayList<>() {{
            ItemDisplayEntityData.DisplayedItem.addEntityData(item.minecraftItem(), this);
            ItemDisplayEntityData.Scale.addEntityData(controller.behavior.itemScale, this);
            ItemDisplayEntityData.DisplayType.addEntityData((byte) 6, this);
        }});
    }

    // 更新显示的数量包
    public void refreshChangeTextContentPacket(int count) {
        this.lastUpdateContent = count; // 更新缓存
        this.changeTextContentDataPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.textId, new ArrayList<>() {{
            TextDisplayEntityData.Text.addEntityData(ComponentUtils.adventureToMinecraft(Component.text(count)), this);
            TextDisplayEntityData.Scale.addEntityData(controller.behavior.textScale, this);
        }});
    }

    // 更新展示物品的位置
    public void refreshSpawnItemAndTextPacket(WorldPosition itemPosition, WorldPosition textPosition, float entityYRot) {
        this.spawnItemPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                itemId, itemUUID, itemPosition.x, itemPosition.y, itemPosition.z,
                0.0f, entityYRot, EntityTypeProxy.ITEM_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        this.spawnTextPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                textId, textUUID, textPosition.x, textPosition.y, textPosition.z,
                0.0f, entityYRot - 180, EntityTypeProxy.TEXT_DISPLAY, 0, Vec3Proxy.ZERO, 0
        );
        this.updateItemPosPacket = EntityUtils.createUpdatePosPacket(this.itemId,
                itemPosition.x, itemPosition.y, itemPosition.z,
                entityYRot, 0.0f, true
        );
        this.updateTextPosPacket = EntityUtils.createUpdatePosPacket(this.textId,
                textPosition.x, textPosition.y, textPosition.z,
                entityYRot - 180, 0.0f, true
        );
    }

    public void positionDirty(boolean dirtyFlag) {
        this.positionDirty = dirtyFlag;
    }

    @Override
    public void show(@NotNull Player player) {
        if (!this.controller.storedItem().isEmpty()) {
            player.sendPackets(List.of(
                    this.spawnItemPacket,
                    this.spawnTextPacket,
                    this.changeItemDataPacket,
                    this.changeTextContentDataPacket
            ), false);
        }
    }

    @Override
    public void hide(@NotNull Player player) {
        player.sendPacket(this.despawnAllPacket, false);
    }

    @Override
    public void update(@NotNull Player player) {
        // 检查最新的物品和当前刷新的是否一样, 不一样则刷新缓存的包.
        Item displayItem = this.controller.storedItem();
        if (!displayItem.isSimilar(this.lastUpdateItem)) {
            this.refreshChangeDisplayItemPacket(displayItem);
        }
        // 检查最新数量和当前的是否一样, 不一样则刷新缓存的包.
        int storageCount = this.controller.storageCount();
        if (this.lastUpdateContent != storageCount) {
            this.refreshChangeTextContentPacket(storageCount);
        }
        // 如果缓存的显示位置和最新的不一样, 额外发送一个同步位置包.
        if (this.positionDirty) {
            player.sendPackets(List.of(this.updateItemPosPacket, this.updateTextPosPacket), false);
        }
        // 重发刷新包
        if (displayItem.isEmpty()) {
            this.hide(player);
        } else {
            player.sendPackets(List.of(
                    this.spawnItemPacket,
                    this.spawnTextPacket,
                    this.changeItemDataPacket,
                    this.changeTextContentDataPacket
            ), false);
        }
    }

    // 只刷新展示的文本值
    public void updateTextContent(Player player) {
        // 检查最新数量和当前的是否一样, 不一样则刷新缓存的包.
        int storageCount = this.controller.storageCount();
        if (this.lastUpdateContent != storageCount) {
            this.refreshChangeTextContentPacket(storageCount);
        }
        player.sendPacket(this.changeTextContentDataPacket, false);
    }
}
