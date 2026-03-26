package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.behavior.DisplayItemBlockBehavior;
import net.momirealms.craftengine.bukkit.block.entity.renderer.DynamicDropItemRenderer;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public final class DisplayItemEntity extends BlockEntity {
    private final DisplayItemBlockBehavior behavior;
    @NotNull
    private Item displayItem;
    private WorldPosition displayItemPosition;
    private final Vector3f blockCenter;
    private final Vector3f relativePosition;

    public DisplayItemEntity(@NotNull BlockPos pos,
                             @NotNull ImmutableBlockState blockState,
                             @NotNull Vector3f relativePosition
    ) {
        super(BukkitBlockEntityTypes.DISPLAY_ITEM, pos, blockState);
        this.behavior = blockState.behavior().getAs(DisplayItemBlockBehavior.class).orElseThrow();
        this.blockCenter = new Vector3f((float) (pos.x + 0.5), (float) (pos.y + 0.5), (float) (pos.z + 0.5));
        this.relativePosition = relativePosition;
        this.displayItem = BukkitItemManager.instance().emptyItem();
        this.displayItemPosition = this.calculateDisplayItemPosition();
        this.blockEntityRenderer = new DynamicDropItemRenderer(this, this.displayItemPosition);
    }

    @Override
    public BlockEntityType<? extends BlockEntity> type() {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.DISPLAY_ITEM);
    }

    @NotNull
    public Item displayItem() {
        return this.displayItem;
    }

    // 放入方块内的展示物品
    public void putDisplayItem(Item inputItem, Player player) {
        this.displayItem = inputItem;
        if (super.blockEntityRenderer != null && super.blockEntityRenderer instanceof DynamicDropItemRenderer dynamicDropItemRenderer) {
            CEChunk chunk = super.world.getChunkAtIfLoaded(super.pos.x >> 4, super.pos.z >> 4);
            if (chunk != null) {
                for (Player trackedPlayer : chunk.getTrackedBy()) {
                    dynamicDropItemRenderer.update(trackedPlayer);
                }
            }
        }
    }

    // 取走方块内的展示物品
    public Item takeDisplayItem(Player player) {
        Item temp = this.displayItem;
        this.displayItem = BukkitItemManager.instance().emptyItem();
        if (super.blockEntityRenderer != null && super.blockEntityRenderer instanceof DynamicDropItemRenderer dynamicDropItemRenderer) {
            CEChunk chunk = super.world.getChunkAtIfLoaded(super.pos.x >> 4, super.pos.z >> 4);
            if (chunk != null) {
                for (Player trackedPlayer : chunk.getTrackedBy()) {
                    dynamicDropItemRenderer.update(trackedPlayer);
                }
            }
        }
        return temp;
    }

    @Override
    public void setBlockState(ImmutableBlockState blockState) {
        super.setBlockState(blockState);
        this.displayItemPosition = this.calculateDisplayItemPosition();
        if (super.blockEntityRenderer != null && super.blockEntityRenderer instanceof DynamicDropItemRenderer dynamicDropItemRenderer) {
            // 刷新Render内部缓存的生成包
            dynamicDropItemRenderer.refreshSpawnVehicleAndPassengerPacket(this.displayItemPosition, true);
            // 发送数据包
            CEChunk chunk = super.world.getChunkAtIfLoaded(super.pos.x >> 4, super.pos.z >> 4);
            if (chunk != null) {
                for (Player trackedPlayer : chunk.getTrackedBy()) {
                    dynamicDropItemRenderer.update(trackedPlayer);
                }
            }
            // 更新脏位, 必须得所有玩家都刷完了才更新.
            dynamicDropItemRenderer.positionDirty(false);
        }
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
        if (super.blockEntityRenderer instanceof DynamicDropItemRenderer dynamicDropItemRenderer) {
            dynamicDropItemRenderer.refreshChangeDisplayItemPacket(this.displayItem.getMinecraftItem());
        }
    }

    // 保存方块内存储的物品
    @Override
    protected void saveCustomData(CompoundTag tag) {
        if (!ItemUtils.isEmpty(displayItem)) {
            tag.put("display_item", ItemStackUtils.saveMinecraftItemStackAsTag(this.displayItem.getMinecraftItem()));
        }
    }

    // 移除方块时
    @Override
    public void preRemove() {
        if (!ItemUtils.isEmpty(displayItem)) {
            super.world.world().dropItemNaturally(this.displayItemPosition, this.displayItem);
        }
        this.displayItem = BukkitItemManager.instance().emptyItem();;
    }

    // 根据当前状态计算最终渲染DisplayItem的位置
    public WorldPosition calculateDisplayItemPosition() {
        float angleDeg;
        HorizontalDirection direction = blockState.get(behavior.directionProperty, HorizontalDirection.SOUTH);
        switch (direction) {
            case NORTH-> angleDeg = 0f;
            case EAST -> angleDeg = 90f;
            case SOUTH -> angleDeg = 180f;
            case WEST -> angleDeg = 270f;
            default -> angleDeg = 0f;
        }
        double angleRad = Math.toRadians(angleDeg);

        float x = -relativePosition.x;
        float z = relativePosition.z;
        double rotatedX = x * Math.cos(angleRad) - z * Math.sin(angleRad);
        double rotatedZ = x * Math.sin(angleRad) + z * Math.cos(angleRad);

        return new WorldPosition(null, this.blockCenter.x + rotatedX, this.blockCenter.y + this.relativePosition.y, this.blockCenter.z + rotatedZ);
    }
}
