package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.entity.BukkitBlockEntityTypes;
import net.momirealms.craftengine.bukkit.block.entity.DisplayItemEntity;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.behavior.EntityBlockBehavior;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityType;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.proxy.minecraft.world.level.LevelProxy;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class DisplayItemBlockBehavior extends BukkitBlockBehavior implements EntityBlockBehavior {
    public static final BlockBehaviorFactory<DisplayItemBlockBehavior> FACTORY = new Factory();
    public final SoundData putSound;
    public final SoundData takeSound;
    public final boolean hasAnalogOutputSignal;
    public final Vector3f relativePosition;
    @Nullable
    public final Property<HorizontalDirection> directionProperty;

    public DisplayItemBlockBehavior(CustomBlock customBlock,
                                    SoundData putSound,
                                    SoundData takeSound,
                                    boolean hasAnalogOutputSignal,
                                    Vector3f relativePosition,
                                    @Nullable Property<HorizontalDirection> directionProperty
    ) {
        super(customBlock);
        this.putSound = putSound;
        this.takeSound = takeSound;
        this.hasAnalogOutputSignal = hasAnalogOutputSignal;
        this.relativePosition = relativePosition;
        this.directionProperty = directionProperty;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = world.storageWorld().getBlockEntityAtIfLoaded(pos);
        if (!(blockEntity instanceof DisplayItemEntity displayItemEntity && displayItemEntity.isValid())) {
            return InteractionResult.PASS;
        }

        InteractionHand hand = context.getHand();
        Item itemInHand = player.getItemInHand(hand);
        // 放入物品
        if (!ItemUtils.isEmpty(itemInHand) && ItemUtils.isEmpty(displayItemEntity.displayItem())) {
            Item inputItem = itemInHand.copyWithCount(1);
            if (!player.canInstabuild()) {
                itemInHand.shrink(1);
            }
            displayItemEntity.putDisplayItem(inputItem, player);
            player.swingHand(hand);
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        // 取出物品
        else if (ItemUtils.isEmpty(itemInHand) && !ItemUtils.isEmpty(displayItemEntity.displayItem())) {
            Item takedItem = displayItemEntity.takeDisplayItem(player);
            player.setItemInHand(hand, takedItem);
            player.swingHand(hand);
            return InteractionResult.SUCCESS_AND_CANCEL;
        }

        return InteractionResult.TRY_EMPTY_HAND;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityType<T> blockEntityType(ImmutableBlockState state) {
        return EntityBlockBehavior.blockEntityTypeHelper(BukkitBlockEntityTypes.DISPLAY_ITEM);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, ImmutableBlockState state) {
        return new DisplayItemEntity(pos, state, this.relativePosition);
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
        if (blockEntity instanceof DisplayItemEntity entity) {
            if (!ItemUtils.isEmpty(entity.displayItem())) {
                return 15;
            }
        }
        return 0;
    }

    @Override
    public boolean hasAnalogOutputSignal(Object thisBlock, Object[] args) {
        return this.hasAnalogOutputSignal;
    }

    private static class Factory implements BlockBehaviorFactory<DisplayItemBlockBehavior> {
        private static final String[] HAS_SIGNAL = new String[]{"has_signal", "has-signal"};

        @Override
        public DisplayItemBlockBehavior create(CustomBlock block, ConfigSection section) {
            // 读取展示相对位置
            Vector3f position = section.getVector3f("position", ConfigConstants.CENTER_VECTOR3);
            // 读取放入取出音效
            ConfigSection soundSection = section.getSection("sounds");
            SoundData putSound = null;
            SoundData takeSound = null;
            if (soundSection != null) {
                putSound = soundSection.getValue("put", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.RANGED_0_9_1));
                takeSound = soundSection.getValue("take", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.RANGED_0_9_1));
            }
            // 获取方向
            Property<HorizontalDirection> facing = BlockBehaviorFactory.getOptionalProperty(block, "facing", HorizontalDirection.class);
            return new DisplayItemBlockBehavior(
                    block,
                    putSound,
                    takeSound,
                    section.getBoolean(HAS_SIGNAL, true),
                    position,
                    facing
            );
        }
    }
}
