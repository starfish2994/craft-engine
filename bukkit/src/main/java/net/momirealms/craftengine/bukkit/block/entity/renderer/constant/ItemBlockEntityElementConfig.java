package net.momirealms.craftengine.bukkit.block.entity.renderer.constant;

import net.momirealms.craftengine.bukkit.entity.data.ItemEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class ItemBlockEntityElementConfig implements BlockEntityElementConfig<ItemBlockEntityElement> {
    public static final BlockEntityElementConfigFactory<ItemBlockEntityElement> FACTORY = new Factory();
    public final Function<Player, List<Object>> lazyMetadataPacket;
    public final Key itemId;
    public final Vector3f position;

    public ItemBlockEntityElementConfig(Key itemId, Vector3f position) {
        this.itemId = itemId;
        this.position = position;
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            Item wrappedItem = BukkitItemManager.instance().createWrappedItem(itemId, player);
            if (wrappedItem == null) {
                wrappedItem = Objects.requireNonNull(BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, player));
            }
            ItemEntityData.Item.addEntityData(wrappedItem.minecraftItem(), dataValues);
            ItemEntityData.NoGravity.addEntityData(true, dataValues);
            return dataValues;
        };
    }

    @Override
    public ItemBlockEntityElement create(World world, BlockPos pos) {
        return new ItemBlockEntityElement(this, pos);
    }

    @Override
    public ItemBlockEntityElement create(World world, BlockPos pos, ItemBlockEntityElement previous) {
        return new ItemBlockEntityElement(this, pos, previous.entityId1, previous.entityId2, !previous.config.position.equals(this.position));
    }

    @Override
    public ItemBlockEntityElement createExact(World world, BlockPos pos, ItemBlockEntityElement previous) {
        if (!previous.config.isSamePosition(this)) {
            return null;
        }
        return new ItemBlockEntityElement(this, pos, previous.entityId1, previous.entityId2, false);
    }

    @Override
    public Class<ItemBlockEntityElement> elementClass() {
        return ItemBlockEntityElement.class;
    }

    public Vector3f position() {
        return this.position;
    }

    public Key itemId() {
        return this.itemId;
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    public boolean isSamePosition(ItemBlockEntityElementConfig that) {
        return this.position.equals(that.position);
    }

    private static class Factory implements BlockEntityElementConfigFactory<ItemBlockEntityElement> {

        @Override
        public ItemBlockEntityElementConfig create(ConfigSection section) {
            return new ItemBlockEntityElementConfig(
                    section.getNonNullIdentifier("item"),
                    section.getVector3f("position", ConfigConstants.CENTER_VECTOR3)
            );
        }
    }
}
