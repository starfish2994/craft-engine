package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.DrawerBlockBehavior;
import net.momirealms.craftengine.bukkit.block.entity.renderer.dynamic.DynamicDrawerBlockEntityElement;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.world.BukkitContainer;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.WorldlyContainer;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.IntTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class DrawerBlockEntityController extends BlockEntityController implements BukkitContainer, WorldlyContainer {
    private static final String DEFAULT_DATA_KEY = "craftengine:drawer";
    private static final int[] SLOTS = new int[] {0};
    public final DrawerBlockBehavior behavior;
    public final DynamicDrawerBlockEntityElement element;
    private final Object container;
    private final InventoryHolder owner;
    @NotNull
    private Item storedItem;
    private WorldPosition itemPosition;
    private WorldPosition textPosition;
    private float entityYRot;
    private final Vector3f blockCenter;
    private Item lastUpdateItem = Item.empty(); // 最后一次包发送的物品
    private int lastUpdateCount = 0; // 最后一次包发送的物品数量
    private UUID lastClickPlayer;
    private Long lastClickTime;

    public DrawerBlockEntityController(BlockEntity blockEntity, DrawerBlockBehavior behavior) {
        super(blockEntity);
        this.behavior = behavior;
        this.blockCenter = new Vector3f((float) (blockEntity.pos.x + 0.5), (float) (blockEntity.pos.y + 0.5), (float) (blockEntity.pos.z + 0.5));
        this.storedItem = Item.empty();
        this.itemPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.itemPosition);
        this.textPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.textPosition);
        this.entityYRot = this.calculateYRot(blockEntity.blockState);
        this.element = new DynamicDrawerBlockEntityElement(this, this.itemPosition, this.textPosition, this.entityYRot);
        this.container = CraftEngine.instance().platform().createContainer(this);
        this.owner = new Holder(this);
    }

    public Object container() {
        return this.container;
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
        return this.storedItem.count();
    }

    public boolean isFull() {
        return this.storageCount() >= behavior.maxStorageCount;
    }

    // 放入物品
    public void putStorageItem(Item inputItem /* Not Empty */) {
        if (this.storedItem.isEmpty()) {
            this.storedItem = inputItem;
        } else {
            int count = this.storageCount() + inputItem.count();
            this.storedItem.count(count);
        }
        this.setChanged();
    }

    // 增加存储物品的数量
    public void growStorageCount(int putCount) {
        if (!storedItem.isEmpty()) {
            int count = this.storageCount() + putCount;
            this.storedItem.count(count);
            this.setChanged();
        }
    }
    
    public void clearStoredItem() {
        this.storedItem = Item.empty();
        this.setChanged();
    }

    public void setStoredItem(Item item) {
        this.storedItem = item;
        this.setChanged();
    }

    // 取走方块内的物品
    public Item takeStorageItem(int takeCount) {
        if (takeCount <= 0) return Item.empty();
        Item takeItem;
        // 全部拿完了
        if (takeCount >= this.storageCount()) {
            takeItem = this.storedItem.copy();
            this.storedItem = Item.empty();
        }
        // 拿一部分
        else {
            int remainingCount = this.storageCount() - takeCount;
            takeItem = this.storedItem.copyWithCount(takeCount);
            this.storedItem.count(remainingCount);
        }
        this.setChanged();
        return takeItem;
    }

    // 方块状态变更时
    @Override
    public void preBlockStateChange(ImmutableBlockState newState) {
        this.itemPosition = this.calculateDisplayPosition(newState, this.behavior.itemPosition);
        this.textPosition = this.calculateDisplayPosition(newState, this.behavior.textPosition);
        this.entityYRot = this.calculateYRot(newState);
        this.refreshElementPosPacket();
        this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateElementPos);
    }

    // 读取方块内存储的物品
    @Override
    public void loadCustomData(CompoundTag tag) {
        CompoundTag dataTag = tag.getCompound(Optional.ofNullable(behavior.customDataKey).orElse(DEFAULT_DATA_KEY));
        // 空数据
        if (dataTag == null) {
            this.storedItem = Item.empty();
            return;
        }
        // 读取数据
        int dataVersion = dataTag.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
        Tag itemTag = dataTag.get("item");
        int count = dataTag.getInt("count", 0);
        // 非法数据
        if (itemTag == null || count <= 0) {
            this.storedItem = Item.empty();
            return;
        }
        // 记录并刷新
        this.storedItem = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(itemTag, dataVersion));
        this.storedItem.count(count);

        this.element.refreshChangeDisplayItemPacket(this.storedItem);
        this.element.refreshChangeTextContentPacket(count);
        this.lastUpdateItem = this.storedItem;
        this.lastUpdateCount = count;
    }

    @Override
    public void saveCustomData(CompoundTag tag) {
        if (!ItemUtils.isEmpty(this.storedItem) && this.storageCount() > 0) {
            CompoundTag compoundTag = MiscUtils.init(new CompoundTag(), dataTag -> {
                dataTag.put("data_version", new IntTag(Config.itemDataFixerUpperFallbackVersion()));
                dataTag.put("item", ItemStackUtils.saveMinecraftItemStackAsTag(this.storedItem.count(1).minecraftItem()));
                dataTag.put("count", new IntTag(this.storageCount()));
            });
            tag.put(Optional.ofNullable(behavior.customDataKey).orElse(DEFAULT_DATA_KEY), compoundTag);
        }
    }

    @Override
    public void onRemove() {
        if (this.storageCount() < storedItem.maxStackSize()) {
            super.blockEntity.world.world().dropItemNaturally(this.itemPosition, this.storedItem);
        } else {
            int remaining = this.storedItem.count();
            while (remaining > 0) {
                Item splitItem = this.storedItem.copyWithCount(Math.min(this.storedItem.maxStackSize(), remaining));
                remaining -= splitItem.count();
                super.blockEntity.world.world().dropItemNaturally(this.itemPosition, splitItem);
            }
        }
        this.storedItem = Item.empty();
    }

    // 刷新展示元素
    public void refreshDynamicElement(BiConsumer<DynamicDrawerBlockEntityElement, Player> consumer) {
        CEChunk chunk = super.blockEntity.world.getChunkAtIfLoaded(super.blockEntity.pos.x >> 4, super.blockEntity.pos.z >> 4);
        if (chunk != null) {
            for (Player trackedPlayer : chunk.getTrackedBy()) {
                consumer.accept(this.element, trackedPlayer);
            }
        }
    }

    // 检查并刷新元素物品展示实体的内容包
    public boolean refreshItemDisplayPacket() {
        Item displayItem = this.storedItem();
        boolean result = false;
        if (displayItem.minecraftItem() == this.lastUpdateItem.minecraftItem()) {
            return false;
        }
        if (!displayItem.isSimilar(this.lastUpdateItem)) {
            this.element.refreshChangeDisplayItemPacket(displayItem);
            result = true;
        }
        this.lastUpdateItem = this.storedItem;
        return result;
    }

    // 检查并刷新元素展示的文本实体的内容包
    public boolean refreshTextContentPacket() {
        int storageCount = this.storageCount();
        boolean result = false;
        if (this.lastUpdateCount != storageCount) {
            this.element.refreshChangeTextContentPacket(storageCount);
            result = true;
        }
        this.lastUpdateCount = storageCount;
        return result;
    }

    // 检查并刷新元素展示位置的内容包
    public void refreshElementPosPacket() {
        this.element.refreshSpawnItemAndTextPacket(this.itemPosition, this.textPosition, this.entityYRot);
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
        return switch (direction.opposite()) {
            case EAST -> 90f;
            case SOUTH -> 180f;
            case WEST -> 270f;
            default ->  0f;
        };
    }

    public UUID lastClickPlayer() {
        return lastClickPlayer;
    }

    public void lastClickPlayer(UUID lastClickPlayer) {
        this.lastClickPlayer = lastClickPlayer;
    }

    public Long lastClickTime() {
        return lastClickTime;
    }

    public void lastClickTime(Long lastClickTime) {
        this.lastClickTime = lastClickTime;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, Item stack, Direction direction) {
        return this.storageCount() < this.behavior.maxStorageCount;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, Item stack, Direction direction) {
        return this.storageCount() > 0;
    }

    @Override
    public int containerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.storedItem.isEmpty();
    }

    @Override
    public Item getItem(int slot) {
        return this.storedItem;
    }

    @Override
    public Item removeItem(int slot, int count) {
        int currentCount = this.storageCount();
        int removeCount = Math.min(currentCount, count);
        int remainingCount = currentCount - removeCount;
        // 全部移除
        if (remainingCount == 0) {
            Item removedItems = this.storedItem;
            this.setChanged();
            return removedItems;
        }
        // 移除一部分
        else {
            Item removedItems = this.storedItem.copyWithCount(removeCount);
            this.storedItem.count(remainingCount);
            this.setChanged();
            return removedItems;
        }
    }

    @Override
    public Item removeItemNoUpdate(int slot) {
        Item removedItems = this.storedItem;
        this.setChanged();
        return removedItems;
    }

    @Override
    public void setItem(int slot, Item item) {
        if (slot == 0) {
            this.storedItem = item;
        }
    }

    @Override
    public int maxStackSize() {
        return this.behavior.maxStorageCount;
    }

    @Override
    public void setChanged() {
        boolean isEmpty = this.storedItem.isEmpty();
        boolean changedItem = this.refreshItemDisplayPacket();
        boolean changedContent = this.refreshTextContentPacket();

        // 空了
        if (isEmpty) {
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::hide);
        }
        // 都变了
        else if (changedItem && changedContent) {
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateItemAndText);
        }
        // 只变了物品
        else if (changedItem) {
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateDisplayItem);
        }
        // 只变了数量
        else if (changedContent) {
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateTextContent);
        }

        CEWorld ceWorld = blockEntity.world;
        if (ceWorld == null) return;
        ceWorld.blockEntityChanged(blockEntity.pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.storedItem = Item.empty();
        this.setChanged();
    }

    @Override
    public List<Item> contents() {
        return List.of(this.storedItem);
    }

    @Override
    public void setMaxStackSize(int size) {
    }

    @Override
    public WorldPosition position() {
        return new WorldPosition(
                super.blockEntity.world.world,
                super.blockEntity.pos.x,
                super.blockEntity.pos.y,
                super.blockEntity.pos.z
        );
    }

    @Override
    public void onOpen(HumanEntity player) {
    }

    @Override
    public void onClose(HumanEntity player) {
    }

    @Override
    public List<HumanEntity> getViewers() {
        return List.of();
    }

    @Override
    public @Nullable InventoryHolder getOwner() {
        return this.owner;
    }

    public static class Holder implements InventoryHolder {
        private final DrawerBlockEntityController container;

        public Holder(DrawerBlockEntityController container) {
            this.container = container;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return CraftInventoryProxy.INSTANCE.newInstance(container);
        }

    }
}
