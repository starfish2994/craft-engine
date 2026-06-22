package net.momirealms.craftengine.core.block.setting;

import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public final class DestroyStageDisplay {
    private static final String[] DISPLAY_CONTEXT = new String[]{"display_context", "display_transform", "display-context", "display-transform"};
    private static final String[] VIEW_RANGE = new String[]{"view_range", "view-range"};
    private static final String[] BLOCK_LIGHT = new String[]{"block_light", "block-light"};
    private static final String[] SKY_LIGHT = new String[]{"sky_light", "sky-light"};

    private final List<Key> items;
    private final Vector3f position;
    private final Vector3f translation;
    private final Vector3f scale;
    private final float pitch;
    private final float yaw;
    private final Quaternionf rotation;
    private final ItemDisplayContext displayContext;
    private final Billboard billboard;
    private final float viewRange;
    private final int blockLight;
    private final int skyLight;

    private DestroyStageDisplay(List<Key> items,
                                Vector3f position,
                                Vector3f translation,
                                Vector3f scale,
                                float pitch,
                                float yaw,
                                Quaternionf rotation,
                                ItemDisplayContext displayContext,
                                Billboard billboard,
                                float viewRange,
                                int blockLight,
                                int skyLight) {
        this.items = items;
        this.position = position;
        this.translation = translation;
        this.scale = scale;
        this.pitch = pitch;
        this.yaw = yaw;
        this.rotation = rotation;
        this.displayContext = displayContext;
        this.billboard = billboard;
        this.viewRange = viewRange;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
    }

    public static DestroyStageDisplay fromConfig(ConfigSection section) {
        List<Key> items = section.getValue("items", v -> v.getAsList(ConfigValue::getAsIdentifier));
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("destroy_stage_display requires a non-empty 'items' list");
        }
        ConfigSection brightness = section.getSection("brightness");
        return new DestroyStageDisplay(
                List.copyOf(items),
                section.getVector3f("position", ConfigConstants.CENTER_VECTOR3),
                section.getVector3f("translation", ConfigConstants.ZERO_VECTOR3),
                section.getVector3f("scale", ConfigConstants.NORMAL_SCALE),
                section.getFloat("pitch", 0f),
                section.getFloat("yaw", 0f),
                section.getQuaternion("rotation", ConfigConstants.ZERO_QUATERNION),
                section.getEnum(DISPLAY_CONTEXT, ItemDisplayContext.class, ItemDisplayContext.NONE),
                section.getEnum("billboard", Billboard.class, Billboard.FIXED),
                section.getFloat(VIEW_RANGE, 1f),
                brightness != null ? brightness.getInt(BLOCK_LIGHT, -1) : -1,
                brightness != null ? brightness.getInt(SKY_LIGHT, -1) : -1
        );
    }

    public int itemIndexForProgress(float progress) {
        if (progress < 0 || this.items.isEmpty()) return -1;
        int index = (int) (progress * this.items.size());
        if (index >= this.items.size()) index = this.items.size() - 1;
        return index;
    }

    @Nullable
    public Key itemForProgress(float progress) {
        int index = itemIndexForProgress(progress);
        return index < 0 ? null : this.items.get(index);
    }

    public List<Key> items() {
        return this.items;
    }

    public Vector3f position() {
        return this.position;
    }

    public Vector3f translation() {
        return this.translation;
    }

    public Vector3f scale() {
        return this.scale;
    }

    public float pitch() {
        return this.pitch;
    }

    public float yaw() {
        return this.yaw;
    }

    public Quaternionf rotation() {
        return this.rotation;
    }

    public ItemDisplayContext displayContext() {
        return this.displayContext;
    }

    public Billboard billboard() {
        return this.billboard;
    }

    public float viewRange() {
        return this.viewRange;
    }

    public int blockLight() {
        return this.blockLight;
    }

    public int skyLight() {
        return this.skyLight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DestroyStageDisplay that)) return false;
        return Objects.equals(this.items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.items);
    }
}
