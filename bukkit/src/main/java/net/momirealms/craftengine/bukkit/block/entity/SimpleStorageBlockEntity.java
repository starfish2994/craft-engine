package net.momirealms.craftengine.bukkit.block.entity;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.block.behavior.SimpleStorageBlockBehavior;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.nms.StorageContainer;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MRegistryOps;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import org.bukkit.GameEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class SimpleStorageBlockEntity extends BlockEntity {
    private final SimpleStorageBlockBehavior behavior;
    private final StorageContainer container;
    private double maxInteractionDistance;
    private boolean openState = false;

    public SimpleStorageBlockEntity(BlockPos pos, ImmutableBlockState blockState) {
        super(BukkitBlockEntityTypes.SIMPLE_STORAGE, pos, blockState);
        this.behavior = super.blockState.behavior().getAs(SimpleStorageBlockBehavior.class).orElseThrow();
        BlockEntityHolder holder = new BlockEntityHolder(this);
        this.container = FastNMS.INSTANCE.createSimpleStorageContainer(holder, this.behavior.rows() * 9, this.behavior.canPlaceItem(), this.behavior.canTakeItem());
        holder.setInventory(FastNMS.INSTANCE.constructor$CraftInventory(this.container));
    }

    @Override
    protected void saveCustomData(CompoundTag tag) {
        // 保存前先把所有打开此容器的玩家界面关闭
        this.container.getViewers().forEach(HumanEntity::closeInventory);
        ListTag itemsTag = new ListTag();
        List<?> items = this.container.contents();
        for (Object itemStack : items) {
            if (FastNMS.INSTANCE.method$ItemStack$isEmpty(itemStack)) continue;
            if (VersionHelper.isOrAbove1_20_5()) {
                CoreReflections.instance$ItemStack$CODEC.encodeStart(MRegistryOps.SPARROW_NBT, itemStack)
                        .ifSuccess(success -> {
                            CompoundTag itemTag = (CompoundTag) success;
                            itemTag.putInt("slot", items.indexOf(itemStack));
                            itemsTag.add(itemTag);
                        })
                        .ifError(error -> CraftEngine.instance().logger().severe("Error while saving storage item: " + error));
            } else {
                Object nmsTag = FastNMS.INSTANCE.method$itemStack$save(itemStack, FastNMS.INSTANCE.constructor$CompoundTag());
                CompoundTag itemTag = (CompoundTag) MRegistryOps.NBT.convertTo(MRegistryOps.SPARROW_NBT, nmsTag);
                itemTag.putInt("slot", items.indexOf(itemStack));
                itemsTag.add(itemTag);
            }
        }
        tag.put("items", itemsTag);
    }

    @Override
    public void loadCustomData(CompoundTag tag) {
        ListTag itemsTag = Optional.ofNullable(tag.getList("items")).orElseGet(ListTag::new);
        for (int i = 0; i < itemsTag.size(); i++) {
            CompoundTag itemTag = itemsTag.getCompound(i);
            int slot = itemTag.getInt("slot");
            if (slot < 0 || slot >= this.behavior.rows() * 9) {
                continue;
            }
            if (VersionHelper.isOrAbove1_20_5()) {
                CoreReflections.instance$ItemStack$CODEC.parse(MRegistryOps.SPARROW_NBT, itemTag)
                        .resultOrPartial((s) -> CraftEngine.instance().logger().severe("Tried to load invalid item: '" + itemTag + "'. " + s))
                        .ifPresent(nmsStack -> this.container.setItemStack(slot, nmsStack));
            } else {
                Object nmsTag = MRegistryOps.SPARROW_NBT.convertTo(MRegistryOps.NBT, itemTag);
                Object itemStack = FastNMS.INSTANCE.method$ItemStack$of(nmsTag);
                this.container.setItemStack(slot, itemStack);
            }
        }
    }

    public StorageContainer container() {
        if (!isValid()) return null;
        return this.container;
    }

    public void onPlayerOpen(Player player) {
        if (!isValidContainer()) return;
        if (!player.isSpectatorMode()) {
            // 有非观察者的人，那么就不触发开启音效和事件
            if (!hasNoViewer(this.container.getViewers())) return;
            this.maxInteractionDistance = Math.max(player.getCachedInteractionRange(), this.maxInteractionDistance);
            this.setOpen(player);
            FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleBlockTick(super.world.world().serverWorld(), LocationUtils.toBlockPos(this.pos), BlockStateUtils.getBlockOwner(this.blockState.customBlockState().literalObject()), 5);
        }
    }

    public void onPlayerClose(Player player) {
        if (!isValidContainer()) return;
        if (!player.isSpectatorMode()) {
            // 有非观察者的人，那么就不触发关闭音效和事件
            for (HumanEntity viewer : this.container.getViewers()) {
                if (viewer.getGameMode() == GameMode.SPECTATOR || viewer == player.platformPlayer()) {
                    continue;
                }
                return;
            }
            this.maxInteractionDistance = 0;
            this.setClose(player);
        }
    }

    private void setOpen(@Nullable Player player) {
        this.updateOpenBlockState(true);
        org.bukkit.World bukkitWorld = (org.bukkit.World) super.world.world().platformWorld();
        if (player != null) {
            bukkitWorld.sendGameEvent((org.bukkit.entity.Player) player.platformPlayer(), GameEvent.CONTAINER_OPEN, new Vector(this.pos.x(), this.pos.y(), this.pos.z()));
        } else {
            bukkitWorld.sendGameEvent(null, GameEvent.CONTAINER_OPEN, new Vector(this.pos.x(), this.pos.y(), this.pos.z()));
        }
        this.openState = true;
        SoundData soundData = this.behavior.openSound();
        if (soundData != null) {
            super.world.world().playBlockSound(Vec3d.atCenterOf(this.pos), soundData);
        }
    }

    private void setClose(@Nullable Player player) {
        this.updateOpenBlockState(false);
        org.bukkit.World bukkitWorld = (org.bukkit.World) super.world.world().platformWorld();
        if (player != null) {
            bukkitWorld.sendGameEvent((org.bukkit.entity.Player) player.platformPlayer(), GameEvent.CONTAINER_CLOSE, new Vector(this.pos.x(), this.pos.y(), this.pos.z()));
        } else {
            bukkitWorld.sendGameEvent(null, GameEvent.CONTAINER_CLOSE, new Vector(this.pos.x(), this.pos.y(), this.pos.z()));
        }
        this.openState = false;
        SoundData soundData = this.behavior.closeSound();
        if (soundData != null) {
            super.world.world().playBlockSound(Vec3d.atCenterOf(this.pos), soundData);
        }
    }

    private boolean hasNoViewer(List<HumanEntity> viewers) {
        for (HumanEntity viewer : viewers) {
            if (viewer.getGameMode() != GameMode.SPECTATOR) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidContainer() {
        return this.isValid() && this.container != null && this.behavior != null;
    }

    public void updateOpenBlockState(boolean open) {
        ImmutableBlockState state = super.world.getBlockStateAtIfLoaded(this.pos);
        if (state == null || state.behavior() != this.behavior) return;
        Property<Boolean> property = this.behavior.openProperty();
        if (property == null) return;
        super.world.world().setBlockAt(this.pos.x(), this.pos.y(), this.pos.z(), state.with(property, open), UpdateOption.UPDATE_ALL.flags());
    }

    public void checkOpeners(Object level, Object pos, Object blockState) {
        if (!this.isValidContainer()) return;
        double maxInteractionDistance = 0d;
        List<HumanEntity> viewers = this.container.getViewers();
        int validViewers = 0;
        for (HumanEntity viewer : viewers) {
            if (viewer instanceof org.bukkit.entity.Player player) {
                maxInteractionDistance = Math.max(BukkitAdaptors.adapt(player).getCachedInteractionRange(), maxInteractionDistance);
                if (player.getGameMode() != GameMode.SPECTATOR) {
                    validViewers++;
                }
            }
        }
        boolean shouldOpen = validViewers != 0;
        if (shouldOpen && !this.openState) {
            this.setOpen(null);
        } else if (!shouldOpen && this.openState) {
            this.setClose(null);
        }

        this.maxInteractionDistance = maxInteractionDistance;
        if (!viewers.isEmpty()) {
            FastNMS.INSTANCE.method$ScheduledTickAccess$scheduleBlockTick(level, pos, BlockStateUtils.getBlockOwner(blockState), 5);
        }
    }

    @Override
    public void preRemove() {
        this.container.getViewers().forEach(HumanEntity::closeInventory);
        Vec3d pos = Vec3d.atCenterOf(this.pos);
        for (Object stack : this.container.contents()) {
            if (FastNMS.INSTANCE.method$ItemStack$isEmpty(stack)) continue;
            super.world.world().dropItemNaturally(pos, BukkitItemManager.instance().wrap(FastNMS.INSTANCE.method$CraftItemStack$asCraftMirror(stack)));

        }
        for (int i = 0; i < this.container.containerSize(); ++i) {
            this.container.setItemStack(i, CoreReflections.instance$ItemStack$EMPTY);
        }
    }
}
