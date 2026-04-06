package net.momirealms.craftengine.bukkit.block.entity.renderer.constant;

import com.google.common.base.Objects;
import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfig;
import net.momirealms.craftengine.core.block.entity.render.element.BlockEntityElementConfigFactory;
import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.config.ConfigValue;
import net.momirealms.craftengine.core.util.Color;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ItemDisplayBlockEntityElementConfig implements BlockEntityElementConfig<ItemDisplayBlockEntityElement> {
    public static final BlockEntityElementConfigFactory<ItemDisplayBlockEntityElement> FACTORY = new Factory();
    public final Function<Player, List<Object>> lazyMetadataPacket;
    public final Key itemId;
    public final Vector3f scale;
    public final Vector3f position;
    public final Vector3f translation;
    public final float xRot;
    public final float yRot;
    public final Quaternionf rotation;
    public final ItemDisplayContext displayContext;
    public final Billboard billboard;
    public final float shadowRadius;
    public final float shadowStrength;
    public final Color glowColor;
    public final int blockLight;
    public final int skyLight;
    public final float viewRange;

    public ItemDisplayBlockEntityElementConfig(Key itemId,
                                               Vector3f scale,
                                               Vector3f position,
                                               Vector3f translation,
                                               float xRot,
                                               float yRot,
                                               Quaternionf rotation,
                                               ItemDisplayContext displayContext,
                                               Billboard billboard,
                                               float shadowRadius,
                                               float shadowStrength,
                                               @Nullable Color glowColor,
                                               int blockLight,
                                               int skyLight,
                                               float viewRange) {
        this.itemId = itemId;
        this.scale = scale;
        this.position = position;
        this.translation = translation;
        this.xRot = xRot;
        this.yRot = yRot;
        this.rotation = rotation;
        this.displayContext = displayContext;
        this.billboard = billboard;
        this.shadowRadius = shadowRadius;
        this.shadowStrength = shadowStrength;
        this.glowColor = glowColor;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
        this.viewRange = viewRange;
        this.lazyMetadataPacket = player -> {
            List<Object> dataValues = new ArrayList<>();
            if (glowColor != null) {
                ItemDisplayEntityData.SharedFlags.addEntityData((byte) 0x40, dataValues);
                ItemDisplayEntityData.GlowColorOverride.addEntityData(glowColor.color(), dataValues);
            } else {
                ItemDisplayEntityData.SharedFlags.addEntityData((byte) 0x0, dataValues);
                ItemDisplayEntityData.GlowColorOverride.addEntityData(-1, dataValues);
            }
            Item wrappedItem = BukkitItemManager.instance().createWrappedItem(itemId, player);
            if (wrappedItem == null) {
                wrappedItem = java.util.Objects.requireNonNull(BukkitItemManager.instance().createWrappedItem(ItemKeys.BARRIER, player));
            }
            ItemDisplayEntityData.DisplayedItem.addEntityData(wrappedItem.minecraftItem(), dataValues);
            ItemDisplayEntityData.Scale.addEntityData(this.scale, dataValues);
            ItemDisplayEntityData.RotationLeft.addEntityData(this.rotation, dataValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityData(this.billboard.id(), dataValues);
            ItemDisplayEntityData.Translation.addEntityData(this.translation, dataValues);
            ItemDisplayEntityData.DisplayType.addEntityData(this.displayContext.id(), dataValues);
            ItemDisplayEntityData.ShadowRadius.addEntityData(this.shadowRadius, dataValues);
            ItemDisplayEntityData.ShadowStrength.addEntityData(this.shadowStrength, dataValues);
            if (this.blockLight != -1 && this.skyLight != -1) {
                ItemDisplayEntityData.BrightnessOverride.addEntityData(this.blockLight << 4 | this.skyLight << 20, dataValues);
            } else {
                ItemDisplayEntityData.BrightnessOverride.addEntityData(-1, dataValues);
            }
            ItemDisplayEntityData.ViewRange.addEntityData((float) (this.viewRange * player.displayEntityViewDistance()), dataValues);
            return dataValues;
        };
    }

    @Override
    public ItemDisplayBlockEntityElement create(World world, BlockPos pos) {
        return new ItemDisplayBlockEntityElement(this, pos);
    }

    @Override
    public ItemDisplayBlockEntityElement create(World world, BlockPos pos, ItemDisplayBlockEntityElement previous) {
        return new ItemDisplayBlockEntityElement(this, pos, previous.entityId,
                previous.config.yRot != this.yRot ||
                        previous.config.xRot != this.xRot ||
                        !previous.config.position.equals(this.position)
        );
    }

    @Override
    public ItemDisplayBlockEntityElement createExact(World world, BlockPos pos, ItemDisplayBlockEntityElement previous) {
        if (!previous.config.isSamePosition(this)) {
            return null;
        }
        return new ItemDisplayBlockEntityElement(this, pos, previous.entityId, false);
    }

    @Override
    public Class<ItemDisplayBlockEntityElement> elementClass() {
        return ItemDisplayBlockEntityElement.class;
    }

    public Color glowColor() {
        return this.glowColor;
    }

    public Key itemId() {
        return this.itemId;
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
        return this.billboard;
    }

    public ItemDisplayContext displayContext() {
        return this.displayContext;
    }

    public Quaternionf rotation() {
        return this.rotation;
    }

    public float shadowRadius() {
        return this.shadowRadius;
    }

    public float shadowStrength() {
        return this.shadowStrength;
    }

    public List<Object> metadataValues(Player player) {
        return this.lazyMetadataPacket.apply(player);
    }

    public boolean isSamePosition(ItemDisplayBlockEntityElementConfig that) {
        return Float.compare(xRot, that.xRot) == 0 &&
                Float.compare(yRot, that.yRot) == 0 &&
                Objects.equal(position, that.position) &&
                Objects.equal(translation, that.translation) &&
                Objects.equal(rotation, that.rotation);
    }

    private static class Factory implements BlockEntityElementConfigFactory<ItemDisplayBlockEntityElement> {
        private static final String[] DISPLAY_CONTEXT = new String[] {"display_context", "display_transform", "display-context", "display-transform"};
        private static final String[] SHADOW_RADIUS = new String[] {"shadow_radius", "shadow-radius"};
        private static final String[] SHADOW_STRENGTH = new String[] {"shadow_strength", "shadow-strength"};
        private static final String[] GLOW_COLOR = new String[] {"glow_color", "glow-color"};
        private static final String[] BLOCK_LIGHT = new String[] {"block_light", "block-light"};
        private static final String[] SKY_LIGHT = new String[] {"sky_light", "sky-light"};
        private static final String[] VIEW_RANGE = new String[] {"view_range", "view-range"};

        @Override
        public ItemDisplayBlockEntityElementConfig create(ConfigSection section) {
            ConfigSection brightness = section.getSection("brightness");
            return new ItemDisplayBlockEntityElementConfig(
                    section.getNonNullIdentifier("item"),
                    section.getVector3f("scale", ConfigConstants.NORMAL_SCALE),
                    section.getVector3f("position", ConfigConstants.CENTER_VECTOR3),
                    section.getVector3f("translation", ConfigConstants.ZERO_VECTOR3),
                    section.getFloat("pitch", 0f),
                    section.getFloat("yaw", 0f),
                    section.getQuaternion("rotation", ConfigConstants.ZERO_QUATERNION),
                    section.getEnum(DISPLAY_CONTEXT, ItemDisplayContext.class, ItemDisplayContext.NONE),
                    section.getEnum("billboard", Billboard.class, Billboard.FIXED),
                    section.getFloat(SHADOW_RADIUS),
                    section.getFloat(SHADOW_STRENGTH, 1f),
                    section.getValue(GLOW_COLOR, ConfigValue::getAsColor),
                    brightness != null ? brightness.getInt(BLOCK_LIGHT, -1) : -1,
                    brightness != null ? brightness.getInt(SKY_LIGHT, -1) : -1,
                    section.getFloat(VIEW_RANGE, 1f)
            );
        }
    }
}
