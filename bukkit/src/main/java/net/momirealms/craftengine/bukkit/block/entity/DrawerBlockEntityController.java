package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.DrawerBlockBehavior;
import net.momirealms.craftengine.bukkit.block.entity.renderer.dynamic.DynamicDrawerBlockEntityElement;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.ConcurrentUUID2ReferenceChainedHashTable;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.IntTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class DrawerBlockEntityController extends BlockEntityController {
    public final DrawerBlockBehavior behavior;
    public final DynamicDrawerBlockEntityElement element;
    @NotNull
    private Item storedItem;
    private int count;
    private WorldPosition itemPosition;
    private WorldPosition textPosition;
    private float entityYRot;
    private final Vector3f blockCenter;
    public final ConcurrentUUID2ReferenceChainedHashTable<Long> lastClickMap = new ConcurrentUUID2ReferenceChainedHashTable<>();

    public DrawerBlockEntityController(BlockEntity blockEntity, DrawerBlockBehavior behavior) {
        super(blockEntity);
        this.behavior = behavior;
        this.blockCenter = new Vector3f((float) (blockEntity.pos.x + 0.5), (float) (blockEntity.pos.y + 0.5), (float) (blockEntity.pos.z + 0.5));
        this.storedItem = BukkitItemManager.instance().emptyItem();
        this.itemPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.itemPosition);
        this.textPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.textPosition);
        this.entityYRot = this.calculateYRot(blockEntity.blockState);
        this.element = new DynamicDrawerBlockEntityElement(this, this.itemPosition, this.textPosition, this.entityYRot);
    }

    @Override
    public boolean hasElement() {
        return true;
    }

    @Override
    public void gatherElements(Consumer<BlockEntityElement> consumer) {
        consumer.accept(this.element);
    }

    @NotNull
    public Item storedItem() {
        return this.storedItem;
    }

    public int storageCount() {
        return this.count;
    }

    public boolean isFull() {
        return this.count >= behavior.maxStorageCount;
    }

    // 放入物品
    public void putStorageItem(Item inputItem /* Not Empty */) {
        if (this.storedItem.isEmpty()) {
            this.storedItem = inputItem;
            this.count = inputItem.count();
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::update);
        } else {
            this.count += inputItem.count();
            this.storedItem.count(this.count);
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateTextContent);
        }
    }

    // 增加存储物品的数量
    public void growStorageCount(int putCount) {
        if (!storedItem.isEmpty()) {
            this.count += putCount;
            this.storedItem.count(this.count);
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateTextContent);
        }
    }

    // 取走方块内的物品
    public Item takeStorageItem(int count) {
        if (count <= 0) return BukkitItemManager.instance().emptyItem();
        Item takeItem;
        // 全部拿完了
        if (count >= this.count) {
            takeItem = this.storedItem.copy();
            this.storedItem = BukkitItemManager.instance().emptyItem();
            this.count = 0;
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::hide);
        }
        // 拿一部分
        else {
            takeItem = this.storedItem.copyWithCount(count);
            this.count -= count;
            this.storedItem.count(this.count);
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateTextContent);
        }
        return takeItem;
    }

    // 刷新展示元素
    public void refreshDynamicElement(BiConsumer<DynamicDrawerBlockEntityElement, Player> consumer) {
        this.element.refreshChangeDisplayItemPacket(this.storedItem);
        CEChunk chunk = super.blockEntity.world.getChunkAtIfLoaded(super.blockEntity.pos.x >> 4, super.blockEntity.pos.z >> 4);
        if (chunk != null) {
            for (Player trackedPlayer : chunk.getTrackedBy()) {
                consumer.accept(this.element, trackedPlayer);
            }
        }
    }

    // 方块状态变更时
    @Override
    public void preBlockStateChange(ImmutableBlockState newState) {
        this.itemPosition = this.calculateDisplayPosition(newState, this.behavior.itemPosition);
        this.textPosition = this.calculateDisplayPosition(newState, this.behavior.textPosition);
        this.entityYRot = this.calculateYRot(newState);
        this.element.positionDirty(true);
        this.element.refreshSpawnItemAndTextPacket(this.itemPosition, this.textPosition, this.entityYRot);
        CEChunk chunk = super.blockEntity.world.getChunkAtIfLoaded(super.blockEntity.pos.x >> 4, super.blockEntity.pos.z >> 4);
        if (chunk != null) {
            for (Player trackedPlayer : chunk.getTrackedBy()) {
                this.element.update(trackedPlayer);
            }
        }
        this.element.positionDirty(false);
    }

    // 读取方块内存储的物品
    @Override
    public void loadCustomData(CompoundTag tag) {
        Tag itemTag = tag.get("drawer_storage_item");
        Tag amountTag = tag.get("drawer_storage_amount");
        this.count = amountTag instanceof IntTag intTag ? intTag.getAsInt() : 0;
        if (itemTag == null || this.count <= 0) {
            this.storedItem = BukkitItemManager.instance().emptyItem();
            this.count = 0;
            return;
        }
        // 如果里面有物品, 同时刷新Render的包缓存.
        int dataVersion = tag.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
        this.storedItem = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(itemTag, dataVersion));
        this.storedItem.count(this.count);
        this.element.refreshChangeDisplayItemPacket(this.storedItem);
        this.element.refreshChangeTextContentPacket(this.count);
    }

    @Override
    public void saveCustomData(CompoundTag tag) {
        if (!ItemUtils.isEmpty(this.storedItem) && this.count > 0) {
            tag.put("drawer_storage_item", ItemStackUtils.saveMinecraftItemStackAsTag(this.storedItem.getMinecraftItem()));
            tag.put("drawer_storage_amount", new IntTag(this.count));
        }
    }

    @Override
    public void onRemove() {
        if (this.count < storedItem.maxStackSize()) {
            super.blockEntity.world.world().dropItemNaturally(this.itemPosition, this.storedItem);
        } else {
            int remaining = this.storedItem.count();
            while (remaining > 0) {
                Item splitItem = this.storedItem.copyWithCount(Math.min(this.storedItem.maxStackSize(), remaining));
                remaining -= splitItem.count();
                super.blockEntity.world.world().dropItemNaturally(this.itemPosition, splitItem);
            }
        }
        this.storedItem = BukkitItemManager.instance().emptyItem();
    }

    public WorldPosition calculateDisplayPosition(ImmutableBlockState blockState, Vector3f relative) {
        float angleDeg;
        Direction direction = blockState.get(behavior.directionProperty, Direction.SOUTH);
        switch (direction) {
            case EAST -> angleDeg = 90f;
            case SOUTH -> angleDeg = 180f;
            case WEST -> angleDeg = 270f;
            default -> angleDeg = 0f;
        }
        double angleRad = Math.toRadians(angleDeg);

        float x = -relative.x;
        float z = relative.z;
        double rotatedX = x * Math.cos(angleRad) - z * Math.sin(angleRad);
        double rotatedZ = x * Math.sin(angleRad) + z * Math.cos(angleRad);

        return new WorldPosition(null,
                this.blockCenter.x + rotatedX,
                this.blockCenter.y + relative.y,
                this.blockCenter.z + rotatedZ
        );
    }

    public float calculateYRot(ImmutableBlockState blockState) {
        Direction direction = blockState.get(behavior.directionProperty, Direction.SOUTH);
        return switch (direction) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default ->  0f;
        };
    }
}
