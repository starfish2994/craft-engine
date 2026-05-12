package net.momirealms.craftengine.bukkit.entity.data;

import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.proxy.minecraft.network.syncher.EntityDataSerializersProxy;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DisplayEntityData<T> extends BaseEntityData<T> {
    // Display only
    public static final DisplayEntityData<Integer> InterpolationDelay = of(DisplayEntityData.class, EntityDataSerializersProxy.INT, 0, true);

    // 1.19.4-1.20.1
    public static final DisplayEntityData<Integer> InterpolationDuration = of(DisplayEntityData.class, EntityDataSerializersProxy.INT, 0, !VersionHelper.isOrAbove1_20_2());

    // 1.20.2+
    public static final DisplayEntityData<Integer> TransformationInterpolationDuration = of(DisplayEntityData.class, EntityDataSerializersProxy.INT, 0, VersionHelper.isOrAbove1_20_2());
    public static final DisplayEntityData<Integer> PositionRotationInterpolationDuration = of(DisplayEntityData.class, EntityDataSerializersProxy.INT, 0, VersionHelper.isOrAbove1_20_2());

    public static final DisplayEntityData<Vector3f> Translation = of(DisplayEntityData.class, EntityDataSerializersProxy.VECTOR3, new Vector3f(0f), true);
    public static final DisplayEntityData<Vector3f> Scale = of(DisplayEntityData.class, EntityDataSerializersProxy.VECTOR3, new Vector3f(1f), true);
    public static final DisplayEntityData<Quaternionf> RotationLeft = of(DisplayEntityData.class, EntityDataSerializersProxy.QUATERNION, new Quaternionf(0f, 0f, 0f, 1f), true);
    public static final DisplayEntityData<Quaternionf> RotationRight = of(DisplayEntityData.class, EntityDataSerializersProxy.QUATERNION, new Quaternionf(0f, 0f, 0f, 1f), true);
    /**
     * 	Billboard Constraints (0 = FIXED, 1 = VERTICAL, 2 = HORIZONTAL, 3 = CENTER)
     */
    public static final DisplayEntityData<Byte> BillboardConstraints = of(DisplayEntityData.class, EntityDataSerializersProxy.BYTE, (byte) 0, true);
    /**
     * Brightness override (blockLight << 4 | skyLight << 20)
     */
    public static final DisplayEntityData<Integer> BrightnessOverride = of(DisplayEntityData.class, EntityDataSerializersProxy.INT, -1, true);
    public static final DisplayEntityData<Float> ViewRange = of(DisplayEntityData.class, EntityDataSerializersProxy.FLOAT, 1f, true);
    public static final DisplayEntityData<Float> ShadowRadius = of(DisplayEntityData.class, EntityDataSerializersProxy.FLOAT, 0f, true);
    public static final DisplayEntityData<Float> ShadowStrength = of(DisplayEntityData.class, EntityDataSerializersProxy.FLOAT, 0f, true);
    public static final DisplayEntityData<Float> Width = of(DisplayEntityData.class, EntityDataSerializersProxy.FLOAT, 0f, true);
    public static final DisplayEntityData<Float> Height = of(DisplayEntityData.class, EntityDataSerializersProxy.FLOAT, 0f, true);
    public static final DisplayEntityData<Integer> GlowColorOverride = of(DisplayEntityData.class, EntityDataSerializersProxy.INT, -1, true);

    public static <T> DisplayEntityData<T> of(final Class<?> clazz, final Object serializer, T defaultValue, boolean condition) {
        if (!condition) return null;
        return new DisplayEntityData<>(clazz, serializer, defaultValue);
    }

    public DisplayEntityData(Class<?> clazz, Object serializer, T defaultValue) {
        super(clazz, serializer, defaultValue);
    }
}
