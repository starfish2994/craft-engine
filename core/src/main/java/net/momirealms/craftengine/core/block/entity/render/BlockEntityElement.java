package net.momirealms.craftengine.core.block.entity.render;

import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.LazyReference;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public interface BlockEntityElement {
    Item<?> item();

    Vector3f scale();

    Vector3f translation();

    Vector3f position();

    float yRot();

    float xRot();

    Billboard billboard();

    ItemDisplayContext displayContext();

    Quaternionf rotation();

    LazyReference<List<Object>> metadataValues();
}