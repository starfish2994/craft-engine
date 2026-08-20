package net.momirealms.craftengine.core.entity.projectile;

import net.momirealms.craftengine.core.plugin.config.ConfigKeys;
import net.momirealms.craftengine.core.plugin.config.ConfigSection;
import net.momirealms.craftengine.core.util.Tristate;

public record ProjectileMeta(ProjectileDisplay display,
                             Tristate gravity,
                             boolean ignoreInfinityEnchantment,
                             boolean removeOnHit,
                             boolean pickupable,
                             float velocity,
                             double damage,
                             int pierceLevel,
                             ProjectileSounds sounds) {
    private static final String[] IGNORE_INFINITY_ENCHANTMENT = ConfigKeys.of("ignore_infinity_enchantment");
    private static final String[] REMOVE_ON_HIT = ConfigKeys.of("remove_on_hit");
    private static final String[] PIERCE_LEVEL = ConfigKeys.of("pierce_level");

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
                section.getDouble("damage", -1),
                section.getInt(PIERCE_LEVEL, -1),
                sounds
        );
    }
}
