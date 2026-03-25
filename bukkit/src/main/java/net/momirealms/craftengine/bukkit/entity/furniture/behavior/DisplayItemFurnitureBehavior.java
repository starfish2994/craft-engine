package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.momirealms.antigrieflib.Flag;
import net.momirealms.craftengine.bukkit.entity.data.ItemEntityData;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.bukkit.util.PacketUtils;
import net.momirealms.craftengine.core.entity.furniture.CustomFurniture;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehavior;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;
import net.momirealms.craftengine.core.entity.furniture.element.FurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfig;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBoxConfigs;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.CustomDataType;
import net.momirealms.craftengine.core.util.ItemUtils;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundAddEntityPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.network.protocol.game.ClientboundSetEntityDataPacketProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityTypeProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.Vec3Proxy;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.Tag;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;

public class DisplayItemFurnitureBehavior extends FurnitureBehavior {
    public static final FurnitureBehaviorFactory<DisplayItemFurnitureBehavior> FACTORY = new Factory();
    public static final CustomDataType<Item> DISPLAY_ITEM = new CustomDataType<>();
    public static final CustomDataType<Set<FurnitureHitBox>> TRACKED_HITBOXES = new CustomDataType<>();
    public static final CustomDataType<DisplayItemElement> DISPLAY_ELEMENT = new CustomDataType<>();
    private static final String DISPLAY_ITEM_TAG = "display_item";
    @NotNull
    private final Map<String, VariantRule> variantRules;
    @Nullable
    private final SoundData putSound;
    @Nullable
    private final SoundData takeSound;

    protected DisplayItemFurnitureBehavior(CustomFurniture furniture,
                                           @NotNull Map<String, VariantRule> variantRules,
                                           @Nullable SoundData putSound,
                                           @Nullable SoundData takeSound
    ) {
        super(furniture);
        this.variantRules = variantRules;
        this.putSound = putSound;
        this.takeSound = takeSound;
    }

    @Override
    public void onLoad(Furniture furniture) {
        CompoundTag displayItem = (CompoundTag) furniture.persistentData.getCustomData(DISPLAY_ITEM_TAG);
        if (displayItem != null) {
            int dataVersion = displayItem.getInt(DISPLAY_ITEM_TAG, Config.itemDataFixerUpperFallbackVersion());
            Item savedItem = ItemStackUtils.wrap(ItemStackUtils.parseMinecraftItem(displayItem, dataVersion));
            furniture.putTempData(DISPLAY_ITEM, savedItem);
        }
    }

