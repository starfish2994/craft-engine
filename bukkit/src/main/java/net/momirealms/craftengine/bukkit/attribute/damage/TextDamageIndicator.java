package net.momirealms.craftengine.bukkit.attribute.damage;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.core.attribute.damage.DamageIndicator;
import net.momirealms.craftengine.core.attribute.damage.DamageIndicatorFactory;
import net.momirealms.craftengine.core.attribute.damage.ViewPointSelector;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.CommonConditions;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.AABBProxy;

import java.util.List;
import java.util.function.Predicate;

public final class TextDamageIndicator implements DamageIndicator {
    private static final String[] ANGLE_SPREAD = ConfigKeys.of("angle_spread");
    private static final String[] HEIGHT_SPREAD = ConfigKeys.of("height_spread");
    private static final String[] SPAWN_SCALE = ConfigKeys.of("spawn_scale");
    private static final String[] POP_SCALE = ConfigKeys.of("pop_scale");
    private static final String[] SETTLE_SCALE = ConfigKeys.of("settle_scale");
    private static final String[] POP_DELAY = ConfigKeys.of("pop_delay");
    private static final String[] SETTLE_DELAY = ConfigKeys.of("settle_delay");
    private static final String[] SHRINK_DELAY = ConfigKeys.of("shrink_delay");
    private static final String[] REMOVE_DELAY = ConfigKeys.of("remove_delay");
    public static final DamageIndicatorFactory<TextDamageIndicator> FACTORY = TextDamageIndicator::new;
    private final String text;
    private final double angleSpread;
    private final double heightSpread;
    private final DamageHologram.Animation animation;
    private final Predicate<Context> conditions;

    public TextDamageIndicator(ConfigSection args) {
        this.text = args.getString("text", "<white><arg:damage></white>");
        this.conditions = MiscUtils.allOf(args.getList(ConfigKeys.of("condition(s)"), CommonConditions::fromConfig));
        ConfigSection position = args.getSection("position");
        this.angleSpread = position != null ? position.getDouble(ANGLE_SPREAD, 30d) : 30d;
        this.heightSpread = position != null ? position.getDouble(HEIGHT_SPREAD, 0.15d) : 0.15d;
        ConfigSection animation = args.getSection("animation");
        this.animation = new DamageHologram.Animation(
                animation != null ? animation.getFloat(SPAWN_SCALE, 0.1f) : 0.1f,
                animation != null ? animation.getFloat(POP_SCALE, 1.25f) : 1.25f,
                animation != null ? animation.getFloat(SETTLE_SCALE, 1f) : 1f,
                animation != null ? animation.getLong(POP_DELAY, 1L) : 1L,
                animation != null ? animation.getLong(SETTLE_DELAY, 5L) : 5L,
                animation != null ? animation.getLong(SHRINK_DELAY, 16L) : 16L,
                animation != null ? animation.getLong(REMOVE_DELAY, 19L) : 19L
        );
    }

    @Override
    public void display(Player attacker, Entity victim, List<Player> viewers, Context context) {
        if (!this.conditions.test(context)) return;
        Component text = AdventureHelper.deserialize(this.text, context);
        Object aabb = EntityProxy.INSTANCE.getBoundingBox(victim.minecraftEntity());
        double width = AABBProxy.INSTANCE.getMaxX(aabb) - AABBProxy.INSTANCE.getMinX(aabb);
        double depth = AABBProxy.INSTANCE.getMaxZ(aabb) - AABBProxy.INSTANCE.getMinZ(aabb);
        double radius = MiscUtils.sqrt((float) (width * width + depth * depth)) / 2;
        Vec3d eye = attacker.getEyePos();
        float yawRad = MiscUtils.toRadians(attacker.yRot());
        float pitchRad = MiscUtils.toRadians(attacker.xRot());
        ViewPointSelector.Point3D point = ViewPointSelector.findViewIntersection(
                new ViewPointSelector.Point3D(eye.x, eye.y, eye.z),
                new ViewPointSelector.Point3D(
                        -MiscUtils.sin(yawRad) * MiscUtils.cos(pitchRad),
                        -MiscUtils.sin(pitchRad),
                        MiscUtils.cos(yawRad) * MiscUtils.cos(pitchRad)
                ),
                new ViewPointSelector.Point3D(
                        (AABBProxy.INSTANCE.getMinX(aabb) + AABBProxy.INSTANCE.getMaxX(aabb)) / 2,
                        AABBProxy.INSTANCE.getMinY(aabb),
                        (AABBProxy.INSTANCE.getMinZ(aabb) + AABBProxy.INSTANCE.getMaxZ(aabb)) / 2
                ),
                radius,
                AABBProxy.INSTANCE.getMaxY(aabb) - AABBProxy.INSTANCE.getMinY(aabb),
                this.angleSpread,
                this.heightSpread
        );
        new DamageHologram(viewers, text, this.animation, point.x(), point.y(), point.z()).start();
    }
}
