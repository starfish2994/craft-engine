package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.antigrieflib.Flag;
import net.momirealms.craftengine.bukkit.block.entity.DrawerBlockEntityController;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

public class DrawerBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final BlockBehaviorFactory<DrawerBlockBehavior> FACTORY = new DrawerBlockBehavior.Factory();
    public final SoundData putSound;
    public final SoundData takeSound;
    public final boolean hasAnalogOutputSignal;
    public final Vector3f itemPosition;
    public final Vector3f textPosition;
    public final Vector3f itemScale;
    public final Vector3f textScale;
    public final int maxStorageCount;
    @Nullable
    public final Property<Direction> directionProperty;
    private int controllerId;
    @Nullable
    public final String customDataKey;

    public DrawerBlockBehavior(BlockDefinition blockDefinition,
                               SoundData putSound,
                               SoundData takeSound,
                               boolean hasAnalogOutputSignal,
                               Vector3f itemPosition,
                               Vector3f textPosition,
                               Vector3f itemScale,
                               Vector3f textScale,
                               int maxStorageCount,
                               @Nullable Property<Direction> directionProperty, @Nullable String customDataKey
    ) {
        super(blockDefinition);
        this.putSound = putSound;
        this.takeSound = takeSound;
        this.hasAnalogOutputSignal = hasAnalogOutputSignal;
        this.itemPosition = itemPosition;
        this.textPosition = textPosition;
        this.itemScale = itemScale;
        this.textScale = textScale;
        this.maxStorageCount = maxStorageCount;
        this.directionProperty = directionProperty;
        this.customDataKey = customDataKey;
    }

    @Override
    public BlockEntityController createController(BlockEntity blockEntity, int controllerId) {
        this.controllerId = controllerId;
        return new DrawerBlockEntityController(blockEntity, this);
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = world.storageWorld().getBlockEntityAtIfLoaded(pos);
        if (blockEntity == null) return InteractionResult.PASS;
        Location location = new Location((org.bukkit.World) context.getWorld().platformWorld(), pos.x, pos.y, pos.z);
        if (!BukkitCraftEngine.instance().antiGriefProvider().test((org.bukkit.entity.Player) player.platformPlayer(), Flag.OPEN_CONTAINER, location)) {
            return InteractionResult.FAIL;
        }
        InteractionHand hand = context.getHand();
        Item itemInHand = player.getItemInHand(hand);
        return blockEntity.controller.let(DrawerBlockEntityController.class, this.controllerId, controller -> {
            if (controller.isFull()) return InteractionResult.FAIL;

            final UUID playerId = player.uuid();
            final long now = System.currentTimeMillis();
            Item storedItem = controller.storedItem();
            final UUID lastClickPlayer = controller.lastClickPlayer();
            final long lastClickTime = controller.lastClickTime() == null ? 0 : controller.lastClickTime();

            boolean isDoubleClick = playerId.equals(lastClickPlayer) && (now - lastClickTime) <= 500;
            boolean hasStoredItem = !storedItem.isEmpty();
            boolean handHasItem = !itemInHand.isEmpty();

            // 双击批量放入背包里所有相似物品
            if (hasStoredItem && isDoubleClick) {
                int putAmount = player.clearOrCountMatchingInventoryItems(item -> item.isSimilar(storedItem), Integer.MAX_VALUE);
                if (putAmount > 0) {
                    controller.growStorageCount(putAmount);
                }
                controller.lastClickPlayer(null);
                controller.lastClickTime(null);
                player.swingHand(hand);
                if (this.putSound != null) world.playBlockSound(Vec3d.atCenterOf(pos), this.putSound);
                return InteractionResult.SUCCESS_AND_CANCEL;
            }

            // 单击放入手中物品（空存储或物品相似）
            if (handHasItem && (!hasStoredItem || storedItem.isSimilar(itemInHand))) {
                // 第一次点击或者间隔超过500ms, 放入手中所有物品
                int count = itemInHand.count();
                Item toInsert = itemInHand.copyWithCount(count);
                controller.putStorageItem(toInsert);
                itemInHand.shrink(count);
                // 更新点击时间, 等待可能的二次点击
                controller.lastClickTime(now);
                controller.lastClickPlayer(playerId);
                player.swingHand(hand);
                if (this.putSound != null) world.playBlockSound(Vec3d.atCenterOf(pos), this.putSound);
                return InteractionResult.SUCCESS_AND_CANCEL;
            }

            return InteractionResult.TRY_EMPTY_HAND;
        });
    }

    @Override
    public void onMiningStart(ImmutableBlockState state, BlockPos pos, Player player, InteractionHand hand, Item tool) {
        World world = player.world();
        BlockEntity blockEntity = world.storageWorld().getBlockEntityAtIfLoaded(pos);
        if (blockEntity == null) return;
        // 有保护, 不交互, 开始挖掘.
        Location location = new Location((org.bukkit.World) world.platformWorld(), pos.x, pos.y, pos.z);
        if (!BukkitCraftEngine.instance().antiGriefProvider().test((org.bukkit.entity.Player) player.platformPlayer(), Flag.OPEN_CONTAINER, location)) {
            return;
        }
        blockEntity.controller.let(DrawerBlockEntityController.class, this.controllerId, controller -> {
            Item storedItem = controller.storedItem();
            if (storedItem.isEmpty() || controller.storageCount() <= 0) return;

            boolean handEmpty = ItemUtils.isEmpty(tool);
            boolean takeGroup = player.isSneaking();

            // 判断是否可以取出物品
            if (!handEmpty) {
                boolean isHandFull = tool.count() == tool.maxStackSize();
                boolean notSimilar = !tool.isSimilar(storedItem);
                if (isHandFull || notSimilar) return;
            }

            // 计算可取出数量
            int takeAmount = 1;
            if (takeGroup) {
                int available = ItemUtils.isEmpty(tool) ? storedItem.maxStackSize() : storedItem.maxStackSize() - tool.count();
                takeAmount = Math.min(available, controller.storageCount());
            }

            // 取出物品
            Item takenItem = controller.takeStorageItem(takeAmount);
            player.setItemInHand(hand, takenItem);
            player.swingHand(hand);
            if (this.takeSound != null) world.playBlockSound(Vec3d.atCenterOf(pos), this.takeSound);
        });
    }

    // 比较器红石信号.
    @Override
    public int getAnalogOutputSignal(Object thisBlock, Object[] args) {
        if (!this.hasAnalogOutputSignal) return 0;
        Object world = args[1];
        Object blockPos = args[2];
        BlockPos pos = LocationUtils.fromBlockPos(blockPos);
        org.bukkit.World bukkitWorld = LevelProxy.INSTANCE.getWorld(world);
        CEWorld ceWorld = BukkitWorldManager.instance().getWorld(bukkitWorld.getUID());
        BlockEntity blockEntity = ceWorld.getBlockEntityAtIfLoaded(pos);
        if (blockEntity == null) {
            return 0;
        }
        return blockEntity.controller.let(DrawerBlockEntityController.class, this.controllerId, c -> {
            if (!ItemUtils.isEmpty(c.storedItem())) {
                float i = (float) c.storageCount() / this.maxStorageCount;
                return MiscUtils.lerpDiscrete(i, 0, 15);
            }
            return 0;
        });
    }

    @Override
    public boolean hasAnalogOutputSignal(Object thisBlock, Object[] args) {
        return this.hasAnalogOutputSignal;
    }

    private static class Factory implements BlockBehaviorFactory<DrawerBlockBehavior> {
        private static final String[] HAS_SIGNAL = new String[]{"has_signal", "has-signal"};
        private static final String[] ITEM_POSITION = new String[] {"item_position", "item-position"};
        private static final String[] TEXT_POSITION = new String[] {"text_position", "text-position"};
        private static final String[] ITEM_SCALE = new String[] {"item_scale", "item-scale"};
        private static final String[] TEXT_SCALE = new String[] {"text_scale", "text-scale"};
        private static final String[] MAX_STORAGE_COUNT = new String[] {"max_storage_count", "max-storage-count"};
        private static final String[] DATA_KEY = new String[] {"data_key", "data-key"};

        @Override
        public DrawerBlockBehavior create(BlockDefinition block, ConfigSection section) {
            // 读取展示物品和文本相对位置和尺寸
            Vector3f itemPosition = section.getVector3f(ITEM_POSITION, ConfigConstants.CENTER_VECTOR3);
            Vector3f textPosition = section.getVector3f(TEXT_POSITION, ConfigConstants.CENTER_VECTOR3);
            Vector3f itemScale = section.getVector3f(ITEM_SCALE, ConfigConstants.CENTER_VECTOR3);
            Vector3f textScale = section.getVector3f(TEXT_SCALE, ConfigConstants.CENTER_VECTOR3);
            int maxStorageCount = section.getInt(MAX_STORAGE_COUNT, 64);
            // 读取放入取出音效
            ConfigSection soundSection = section.getSection("sounds");
            SoundData putSound = null;
            SoundData takeSound = null;
            if (soundSection != null) {
                putSound = soundSection.getValue("put", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.RANGED_0_9_1));
                takeSound = soundSection.getValue("take", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.RANGED_0_9_1));
            }
            // 获取方向
            Property<Direction> facing = BlockBehaviorFactory.getOptionalProperty(block, "facing", Direction.class);
            return new DrawerBlockBehavior(
                    block,
                    putSound,
                    takeSound,
                    section.getBoolean(HAS_SIGNAL, true),
                    itemPosition,
                    textPosition,
                    itemScale,
                    textScale,
                    maxStorageCount,
                    facing,
                    section.getString(DATA_KEY)
            );
        }
    }
}
