package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.block.entity.renderer.DynamicDropItemRenderer;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.entity.render.DynamicBlockEntityRenderer;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class DisplayItemEntity extends BlockEntity {
    @Nullable
    private Item displayItem;
    @Nullable
    private DynamicDropItemRenderer cachedRenderer;
    private WorldPosition displayItemPosition;
    private final HorizontalDirection direction;
    private final Vector3f blockCenter;
    private final Vector3f relative;

    public DisplayItemEntity(@NotNull BlockPos pos,
                             @NotNull ImmutableBlockState blockState,
                             @NotNull Vector3f relative,
                             @Nullable Property<HorizontalDirection> directionProperty
    ) {
        super(BukkitBlockEntityTypes.DISPLAY_ITEM, pos, blockState);
        this.direction = blockState.get(directionProperty, HorizontalDirection.SOUTH);
        this.blockCenter = new Vector3f((float) (pos.x + 0.5), (float) (pos.y + 0.5), (float) (pos.z + 0.5));
        this.relative = relative;
    }

    @Override
    public BlockEntityType<? extends BlockEntity> type() {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.DISPLAY_ITEM);
    }

    @NotNull
    public Item displayItem() {
        if (this.displayItem == null) {
            return ItemStackUtils.wrap(null);
        }
        return this.displayItem;
    }

    // 放入方块内的展示物品
    public void putDisplayItem(Item inputItem, Player player) {
        this.displayItem = inputItem;
        if (cachedRenderer != null) {
            cachedRenderer.displayItem(inputItem);
            cachedRenderer.update(player);
        }
    }

    // 取走方块内的展示物品
    public Item takeDisplayItem(Player player) {
        Item temp = this.displayItem;
        this.displayItem = null;
        if (cachedRenderer != null) {
            cachedRenderer.displayItem(BukkitItemManager.instance().emptyItem());
            cachedRenderer.update(player);
        }
        return temp;
    }

    @Override
    public @Nullable DynamicBlockEntityRenderer blockEntityRenderer() {
        float angleDeg;
        switch (direction) {
            case NORTH-> angleDeg = 0f;
            case EAST -> angleDeg = 90f;
            case SOUTH -> angleDeg = 180f;
            case WEST -> angleDeg = 270f;
            default -> angleDeg = 0f;
        }
        double angleRad = Math.toRadians(angleDeg);

        float x = -relative.x();
        float z = relative.z();
        double rotatedX = x * Math.cos(angleRad) - z * Math.sin(angleRad);
        double rotatedZ = x * Math.sin(angleRad) + z * Math.cos(angleRad);

        this.displayItemPosition = new WorldPosition(world.world(), blockCenter.x + rotatedX, blockCenter.y + relative.y, blockCenter.z + rotatedZ);
        this.cachedRenderer = new DynamicDropItemRenderer(displayItem(), this.displayItemPosition);
        return cachedRenderer;
    }

    // 读取方块内存储的物品
    @Override
    public void loadCustomData(CompoundTag tag) {
        Tag itemTag = tag.get("display_item");
        if (itemTag != null) {
            int dataVersion = tag.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
            this.displayItem = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(itemTag, dataVersion));
        }
    }

    // 保存方块内存储的物品
    @Override
    protected void saveCustomData(CompoundTag tag) {
        if (!ItemUtils.isEmpty(displayItem)) {
            tag.put("display_item", ItemStackUtils.saveMinecraftItemStackAsTag(displayItem.getMinecraftItem()));
        }
    }

    // 移除方块时
    @Override
    public void preRemove() {
        if (!ItemUtils.isEmpty(displayItem)) {
            super.world.world().dropItemNaturally(this.displayItemPosition, displayItem);
        }
        this.displayItem = null;
    }
}
