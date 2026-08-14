package net.momirealms.craftengine.bukkit.entity.hologram;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.hologram.DamageIndicator;
import net.momirealms.craftengine.core.entity.hologram.DamageIndicatorFactory;
import net.momirealms.craftengine.core.entity.hologram.ViewPointSelector;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.ContextKey;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.util.AdventureHelper;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.proxy.minecraft.world.entity.EntityProxy;
import net.momirealms.craftengine.proxy.minecraft.world.phys.AABBProxy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public final class TextDamageIndicator implements DamageIndicator {
    public static final DamageIndicatorFactory<TextDamageIndicator> FACTORY = TextDamageIndicator::new;
    private static final String[] NUMBER_FORMAT = {"number-format", "number_format"};
    private static final String[] ANGLE_SPREAD = {"angle-spread", "angle_spread"};
    private static final String[] HEIGHT_SPREAD = {"height-spread", "height_spread"};
    private static final String[] SPAWN_SCALE = {"spawn-scale", "spawn_scale"};
    private static final String[] POP_SCALE = {"pop-scale", "pop_scale"};
    private static final String[] SETTLE_SCALE = {"settle-scale", "settle_scale"};
    private static final String[] POP_DELAY = {"pop-delay", "pop_delay"};
    private static final String[] SETTLE_DELAY = {"settle-delay", "settle_delay"};
    private static final String[] SHRINK_DELAY = {"shrink-delay", "shrink_delay"};
    private static final String[] REMOVE_DELAY = {"remove-delay", "remove_delay"};

    private final String numberFormat;
    private final String text;
    private final double angleSpread;
    private final double heightSpread;
    private final DamageHologram.Animation animation;

    public TextDamageIndicator(ConfigSection args) {
        this.numberFormat = args.getString(NUMBER_FORMAT, "#.#");
        this.text = args.getString("text", "<white><arg:damage></white>");
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
    public void display(Player attacker, Entity victim, double damage, List<Player> viewers) {
        if (!(attacker instanceof BukkitServerPlayer attackerUser) || !(victim instanceof BukkitEntity victimEntity)) return;
        List<BukkitServerPlayer> serverPlayers = new ArrayList<>(viewers.size());
        for (Player viewer : viewers) {
            if (viewer instanceof BukkitServerPlayer serverPlayer) {
                serverPlayers.add(serverPlayer);
            }
        }
        if (serverPlayers.isEmpty()) return;
        String damageText = new DecimalFormat(this.numberFormat).format(damage);
        PlayerOptionalContext context = PlayerOptionalContext.of(attackerUser, ContextHolder.builder()
                .withParameter(ContextKey.direct("damage"), damageText));
        Component text = AdventureHelper.miniMessage().deserialize(this.text, context.tagResolvers());
        Object aabb = EntityProxy.INSTANCE.getBoundingBox(victimEntity.minecraftEntity());
        double width = AABBProxy.INSTANCE.getMaxX(aabb) - AABBProxy.INSTANCE.getMinX(aabb);
        double depth = AABBProxy.INSTANCE.getMaxZ(aabb) - AABBProxy.INSTANCE.getMinZ(aabb);
        double radius = MiscUtils.sqrt((float) (width * width + depth * depth)) / 2;
        Vec3d eye = attackerUser.getEyePos();
        float yawRad = MiscUtils.toRadians(attackerUser.yRot());
        float pitchRad = MiscUtils.toRadians(attackerUser.xRot());
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
        new DamageHologram(serverPlayers, text, this.animation, point.x(), point.y(), point.z()).start();
    }
}
