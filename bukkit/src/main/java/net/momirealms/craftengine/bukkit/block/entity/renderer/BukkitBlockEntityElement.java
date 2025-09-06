package net.momirealms.craftengine.bukkit.block.entity.renderer;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.core.block.entity.render.BlockEntityElement;
import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.LazyReference;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BukkitBlockEntityElement implements BlockEntityElement {
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

    public BukkitBlockEntityElement(LazyReference<Item<?>> item,
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
    public Item<?> item() {
        return this.item.get();
    }

    @Override
    public Vector3f scale() {
        return this.scale;
    }

    @Override
    public Vector3f translation() {
        return this.translation;
    }

    @Override
    public Vector3f position() {
        return this.position;
    }

    @Override
    public float yRot() {
        return this.yRot;
    }

    @Override
    public float xRot() {
        return this.xRot;
    }

    @Override
    public Billboard billboard() {
        return billboard;
    }

    @Override
    public ItemDisplayContext displayContext() {
        return displayContext;
    }

    @Override
    public Quaternionf rotation() {
        return rotation;
    }

    @Override
    public LazyReference<List<Object>> metadataValues() {
        return this.lazyMetadataPacket;
    }
}
