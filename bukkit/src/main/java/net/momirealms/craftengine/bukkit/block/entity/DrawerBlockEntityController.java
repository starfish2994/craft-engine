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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public final class DrawerBlockEntityController extends BlockEntityController implements BukkitContainer, WorldlyContainer {
    private static final String DEFAULT_DATA_KEY = "craftengine:drawer";
    public final DrawerBlockBehavior behavior;
    public final DynamicDrawerBlockEntityElement element;
    private final Object container;
    private final InventoryHolder owner;
    private WorldPosition itemPosition;
    private WorldPosition textPosition;
    private float entityYRot;
    private final Vector3f blockCenter;
    private Item lastUpdateItem = Item.empty(); // 最后一次包发送的物品
    private int lastUpdateCount = 0; // 最后一次包发送的物品数量
    private UUID lastClickPlayer;
    private Long lastClickTime;
    private final Item[] items;
    private final int[] slots;

    public DrawerBlockEntityController(BlockEntity blockEntity, DrawerBlockBehavior behavior) {
        super(blockEntity);
        this.items = new Item[behavior.maxStacks];
        Arrays.fill(items, Item.empty());
        this.behavior = behavior;
        this.blockCenter = new Vector3f((float) (blockEntity.pos.x + 0.5), (float) (blockEntity.pos.y + 0.5), (float) (blockEntity.pos.z + 0.5));
        this.itemPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.itemPosition);
        this.textPosition = this.calculateDisplayPosition(blockEntity.blockState, this.behavior.textPosition);
        this.entityYRot = this.calculateYRot(blockEntity.blockState);
        this.element = new DynamicDrawerBlockEntityElement(this, this.itemPosition, this.textPosition, this.entityYRot);
        this.container = CraftEngine.instance().platform().createContainer(this);
        this.owner = new Holder(this);
        this.slots = IntStream.range(0, behavior.maxStacks).toArray();
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
        return this.items[0];
    }

    public int maxStorageCount(Item item) {
        return item.maxStackSize() * this.behavior.maxStacks;
    }

    public int maxStorageCount() {
        return this.items[0].maxStackSize() * this.behavior.maxStacks;
    }

    public int storageCount() {
        int count = 0;
        for (int i = 0; i < this.items.length; i++) {
            Item item = this.items[i];
            if (item.isEmpty()) {
                break;
            }
            count += item.count();
        }
        return count;
    }

    public boolean isFull() {
        Item lastItem = this.items[this.items.length - 1];
        if (lastItem.isEmpty()) {
            return false;
        }
        if (lastItem.count() < lastItem.maxStackSize()) {
            return false;
        }
        return true;
    }

    // 放入物品，返回值为实际放入的量
    public int put(Item inputItem /* Not Empty */, int count) {
        if (count <= 0) return 0;

        if (!isEmpty() && !this.items[0].isSimilar(inputItem)) {
            return 0;
        }

        int remainingToAdd = count;
        int maxStack = inputItem.maxStackSize();

        for (int i = 0; i < this.items.length && remainingToAdd > 0; i++) {
            if (this.items[i].isEmpty()) {
                int toAdd = Math.min(remainingToAdd, maxStack);
                this.items[i] = inputItem.copyWithCount(toAdd);
                remainingToAdd -= toAdd;
            } else {
                int currentCount = this.items[i].count();
                int space = maxStack - currentCount;
                if (space > 0) {
                    int toAdd = Math.min(remainingToAdd, space);
                    this.items[i].grow(toAdd);
                    remainingToAdd -= toAdd;
                }
            }
        }

        this.setChanged();
        return count - remainingToAdd;
    }

    // 增加存储物品的数量，返回值为实际增加的量
    public int add(int count) {
        if (!isEmpty() && count > 0) {
            Item baseItem = this.items[0];
            int maxStack = baseItem.maxStackSize();
            int remaining = count;

            for (int i = 0; i < this.items.length && remaining > 0; i++) {
                if (this.items[i].isEmpty()) {
                    int toAdd = Math.min(remaining, maxStack);
                    this.items[i] = baseItem.copyWithCount(toAdd);
                    remaining -= toAdd;
                } else {
                    int canAccept = maxStack - this.items[i].count();
                    int toAdd = Math.min(remaining, canAccept);
                    this.items[i].grow(toAdd);
                    remaining -= toAdd;
                }
            }

            this.setChanged();
            return count - remaining;
        }
        return 0;
    }

    // 取走方块内的物品，返回值为实际取走的量
    public int take(int count, Consumer<Item> consumer) {
        if (count <= 0 || isEmpty()) return 0;

        // 记录物品模板，用于后续生成 Item
        Item template = this.items[0];
        int maxStack = template.maxStackSize();
        int remainingToTake = count;
        for (int i = this.items.length - 1; i >= 0 && remainingToTake > 0; i--) {
            if (this.items[i].isEmpty()) continue;

            int canTakeFromSlot = Math.min(remainingToTake, this.items[i].count());
            this.items[i].shrink(canTakeFromSlot);
            remainingToTake -= canTakeFromSlot;

            if (this.items[i].count() <= 0) {
                this.items[i] = Item.empty();
            }
        }

        int actualTaken = count - remainingToTake;

        if (actualTaken > 0) {
            int toCallback = actualTaken;
            while (toCallback > 0) {
                int currentBatch = Math.min(toCallback, maxStack);
                consumer.accept(template.copyWithCount(currentBatch));
                toCallback -= currentBatch;
            }
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
        CompoundTag dataTag = tag.getCompound(Optional.ofNullable(behavior.customDataKey).orElse(DEFAULT_DATA_KEY));
        // 空数据
        if (dataTag == null) {
            return;
        }
        // 读取数据
        int dataVersion = dataTag.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
        Tag itemTag = dataTag.get("item");
        int count = dataTag.getInt("count", 0);
        // 非法数据
        if (itemTag == null || count <= 0) {
            return;
        }

        Item itemTemplate = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(itemTag, dataVersion));
        int maxStackSize = itemTemplate.maxStackSize();
        int remaining = count;

        for (int i = 0; i < this.items.length; i++) {
            if (remaining <= 0) {
                break;
            } else {
                int currentCount = Math.min(remaining, maxStackSize);
                this.items[i] = itemTemplate.copyWithCount(currentCount);
                remaining -= currentCount;
            }
        }

        this.element.refreshChangeDisplayItemPacket(this.items[0]);
        this.element.refreshChangeTextContentPacket(count);
        this.lastUpdateItem = this.items[0].copy();
        this.lastUpdateCount = count;
    }

    @Override
    public void saveCustomData(CompoundTag tag) {
        if (!isEmpty() && this.storageCount() > 0) {
            CompoundTag compoundTag = MiscUtils.init(new CompoundTag(), dataTag -> {
                dataTag.put("data_version", new IntTag(Config.itemDataFixerUpperFallbackVersion()));
                dataTag.put("count", new IntTag(this.storageCount()));
                dataTag.put("item", ItemStackUtils.saveMinecraftItemStackAsTag(this.items[0].copyWithCount(1).minecraftItem()));
            });
            tag.put(Optional.ofNullable(behavior.customDataKey).orElse(DEFAULT_DATA_KEY), compoundTag);
        }
    }

    @Override
    public void onRemove() {
        for (Item item : this.items) {
            super.blockEntity.world.world().dropItemNaturally(this.itemPosition, item);
        }
        Arrays.fill(this.items, Item.empty());
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
        this.lastUpdateItem = displayItem.copy();
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
        return this.slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, Item stack, Direction direction) {
        if (!this.isEmpty() && !stack.isSimilar(this.items[0])) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, Item stack, Direction direction) {
        if (slot < 0 || slot >= this.items.length || this.items[slot].isEmpty()) {
            return false;
        }
        // 保证漏斗从后往前遍历
        if (slot == this.items.length - 1) {
            return true;
        }
        return this.items[slot + 1].isEmpty();
    }

    @Override
    public int containerSize() {
        return this.behavior.maxStacks;
    }

    @Override
    public boolean isEmpty() {
        return this.items[0].isEmpty();
    }

    @Override
    public Item getItem(int slot) {
        return this.items[slot];
    }

    @Override
    public Item removeItem(int slot, int count) {
        Item item = this.items[slot];
        if (item.isEmpty()) return item;
        Item result;
        if (item.count() <= count) {
            this.setItem(slot, Item.empty());
            result = item;
        } else {
            result = item.copyWithCount(count);
            item.shrink(count);
        }
        this.setChanged();
        return result;
    }

    @Override
    public Item removeItemNoUpdate(int slot) {
        Item item = this.items[slot];
        if (item.isEmpty()) return item;
        Item result;
        if (item.count() <= 1) {
            this.setItem(slot, Item.empty());
            result = item;
        } else {
            result = item.copyWithCount(1);
            item.shrink(1);
        }
        return result;
    }

    @Override
    public void setItem(int slot, Item item) {
        this.items[slot] = item;
    }

    @Override
    public int maxStackSize() {
        return 99;
    }

    @Override
    public void setChanged() {
        boolean previousEmpty = this.lastUpdateItem.isEmpty();
        boolean isEmpty = this.items[0].isEmpty();
        boolean changedItem = this.refreshItemDisplayPacket();
        boolean changedCount = this.refreshTextContentPacket();

        // 空了
        if (isEmpty) {
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::hide);
        }
        // 都变了
        else if (changedItem && changedCount) {
            this.refreshDynamicElement((e, p) -> e.updateItemAndText(p, previousEmpty));
        }
        // 只变了物品
        else if (changedItem) {
            this.refreshDynamicElement(DynamicDrawerBlockEntityElement::updateDisplayItem);
        }
        // 只变了数量
        else if (changedCount) {
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
        Arrays.fill(this.items, Item.empty());
        this.setChanged();
    }

    @Override
    public List<Item> contents() {
        return Arrays.asList(this.items);
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
        private final Inventory inventory;

        public Holder(DrawerBlockEntityController container) {
            this.inventory = CraftInventoryProxy.INSTANCE.newInstance(container.container);
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }
    }
}
