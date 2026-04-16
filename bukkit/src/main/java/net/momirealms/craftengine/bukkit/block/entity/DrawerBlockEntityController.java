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
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.WorldlyContainer;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.proxy.bukkit.craftbukkit.inventory.CraftInventoryProxy;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.IntTag;
import net.momirealms.sparrow.nbt.LongTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class DrawerBlockEntityController extends BlockEntityController implements BukkitContainer, WorldlyContainer, InventoryHolder {
    private static final int HOPPER_PLACE_SLOT = -1;
    private static final int HOPPER_TAKE_SLOT = -2;
    private static final int[] SLOTS = new int[]{HOPPER_PLACE_SLOT, HOPPER_TAKE_SLOT};
    public final DrawerBlockBehavior behavior;
    public final DynamicDrawerBlockEntityElement element;
    private final Object container;
    private final Inventory inventory;
    private WorldPosition itemPosition;
    private WorldPosition textPosition;
    private float entityYRot;
    private final Vector3f blockCenter;
    private Item lastUpdateItem = Item.empty(); // 最后一次包发送的物品
    private long lastUpdateCount = 0; // 最后一次包发送的物品数量
    private UUID lastClickPlayer;
    private Long lastClickTime;
    private Item templateItem = Item.empty();
    private long itemCount = 0;

    public DrawerBlockEntityController(BlockEntity blockEntity, DrawerBlockBehavior behavior) {
        super(blockEntity);
        this.behavior = behavior;
        this.blockCenter = new Vector3f((float) (blockEntity.pos.x + 0.5), (float) (blockEntity.pos.y + 0.5), (float) (blockEntity.pos.z + 0.5));
        this.itemPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.itemPosition);
        this.textPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.textPosition);
        this.entityYRot = this.calculateYRot(blockEntity.blockState);
        this.element = new DynamicDrawerBlockEntityElement(this, this.itemPosition, this.textPosition, this.entityYRot);
        this.container = CraftEngine.instance().platform().createContainer(this);
        this.inventory = CraftInventoryProxy.INSTANCE.newInstance(this.container);
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
        return this.templateItem;
    }

    public long itemCount() {
        return this.itemCount;
    }

    public void addItemCount(long count) {
        this.itemCount += count;
    }

    public void setItemCount(long count) {
        this.itemCount = count;
    }

    public boolean isFull() {
        return this.itemCount() >= this.behavior.maxCount;
    }

    /**
     * 放入物品，返回值为实际放入的量
     *
     * @param inputItem 不为空的物品
     * @param count 尝试放入的数量
     */
    public long put(Item inputItem, long count) {
        if (count <= 0 || inputItem.isEmpty()) return 0;

        if (isEmpty()) {
            this.templateItem = inputItem.copyWithCount(1);
        } else if (!this.templateItem.isSimilar(inputItem)) {
            return 0;
        }

        long actualAdded = Math.min(count, this.behavior.maxCount - this.itemCount());
        this.addItemCount(actualAdded);

        this.setChanged();
        return actualAdded;
    }

    // 增加存储物品的数量，返回值为实际增加的量
    public long add(long count) {
        if (count <= 0 || isEmpty()) return 0;
        long actualAdded = Math.min(count, this.behavior.maxCount - this.itemCount());
        this.addItemCount(actualAdded);
        this.setChanged();
        return actualAdded;
    }

    // 取走方块内的物品，返回值为实际取走的量
    @SuppressWarnings("UnusedReturnValue")
    public long take(long count, Consumer<Item> consumer, boolean update) {
        if (count <= 0 || isEmpty()) return 0;

        // 记录物品模板，用于后续生成 Item
        Item template = this.templateItem.copyWithCount(1);
        long actualTaken = Math.min(count, this.itemCount());

        if (actualTaken <= 0) return 0;

        this.addItemCount(-actualTaken);

        if (this.itemCount() <= 0) {
            this.templateItem = Item.empty();
            this.setItemCount(0);
        }

        int maxStack = template.maxStackSize();
        long toCallback = actualTaken;
        while (toCallback > 0) {
            int currentBatch = (int) Math.min(toCallback, maxStack);
            toCallback -= currentBatch;
            consumer.accept(template.copyWithCount(currentBatch));
        }
        if (update) {
            this.setChanged();
        }

        return actualTaken;
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
        CompoundTag dataTag = tag.getCompound(behavior.customDataKey);
        // 空数据
        if (dataTag == null) return;
        // 读取数据
        int dataVersion = dataTag.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
        Tag itemTag = dataTag.get("item");
        long count = dataTag.getLong("count", 0);
        // 非法数据
        if (itemTag == null || count <= 0) return;

        this.templateItem = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(itemTag, dataVersion)).copyWithCount(1);
        this.setItemCount(count);

        this.element.refreshChangeDisplayItemPacket(this.templateItem);
        this.element.refreshChangeTextContentPacket(count);
        this.lastUpdateItem = this.templateItem.copy();
        this.lastUpdateCount = count;
    }

    @Override
    public void saveCustomData(CompoundTag tag) {
        if (isEmpty() || this.itemCount() <= 0) return;
        CompoundTag data = new CompoundTag();
        data.put("data_version", new IntTag(Config.itemDataFixerUpperFallbackVersion()));
        data.put("count", new LongTag(this.itemCount()));
        data.put("item", ItemStackUtils.saveMinecraftItemStackAsTag(this.templateItem.copyWithCount(1).minecraftItem()));
        tag.put(behavior.customDataKey, data);
    }

    @Override
    public void onRemove() {
        if (this.itemCount() <= 0 || this.templateItem.isEmpty()) return;
        Item template = this.templateItem.copyWithCount(1);
        long count = this.itemCount();
        this.templateItem = Item.empty();
        this.setItemCount(0);
        int maxStackSize = template.maxStackSize();
        while (count > 0) {
            int toDrop = (int) Math.min(count, maxStackSize);
            count -= toDrop;
            super.blockEntity.world.world().dropItemNaturally(this.itemPosition, template.copyWithCount(toDrop));
        }
    }

    // 刷新展示元素
    public void refreshDynamicElement(BiConsumer<DynamicDrawerBlockEntityElement, Player> consumer) {
        CEChunk chunk = super.blockEntity.world.getChunkAtIfLoaded(super.blockEntity.pos.x >> 4, super.blockEntity.pos.z >> 4);
        if (chunk == null) return;
        for (Player trackedPlayer : chunk.getTrackedBy()) {
            consumer.accept(this.element, trackedPlayer);
        }
    }

    // 检查并刷新元素物品展示实体的内容包
    public boolean refreshItemDisplayPacket() {
        Item displayItem = this.templateItem;
        boolean result = false;
        if (displayItem.minecraftItem() == this.lastUpdateItem.minecraftItem()) {
            return false;
        }
        if (!displayItem.isSimilar(this.lastUpdateItem)) {
            this.lastUpdateItem = displayItem.copy();
            this.element.refreshChangeDisplayItemPacket(displayItem);
            result = true;
        }
        return result;
    }

    // 检查并刷新元素展示的文本实体的内容包
    public boolean refreshTextContentPacket() {
        long storageCount = this.itemCount();
        boolean result = false;
        if (this.lastUpdateCount != storageCount) {
            this.lastUpdateCount = storageCount;
            this.element.refreshChangeTextContentPacket(storageCount);
            result = true;
        }
        return result;
    }

    // 检查并刷新元素展示位置的内容包
    public void refreshElementPosPacket() {
        this.element.refreshSpawnItemAndTextPacket(this.itemPosition, this.textPosition, this.entityYRot);
    }

    public WorldPosition calculateDisplayPosition(ImmutableBlockState blockState, Vector3f relative) {
        float angleDeg;
        if (this.behavior.directionProperty != null) {
            Direction direction = blockState.get(this.behavior.directionProperty, Direction.SOUTH);
            switch (direction) {
                case EAST -> angleDeg = 90f;
                case SOUTH -> angleDeg = 180f;
                case WEST -> angleDeg = 270f;
                default -> angleDeg = 0f;
            }
        } else {
            angleDeg = 0f;
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
        if (this.behavior.directionProperty == null) return 0f;
        Direction direction = blockState.get(this.behavior.directionProperty, Direction.SOUTH);
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
        return behavior.canPlaceItem
                && slot == HOPPER_PLACE_SLOT
                && (this.isEmpty() || this.templateItem.isSimilar(stack))
                && this.itemCount() + stack.count() <= behavior.maxCount;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, Item stack, Direction direction) {
        return behavior.canTakeItem && slot == HOPPER_TAKE_SLOT && !this.isEmpty();
    }

    @Override
    public int containerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.templateItem.isEmpty() && this.itemCount() <= 0;
    }

    @Override
    public Item getItem(int slot) {
        if (slot != HOPPER_TAKE_SLOT || this.isEmpty()) return Item.empty();
        return this.templateItem.copyWithCount(1);
    }

    @Override
    public Item removeItem(int slot, int count) {
        if (slot < 0) return Item.empty();
        Item item = this.templateItem.copyWithCount(1);
        take(count, $ -> {}, true);
        return item;
    }

    @Override
    public Item removeItemNoUpdate(int slot) {
        if (slot < 0) return Item.empty();
        Item item = this.templateItem.copyWithCount(1);
        take(this.itemCount(), $ -> {}, false);
        return item;
    }

    @Override
    public void setItem(int slot, Item item) {
        int count = item.count();
        if (slot == HOPPER_PLACE_SLOT && this.templateItem.isSimilar(item) && count == 1) {
            add(1); // 漏斗放入
        } else if (slot == HOPPER_TAKE_SLOT && item.isEmpty()) {
            take(1, $ -> {}, true); // 漏斗取出
        } else {
            this.templateItem = item.copyWithCount(1);
            this.setItemCount(count);
        }
    }

    @Override
    public int maxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        boolean previousEmpty = this.lastUpdateItem.isEmpty();
        boolean isEmpty = this.isEmpty();
        boolean changedItem = this.refreshItemDisplayPacket();
        boolean changedCount = this.refreshTextContentPacket();

        if (isEmpty) {
            // 空了
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::hide);
        } else if (changedItem && changedCount) {
            // 都变了
            this.refreshDynamicElement((e, p) -> e.updateItemAndText(p, previousEmpty));
        } else if (changedItem) {
            // 只变了物品
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateDisplayItem);
        } else if (changedCount) {
            // 只变了数量
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
        this.templateItem = Item.empty();
        this.setItemCount(0);
        this.setChanged();
    }

    @Override
    public List<Item> contents() {
        return Collections.singletonList(this.templateItem);
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
    public InventoryHolder getOwner() {
        return this;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
