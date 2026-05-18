package net.momirealms.craftengine.core.entity.projectile;

import net.momirealms.craftengine.core.entity.display.Billboard;
import net.momirealms.craftengine.core.entity.display.ItemDisplayContext;
import net.momirealms.craftengine.core.plugin.config.ConfigConstants;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.Tristate;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record ProjectileMeta(ProjectileDisplay display,
                             Tristate gravity,
                             boolean ignoreInfinityEnchantment,
                             boolean removeOnHit,
                             boolean pickupable,
                             float velocity,
                             ProjectileSounds sounds) {


    private static final String[] IGNORE_INFINITY_ENCHANTMENT = new String[] {"ignore_infinity_enchantment", "ignore-infinity-enchantment"};
    private static final String[] REMOVE_ON_HIT = new String[] {"remove_on_hit", "remove-on-hit"};

    public static ProjectileMeta fromConfig(ConfigSection section) {
        ConfigSection soundsSection = section.getSection("sounds");
        ProjectileSounds sounds = null;
        if (soundsSection != null) {
            sounds = ProjectileSounds.fromConfig(soundsSection);
        }
        ProjectileDisplay display = null;
        ConfigSection displaySection = section.getSection("display");
        if (displaySection != null) {
            display = ProjectileDisplay.fromConfig(displaySection);
        } else if (section.containsKey("item")) {
            display = ProjectileDisplay.fromConfig(section);
        }
        return new ProjectileMeta(
                display,
                section.getEnum("gravity", Tristate.class, Tristate.UNDEFINED),
                section.getBoolean(IGNORE_INFINITY_ENCHANTMENT, false),
                section.getBoolean(REMOVE_ON_HIT, false),
                section.getBoolean("pickupable", true),
                section.getFloat("velocity", 1f),
                sounds
        );
    }
}
