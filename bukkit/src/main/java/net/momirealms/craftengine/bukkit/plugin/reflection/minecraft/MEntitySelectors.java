package net.momirealms.craftengine.bukkit.plugin.reflection.minecraft;

import net.momirealms.craftengine.bukkit.nms.FastNMS;

import java.util.function.Predicate;

public final class MEntitySelectors {
    private MEntitySelectors() {}

    public static final Predicate<Object> NO_SPECTATORS = entity -> !FastNMS.INSTANCE.method$Entity$isSpectator(entity);

}
