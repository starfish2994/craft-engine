package net.momirealms.craftengine.bukkit.block.entity.renderer.element;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElement;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.LazyReference;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ItemDisplayBlockEntityElementConfig implements BlockEntityElementConfig<ItemDisplayBlockEntityElement> {
    public static final Factory FACTORY = new Factory();
    private final LazyReference<List<Object>> lazyMetadataPacket;
    private final LazyReference<Item<?>> item;
    private final Vector3f scale;
    private final Vector3f position;
    private final Vector3f translation;
    private final float xRot;
    private final float yRot;
    private final Quaternionf rotation;
    private final ItemDisplayContext displayContext;
    private final Billboard billboard;

    public ItemDisplayBlockEntityElementConfig(LazyReference<Item<?>> item,
                                               Vector3f scale,
                                               Vector3f position,
                                               Vector3f translation,
                                               float xRot,
                                               float yRot,
                                               Quaternionf rotation,
                                               ItemDisplayContext displayContext,
                                               Billboard billboard) {
        this.item = item;
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.displayContext = displayContext;
        this.billboard = billboard;
        this.lazyMetadataPacket = LazyReference.lazyReference(() -> {
            List<Object> dataValues = new ArrayList<>();
            ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(item.get().getLiteralObject(), dataValues);
            ItemDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(this.scale, dataValues);
            ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(this.rotation, dataValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(this.billboard.id(), dataValues);
            ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(this.translation, dataValues);
            ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(this.displayContext.id(), dataValues);
            return dataValues;
        });
    }

    @Override
    public ItemDisplayBlockEntityElement create(BlockPos pos) {
        return new ItemDisplayBlockEntityElement(this, pos);
    }

    public Item<?> item() {
        return this.item.get();
    }

    public Vector3f scale() {
        return this.scale;
    }

    public Vector3f translation() {
        return this.translation;
    }

    public Vector3f position() {
        return this.position;
    }

    public float yRot() {
        return this.yRot;
    }

    public float xRot() {
        return this.xRot;
    }

    public Billboard billboard() {
        return billboard;
    }

    public ItemDisplayContext displayContext() {
        return displayContext;
    }

    public Quaternionf rotation() {
        return rotation;
    }

    public LazyReference<List<Object>> metadataValues() {
        return this.lazyMetadataPacket;
    }

    public static class Factory implements BlockEntityElementConfigFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <E extends BlockEntityElement> BlockEntityElementConfig<E> create(Map<String, Object> arguments) {
            // todo item should not be null
            Key itemId = Key.of(ResourceConfigUtils.requireNonEmptyStringOrThrow(arguments.get("item"), ""));
            return (BlockEntityElementConfig<E>) new ItemDisplayBlockEntityElementConfig(
                    LazyReference.lazyReference(() -> BukkitItemManager.instance().createWrappedItem(itemId, null)),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("scale", 1f), "scale"),
                    ResourceConfigUtils.getAsVector3f(arguments.getOrDefault("position", 0.5f), "position"),
                    ResourceConfigUtils.getAsVector3f(arguments.get("translation"), "translation"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("pitch", 0f), "pitch"),
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("yaw", 0f), "yaw"),
                    ResourceConfigUtils.getAsQuaternionf(arguments.getOrDefault("rotation", 0f), "rotation"),
                    ItemDisplayContext.valueOf(arguments.getOrDefault("display-context", "none").toString().toUpperCase(Locale.ROOT)),
                    Billboard.valueOf(arguments.getOrDefault("billboard", "fixed").toString().toUpperCase(Locale.ROOT))
            );
        }
    }
}
