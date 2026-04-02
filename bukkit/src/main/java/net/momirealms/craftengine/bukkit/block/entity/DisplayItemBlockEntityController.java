package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.DisplayItemBlockBehavior;
import net.momirealms.craftengine.bukkit.block.entity.renderer.dynamic.DynamicItemBlockEntityElement;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;

public final class DisplayItemBlockEntityController extends BlockEntityController {
    private final DisplayItemBlockBehavior behavior;
    private final DynamicItemBlockEntityElement element;
    @NotNull
    private Item displayItem;
    private WorldPosition displayItemPosition;
    private final Vector3f blockCenter;

    public DisplayItemBlockEntityController(BlockEntity blockEntity, DisplayItemBlockBehavior behavior) {
        super(blockEntity);
        this.behavior = behavior;
        this.blockCenter = new Vector3f((float) (blockEntity.pos.x + 0.5), (float) (blockEntity.pos.y + 0.5), (float) (blockEntity.pos.z + 0.5));
        this.displayItem = BukkitItemManager.instance().emptyItem();
        this.displayItemPosition = this.calculateDisplayItemPosition();
        this.element = new DynamicItemBlockEntityElement(this, this.displayItemPosition);
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
    public Item displayItem() {
        return this.displayItem;
    }

    // 放入方块内的展示物品
    public void putDisplayItem(Item inputItem, Player player) {
        this.displayItem = inputItem;
        CEChunk chunk = super.blockEntity.world.getChunkAtIfLoaded(super.blockEntity.pos.x >> 4, super.blockEntity.pos.z >> 4);
        if (chunk != null) {
            for (Player trackedPlayer : chunk.getTrackedBy()) {
                this.element.update(trackedPlayer);
            }
        }
    }

    // 取走方块内的展示物品
    public Item takeDisplayItem(Player player) {
        Item temp = this.displayItem;
        this.displayItem = BukkitItemManager.instance().emptyItem();
        CEChunk chunk = super.blockEntity.world.getChunkAtIfLoaded(super.blockEntity.pos.x >> 4, super.blockEntity.pos.z >> 4);
        if (chunk != null) {
            for (Player trackedPlayer : chunk.getTrackedBy()) {
                this.element.update(trackedPlayer);
            }
        }
        return temp;
    }

    @Override
    public void onBlockStateChange(ImmutableBlockState blockState) {
        this.displayItemPosition = this.calculateDisplayItemPosition();
        this.element.refreshSpawnVehicleAndPassengerPacket(this.displayItemPosition, true);
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
        Tag itemTag = tag.get("display_item");
        if (itemTag == null) {
            this.displayItem = BukkitItemManager.instance().emptyItem();
            return;
        }
        // 如果里面有物品, 同时刷新Render的包缓存.
        int dataVersion = tag.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
        this.displayItem = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(itemTag, dataVersion));
        this.element.refreshChangeDisplayItemPacket(this.displayItem.getMinecraftItem());
    }

    @Override
    public void saveCustomData(CompoundTag tag) {
        if (!ItemUtils.isEmpty(displayItem)) {
            tag.put("display_item", ItemStackUtils.saveMinecraftItemStackAsTag(this.displayItem.getMinecraftItem()));
        }
    }

    @Override
    public void onRemove() {
        if (!ItemUtils.isEmpty(displayItem)) {
            super.blockEntity.world.world().dropItemNaturally(this.displayItemPosition, this.displayItem);
        }
        this.displayItem = BukkitItemManager.instance().emptyItem();;
    }

    public WorldPosition calculateDisplayItemPosition() {
        float angleDeg;
        Direction direction = super.blockEntity.blockState.get(behavior.directionProperty, Direction.SOUTH);
        switch (direction) {
            case EAST -> angleDeg = 90f;
            case SOUTH -> angleDeg = 180f;
            case WEST -> angleDeg = 270f;
            default -> angleDeg = 0f;
        }
        double angleRad = Math.toRadians(angleDeg);

        float x = -this.behavior.relativePosition.x;
        float z = this.behavior.relativePosition.z;
        double rotatedX = x * Math.cos(angleRad) - z * Math.sin(angleRad);
        double rotatedZ = x * Math.sin(angleRad) + z * Math.cos(angleRad);

        return new WorldPosition(null,
                this.blockCenter.x + rotatedX,
                this.blockCenter.y + this.behavior.relativePosition.y,
                this.blockCenter.z + rotatedZ
        );
    }
}
