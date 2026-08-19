package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.antigrieflib.Flag;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.entity.DrawerBlockEntityController;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlock;
import net.momirealms.craftengine.core.block.behavior.WorldlyContainerHolder;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.*;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.proxy.minecraft.server.level.ServerPlayerProxy;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.UUID;

public final class DrawerBlockBehavior extends BukkitBlockBehavior implements EntityBlock, WorldlyContainerHolder {
    public static final BlockBehaviorFactory<DrawerBlockBehavior> FACTORY = new DrawerBlockBehavior.Factory();
    public final SoundData putSound;
    public final SoundData takeSound;
    public final boolean hasAnalogOutputSignal;
    public final Vector3f itemPosition;
    public final Vector3f textPosition;
    public final Vector3f itemScale;
    public final Vector3f textScale;
    public final int maxStacks;
    @Nullable
    public final Property<Direction> directionProperty;
    public final boolean canPlaceItem;
    public final boolean canTakeItem;
    public final String customDataKey;
    public final boolean compatibleMode;
    private int controllerId;

    public DrawerBlockBehavior(BlockDefinition blockDefinition,
                               SoundData putSound,
                               SoundData takeSound,
                               boolean hasAnalogOutputSignal,
                               Vector3f itemPosition,
                               Vector3f textPosition,
                               Vector3f itemScale,
                               Vector3f textScale,
                               int maxStacks,
                               boolean canPlaceItem,
                               boolean canTakeItem,
                               @Nullable Property<Direction> directionProperty,
                               String customDataKey,
                               boolean compatibleMode
    ) {
        super(blockDefinition);
        this.putSound = putSound;
        this.takeSound = takeSound;
        this.hasAnalogOutputSignal = hasAnalogOutputSignal;
        this.itemPosition = itemPosition;
        this.textPosition = textPosition;
        this.itemScale = itemScale;
        this.textScale = textScale;
        this.maxStacks = maxStacks;
        this.directionProperty = directionProperty;
        this.customDataKey = customDataKey;
        this.canPlaceItem = canPlaceItem;
        this.canTakeItem = canTakeItem;
        this.compatibleMode = compatibleMode;
    }

    @Override
    public BlockEntityController createBlockEntityController(BlockEntity blockEntity) {
        return DrawerBlockEntityController.create(blockEntity, this);
    }

    @Override
    public void initControllerId(int id) {
        this.controllerId = id;
    }