    @Override
    public InteractionResult useOnFurniture(Furniture furniture, FurnitureHitBox hitBox, InteractEntityContext context) {
        // 如果配置了追踪碰撞箱, 则检查是不是追踪的碰撞箱, 如果没配置则全部碰撞箱都可以.
        Set<FurnitureHitBox> trackedHitboxes = furniture.getTempData(TRACKED_HITBOXES);
        if (trackedHitboxes != null && !trackedHitboxes.contains(hitBox)) {
            return InteractionResult.PASS;
        }
        // 检查区域保护权限
        Player player = context.getPlayer();
        WorldPosition pos = furniture.position();
        Location location = new Location((World) pos.world.platformWorld(), pos.x, pos.y, pos.z);
        if (!BukkitCraftEngine.instance().antiGriefProvider().test((org.bukkit.entity.Player) player.platformPlayer(), Flag.OPEN_CONTAINER, location)) {
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        // 如果当前不存在物品并且手中有物品, 则放入1个物品进去.
        Item displayItem = displayItem(furniture);
        Item itemInHand = context.getItem();
        if (ItemUtils.isEmpty(displayItem) && !ItemUtils.isEmpty(itemInHand)) {
            Item inputItem = itemInHand.copyWithCount(1);
            if (!player.canInstabuild()) {
                itemInHand.shrink(1);
            }
            this.handlePutDisplayItem(furniture, inputItem);
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        // 如果当前存在物品, 并且手中没有物品, 则取出物品到手中.
        else if (!ItemUtils.isEmpty(displayItem) && ItemUtils.isEmpty(itemInHand)) {
            player.setItemInHand(context.getHand(), displayItem);
            this.handleTakeDisplayItem(furniture);
            return InteractionResult.SUCCESS_AND_CANCEL;
        }
        return InteractionResult.PASS;
    }

    // 破坏家具时, 掉落存储的展示物品.
    @Override
    public void onDestroy(Furniture furniture) {
        Item displayItem = displayItem(furniture);
        DisplayItemElement displayItemElement = furniture.getTempData(DISPLAY_ELEMENT);
        if (!ItemUtils.isEmpty(displayItem) && displayItemElement != null) {
            furniture.world().dropItemNaturally(displayItemElement.position, displayItem);
        }
    }

    // 处理放入展示物品, 存储刷新并播放音效.
    private void handlePutDisplayItem(Furniture furniture, Item inputItem) {
        saveDisplayItem(furniture, inputItem);
        furniture.refreshElements();
        if (putSound != null) {
            furniture.world().playSound(furniture.position(), putSound.id(), putSound.volume().get(), putSound.pitch().get(), SoundSource.MASTER);
        }
    }

    // 处理取出展示物品逻辑, 存储刷新并播放音效.
    private void handleTakeDisplayItem(Furniture furniture) {
        saveDisplayItem(furniture, null);
        furniture.refreshElements();
        if (takeSound != null) {
            furniture.world().playSound(furniture.position(), takeSound.id(), takeSound.volume().get(), takeSound.pitch().get(), SoundSource.MASTER);
        }
    }

    // 根据当前家具变体查找对应的展示物品相对坐标
    @Override
    public void createFurnitureElements(Furniture furniture, Consumer<FurnitureElement> consumer) {
        VariantRule variantRule = variantRules.get(furniture.getCurrentVariant().name());
        if (variantRule != null) {
            DisplayItemElement displayItemElement = new DisplayItemElement(furniture, variantRule.itemRelative);
            furniture.putTempData(DISPLAY_ELEMENT, displayItemElement);
            consumer.accept(displayItemElement);
        }
    }

    // 根据当前家具变体查找对应的碰撞箱并创建
    @Override
    public void createFurnitureHitboxes(Furniture furniture, Consumer<FurnitureHitBox> consumer) {
        VariantRule variantRule = variantRules.get(furniture.getCurrentVariant().name());
        if (variantRule != null && !variantRule.hitBoxConfigs.isEmpty()) {
            HashSet<FurnitureHitBox> trackedHitBoxes = new HashSet<>();
            for (FurnitureHitBoxConfig<? extends FurnitureHitBox> hitBoxConfig : variantRule.hitBoxConfigs) {
                FurnitureHitBox furnitureHitBox = hitBoxConfig.create(furniture);
                trackedHitBoxes.add(furnitureHitBox);
                consumer.accept(furnitureHitBox);
            }
            furniture.putTempData(TRACKED_HITBOXES, trackedHitBoxes);
        }
    }

    // 获取存储的展示物品
    @NotNull
    private static Item displayItem(Furniture furniture) {
        Item displayItem = furniture.getTempData(DISPLAY_ITEM);
        if (ItemUtils.isEmpty(displayItem)) {
            return ItemStackUtils.wrap(null);
        }
        return displayItem;
    }

    // 设置存储的物品
    private void saveDisplayItem(Furniture furniture, @Nullable Item item) {
        if (item != null) {
            Tag itemStackAsTag = ItemStackUtils.saveMinecraftItemStackAsTag(item.getMinecraftItem());
            furniture.persistentData.addCustomData(DISPLAY_ITEM_TAG, itemStackAsTag);
            furniture.putTempData(DISPLAY_ITEM, item);
        } else {
            furniture.persistentData.removeCustomData(DISPLAY_ITEM_TAG);
            furniture.putTempData(DISPLAY_ITEM, ItemStackUtils.wrap(null));
        }
    }

    public static final class DisplayItemElement implements FurnitureElement {
        public final Furniture furniture;
        public final WorldPosition position;
        public final int vehicleId;
        public final int passengerId;
        public final Object despawnAllPacket;
        public final Object despawnVehiclePacket;
        public final Object despawnPassengerPacket;
        public final Object spawnVehiclePacket;
        public final Object spawnPassengerPacket;
        public final Object ridePacket;

        public DisplayItemElement(Furniture furniture, Vector3f relative) {
            this.furniture = furniture;
            WorldPosition furniturePos = furniture.position();
            Vec3d position = Furniture.getRelativePosition(furniturePos, relative);
            this.position = new WorldPosition(furniturePos.world, position.x, position.y, position.z, furniturePos.xRot, furniturePos.yRot);
            this.vehicleId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
            this.passengerId = EntityProxy.ENTITY_COUNTER.incrementAndGet();
            this.spawnVehiclePacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                    vehicleId, UUID.randomUUID(), position.x, position.y, position.z,
                    0, 0, EntityTypeProxy.ITEM_DISPLAY, 0, Vec3Proxy.ZERO, 0
            );
            this.spawnPassengerPacket = ClientboundAddEntityPacketProxy.INSTANCE.newInstance(
                    passengerId, UUID.randomUUID(), position.x, position.y, position.z,
                    0, 0, EntityTypeProxy.ITEM, 0, Vec3Proxy.ZERO, 0
            );
            this.ridePacket = PacketUtils.createClientboundSetPassengersPacket(vehicleId, passengerId);
            this.despawnAllPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(),
                    a -> {
                        a.add(vehicleId);
                        a.add(passengerId);
                    }
            ));
            this.despawnVehiclePacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(vehicleId)));
            this.despawnPassengerPacket = ClientboundRemoveEntitiesPacketProxy.INSTANCE.newInstance(MiscUtils.init(new IntArrayList(), a -> a.add(passengerId)));
        }

        @Override
        public void collectInteractableEntityId(Consumer<Integer> collector) {
        }

        @Override
        public void show(Player player) {
            player.sendPackets(List.of(
                    this.spawnVehiclePacket,
                    this.spawnPassengerPacket,
                    this.ridePacket,
                    ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.passengerId, new ArrayList<>() {{
                        ItemEntityData.Item.addEntityData(displayItem(furniture).getMinecraftItem(), this);
                    }})
            ), false);
        }

        @Override
        public void hide(Player player) {
            player.sendPacket(this.despawnAllPacket, false);
        }

        @Override
        public void refresh(Player player) {
            Object changeDisplayItemPacket = ClientboundSetEntityDataPacketProxy.INSTANCE.newInstance(this.passengerId, new ArrayList<>() {{
                ItemEntityData.Item.addEntityData(displayItem(furniture).getMinecraftItem(), this);
            }});
            player.sendPackets(List.of(
                    despawnPassengerPacket, spawnPassengerPacket, ridePacket, changeDisplayItemPacket
            ), false);
        }
    }

    private static class Factory implements FurnitureBehaviorFactory<DisplayItemFurnitureBehavior> {
        private static final String[] ITEM_POSITION = new String[] {"item_position", "item_position"};

        @Override
        public DisplayItemFurnitureBehavior create(CustomFurniture furniture, ConfigSection section) {
            // 如果没有配置变体展示规则
            ConfigSection variantsSection = section.getSection("variants");
            if (variantsSection == null) {
                return new DisplayItemFurnitureBehavior(furniture, Map.of(), null, null);
            }
            // 读取变体展示规则
            HashMap<String, VariantRule> variantRule = new HashMap<>();
            for (String variantName : variantsSection.keySet()) {
                ConfigSection variantSection = variantsSection.getSection(variantName);
                Vector3f itemRelative = variantSection.getVector3f(ITEM_POSITION, ConfigConstants.ZERO_VECTOR3);
                List<? extends FurnitureHitBoxConfig<? extends FurnitureHitBox>> hitboxes =
                        variantSection.getList("hitboxes", v -> FurnitureHitBoxConfigs.fromConfig(v.getAsSection()));
                variantRule.put(variantName, new VariantRule(itemRelative, hitboxes));
            }
            // 读取放入取出音效
            ConfigSection soundSection = section.getSection("sounds");
            SoundData inputSound = null;
            SoundData takeSound = null;
            if (soundSection != null) {
                inputSound = soundSection.getValue("put", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.RANGED_0_9_1));
                takeSound = soundSection.getValue("take", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.RANGED_0_9_1));
            }
            return new DisplayItemFurnitureBehavior(furniture, variantRule, inputSound, takeSound);
        }
    }

    // 变体展示规则
    public record VariantRule(
            Vector3f itemRelative,
            List<? extends FurnitureHitBoxConfig<? extends FurnitureHitBox>> hitBoxConfigs
    ) {}
}
