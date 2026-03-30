package net.momirealms.craftengine.bukkit.entity.furniture.behavior;

import net.momirealms.antigrieflib.Flag;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.entity.furniture.FurnitureInventoryHolder;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.gui.BukkitInventory;
import net.momirealms.craftengine.bukkit.util.ItemStackUtils;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.furniture.FurnitureDefinition;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorFactory;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureBehaviorTemplate;
import net.momirealms.craftengine.core.entity.furniture.behavior.FurnitureController;
import net.momirealms.craftengine.core.entity.furniture.hitbox.FurnitureHitBox;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.sound.SoundSource;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.sparrow.nbt.CompoundTag;
import net.momirealms.sparrow.nbt.ListTag;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class SimpleStorageFurnitureBehaviorTemplate extends FurnitureBehaviorTemplate {
    public static final FurnitureBehaviorFactory<SimpleStorageFurnitureBehaviorTemplate> FACTORY = new Factory();
    public final String containerTitle;
    public final int rows;
    public final SoundData openSound;
    public final SoundData closeSound;

    private SimpleStorageFurnitureBehaviorTemplate(FurnitureDefinition furniture,
                                                   String containerTitle,
                                                   int rows,
                                                   SoundData openSound,
                                                   SoundData closeSound) {
        super(furniture);
        this.containerTitle = containerTitle;
        this.rows = rows;
        this.openSound = openSound;
        this.closeSound = closeSound;
    }

    @Override
    public FurnitureController createController(Furniture furniture) {
        return new SimpleStorageFurnitureController(furniture, this);
    }

    private static class Factory implements FurnitureBehaviorFactory<SimpleStorageFurnitureBehaviorTemplate> {

        @SuppressWarnings("DuplicatedCode")
        @Override
        public SimpleStorageFurnitureBehaviorTemplate create(FurnitureDefinition furniture, ConfigSection section) {
            ConfigSection soundSection = section.getSection("sounds");
            SoundData openSound = null;
            SoundData closeSound = null;
            if (soundSection != null) {
                openSound = soundSection.getValue("open", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.RANGED_0_9_1));
                closeSound = soundSection.getValue("close", v -> SoundData.fromConfig(v, SoundData.SoundValue.FIXED_0_5, SoundData.SoundValue.RANGED_0_9_1));
            }
            return new SimpleStorageFurnitureBehaviorTemplate(
                    furniture,
                    section.getString("title", "<lang:container.chest>"),
                    section.getInt("rows", 1),
                    openSound,
                    closeSound
            );
        }
    }

    public static final class SimpleStorageFurnitureController extends FurnitureController implements FurnitureInventoryHolder {
        private static final String KEY = "craftengine:simple_storage_furniture";
        public final Furniture furniture;
        private final SimpleStorageFurnitureBehaviorTemplate template;
        private final Inventory inventory;

        private SimpleStorageFurnitureController(Furniture furniture, SimpleStorageFurnitureBehaviorTemplate template) {
            super(furniture);
            this.furniture = furniture;
            this.template = template;
            this.inventory = FastNMS.INSTANCE.createSimpleStorageContainer(this, this.template.rows * 9, false, false);
        }

        @Override
        public Furniture furniture() {
            return this.furniture;
        }

        @Override
        public InteractionResult useOnFurniture(FurnitureHitBox hitBox, InteractEntityContext context) {
            BlockPos blockPos = context.getClickedPos();
            World bukkitWorld = (World) context.getLevel().platformWorld();
            Location location = new Location(bukkitWorld, blockPos.x(), blockPos.y(), blockPos.z());
            Player player = context.getPlayer();
            if (!BukkitCraftEngine.instance().antiGriefProvider().test((org.bukkit.entity.Player) player.platformPlayer(), Flag.OPEN_CONTAINER, location)) {
                return InteractionResult.FAIL;
            }
            this.onOpen(player);
            return InteractionResult.SUCCESS_AND_CANCEL;
        }

        @Override
        public void onLoad() {
            this.inventory.close();
            CompoundTag data = this.furniture.persistentData.getCustomData(KEY) instanceof CompoundTag tag ? tag : new CompoundTag();
            int dataVersion = data.getInt("data_version", Config.itemDataFixerUpperFallbackVersion());
            ListTag items = Optional.ofNullable(data.getList("items")).orElseGet(ListTag::new);
            this.inventory.setStorageContents(ItemStackUtils.parseBukkitItems(items, this.template.rows * 9, dataVersion));
        }

        @Override
        public void onUnload() {
            this.inventory.close();
            CompoundTag data = this.furniture.persistentData.getCustomData(KEY) instanceof CompoundTag tag ? tag : new CompoundTag();
            data.put("items", ItemStackUtils.saveBukkitItemsAsListTag(this.inventory.getStorageContents()));
            data.putInt("data_version", VersionHelper.WORLD_VERSION);
            this.furniture.persistentData.addCustomData(KEY, data);
        }

        @Override
        public void onDestroy(Player player) {
            this.inventory.close();
            this.furniture.persistentData.removeCustomData(KEY);
            for (ItemStack stack : this.inventory.getContents()) {
                if (stack != null) {
                    this.furniture.world().dropItemNaturally(this.furniture.position(), BukkitAdaptor.adapt(stack));
                }
            }
            this.inventory.clear();
        }

        @Override
        public void onOpen(Player player) {
            if (!player.isSpectatorMode()) {
                for (HumanEntity viewer : this.inventory.getViewers()) {
                    if (viewer.getGameMode() != GameMode.SPECTATOR) return;
                }
                SoundData sound = this.template.openSound;
                if (sound != null) {
                    this.furniture.world().playSound(this.furniture.position(), sound.id(), sound.volume().get(), sound.pitch().get(), SoundSource.MASTER);
                }
            }
            new BukkitInventory(this.inventory).open(player, AdventureHelper.miniMessage().deserialize(this.template.containerTitle, PlayerOptionalContext.of(player).tagResolvers()));
        }

        @Override
        public void onClose(Player player) {
            if (!player.isSpectatorMode()) {
                for (HumanEntity viewer : this.inventory.getViewers()) {
                    if (viewer.getGameMode() == GameMode.SPECTATOR || viewer == player.platformPlayer()) continue;
                    return;
                }
                SoundData sound = this.template.closeSound;
                if (sound != null) {
                    this.furniture.world().playSound(this.furniture.position(), sound.id(), sound.volume().get(), sound.pitch().get(), SoundSource.MASTER);
                }
            }
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }
    }
}