    @Override
    public Object getContainer(Object thisBlock, Object[] args) {
        CEWorld ceWorld = BukkitAdaptor.adapt(LevelProxy.INSTANCE.getWorld(args[1])).storageWorld();
        BlockPos blockPos = LocationUtils.fromBlockPos(args[2]);
        BlockEntity blockEntity = ceWorld.getBlockEntityAtIfLoaded(blockPos);
        if (blockEntity == null) return null;
        return blockEntity.controller.let(DrawerBlockEntityController.class, this.controllerId, DrawerBlockEntityController::container);
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
            if (controller.isFull()) return InteractionResult.SUCCESS_AND_CANCEL;

            UUID playerId = player.uuid();
            long now = System.currentTimeMillis();
            Item storedItem = controller.item();
            UUID lastClickPlayer = controller.lastClickPlayer();
            long lastClickTime = controller.lastClickTime();

            boolean isDoubleClick = playerId.equals(lastClickPlayer) && (now - lastClickTime) <= 500;
            boolean hasStoredItem = !storedItem.isEmpty();
            boolean handHasItem = !itemInHand.isEmpty();
            Item oldItem = snapshotItem(controller);

            // 双击批量放入背包里所有相似物品
            if (hasStoredItem && isDoubleClick) {
                // 清理缓存
                controller.lastClickPlayer(null);
                controller.lastClickTime(0);
                // 先计算能放入多少个
                int matchedCount = player.clearOrCountMatchingInventoryItems(item -> item.isSimilar(storedItem), 0);
                int actuallyAdded = controller.add(matchedCount);
                if (actuallyAdded > 0) {
                    player.clearOrCountMatchingInventoryItems(item -> item.isSimilar(storedItem), actuallyAdded);
                    logTransaction(player, world, pos, oldItem, controller);
                    if (this.putSound != null) world.playBlockSound(Vec3d.atCenterOf(pos), this.putSound);
                    player.swingHand(hand);
                    return InteractionResult.SUCCESS_AND_CANCEL;
                }
            }
            // 单击放入手中物品（空存储或物品相似）
            else if (handHasItem && (!hasStoredItem || storedItem.isSimilar(itemInHand))) {
                // 第一次点击或者间隔超过500ms, 放入手中所有物品
                int count = itemInHand.count();
                int actuallyPut = controller.put(itemInHand.copyWithCount(1), count);
                itemInHand.shrink(actuallyPut);
                if (actuallyPut > 0) {
                    logTransaction(player, world, pos, oldItem, controller);
                }
                // 更新点击时间, 等待可能的二次点击
                controller.lastClickTime(now);
                controller.lastClickPlayer(playerId);
                player.swingHand(hand);
                if (this.putSound != null) world.playBlockSound(Vec3d.atCenterOf(pos), this.putSound);
                return InteractionResult.SUCCESS_AND_CANCEL;
            }

            return InteractionResult.SUCCESS_AND_CANCEL;
        });
    }

    @Override
    public void attack(Object thisBlock, Object[] args) {
        BlockPos pos = LocationUtils.fromBlockPos(args[2]);
        BukkitServerPlayer player = BukkitAdaptor.adapt(ServerPlayerProxy.INSTANCE.getBukkitEntity(args[3]));
        if (player == null) return;
        World world = player.world();
        BlockEntity blockEntity = world.storageWorld().getBlockEntityAtIfLoaded(pos);
        if (blockEntity == null) return;
        // 有保护, 不交互, 开始挖掘.
        Location location = new Location((org.bukkit.World) world.platformWorld(), pos.x, pos.y, pos.z);
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player.platformPlayer(), Flag.OPEN_CONTAINER, location)) {
            return;
        }
        blockEntity.controller.let(DrawerBlockEntityController.class, this.controllerId, controller -> {
            Item storedItem = controller.item();
            if (storedItem.isEmpty() || controller.itemCount() <= 0) return;
            Item oldItem = snapshotItem(controller);

            Item itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            boolean handEmpty = itemInHand.isEmpty();
            boolean takeGroup = player.isSneaking();

            // 判断是否可以取出物品
            if (!handEmpty) {
                boolean isHandFull = itemInHand.count() == itemInHand.maxStackSize();
                boolean notSimilar = !itemInHand.isSimilar(storedItem);
                if (isHandFull || notSimilar) return;
            }

            // 计算可取出数量
            int takeAmount = 1;
            if (takeGroup) {
                int available = handEmpty ? storedItem.maxStackSize() : storedItem.maxStackSize() - itemInHand.count();
                takeAmount = Math.min(available, controller.itemCount());
            }

            // 取出物品
            int actuallyTaken = controller.take(takeAmount, item -> {
                if (handEmpty) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, item);
                } else {
                    itemInHand.grow(item.count());
                }
            }, true);
            if (actuallyTaken > 0) {
                logTransaction(player, world, pos, oldItem, controller);
            }

            player.swingHand(InteractionHand.MAIN_HAND);
            if (this.takeSound != null) world.playBlockSound(Vec3d.atCenterOf(pos), this.takeSound);
        });
    }

    private static void logTransaction(Player player,
                                       World world,
                                       BlockPos pos,
                                       @Nullable Item oldItem,
                                       DrawerBlockEntityController controller) {
        BukkitCraftEngine.instance().compatibilityManager().logSingleSlotContainerTransaction(
                player,
                new WorldPosition(world, pos),
                oldItem,
                snapshotItem(controller)
        );
    }

    @Nullable
    private static Item snapshotItem(DrawerBlockEntityController controller) {
        Item item = controller.item();
        int count = controller.itemCount();
        if (item.isEmpty() || count <= 0) {
            return null;
        }
        return item.copyWithCount(count);
    }

    // 比较器红石信号.
    @Override
    public int getAnalogOutputSignal(Object thisBlock, Object[] args) {
        if (!this.hasAnalogOutputSignal) return 0;
        Object world = args[1];
        Object blockPos = args[2];
        BlockPos pos = LocationUtils.fromBlockPos(blockPos);
        org.bukkit.World bukkitWorld = LevelProxy.INSTANCE.getWorld(world);
        CEWorld ceWorld = BukkitAdaptor.adapt(bukkitWorld).storageWorld();
        BlockEntity blockEntity = ceWorld.getBlockEntityAtIfLoaded(pos);
        if (blockEntity == null) {
            return 0;
        }
        return blockEntity.controller.let(DrawerBlockEntityController.class, this.controllerId, c -> {
            if (ItemUtils.isEmpty(c.item())) {
                return 0;
            }
            return MiscUtils.lerpDiscrete((float) c.itemCount() / c.maxCount(), 0, 15);
        });
    }

    @Override
    public boolean hasAnalogOutputSignal(Object thisBlock, Object[] args) {
        return this.hasAnalogOutputSignal;
    }

    private static class Factory implements BlockBehaviorFactory<DrawerBlockBehavior> {
        private static final String[] HAS_SIGNAL = ConfigKeys.of("has_signal");
        private static final String[] ITEM_POSITION = ConfigKeys.of("item_position");
        private static final String[] TEXT_POSITION = ConfigKeys.of("text_position");
        private static final String[] ITEM_SCALE = ConfigKeys.of("item_scale");
        private static final String[] TEXT_SCALE = ConfigKeys.of("text_scale");
        private static final String[] MAX_STACKS = ConfigKeys.of("max_stacks");
        private static final String[] DATA_KEY = ConfigKeys.of("data_key");
        private static final String[] ALLOW_INPUT = ConfigKeys.of("allow_input");
        private static final String[] ALLOW_OUTPUT = ConfigKeys.of("allow_output");
        private static final String[] COMPATIBLE_MODE = ConfigKeys.of("compatible_mode");

        @Override
        public DrawerBlockBehavior create(BlockDefinition block, ConfigSection section) {
            // 读取展示物品和文本相对位置和尺寸
            Vector3f itemPosition = section.getVector3f(ITEM_POSITION, ConfigConstants.CENTER_VECTOR3);
            Vector3f textPosition = section.getVector3f(TEXT_POSITION, ConfigConstants.CENTER_VECTOR3);
            Vector3f itemScale = section.getVector3f(ITEM_SCALE, ConfigConstants.CENTER_VECTOR3);
            Vector3f textScale = section.getVector3f(TEXT_SCALE, ConfigConstants.CENTER_VECTOR3);
            int maxStacks = section.getInt(MAX_STACKS, 32);
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
            boolean compatibleMode = section.getBoolean(COMPATIBLE_MODE, false);
            return new DrawerBlockBehavior(
                    block,
                    putSound,
                    takeSound,
                    section.getBoolean(HAS_SIGNAL, true),
                    itemPosition,
                    textPosition,
                    itemScale,
                    textScale,
                    maxStacks,
                    section.getBoolean(ALLOW_INPUT, true),
                    section.getBoolean(ALLOW_OUTPUT, true),
                    facing,
                    section.getValue(DATA_KEY, ConfigValue::getAsNonEmptyString, "craftengine:drawer"),
                    compatibleMode
            );
        }
    }
}
