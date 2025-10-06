package net.momirealms.craftengine.bukkit.plugin.reflection.minecraft;

import net.momirealms.craftengine.bukkit.nms.FastNMS;

public final class MSoundEvents {
    private MSoundEvents() {}

    public static final Object EMPTY = getById("intentionally_empty");
    public static final Object TRIDENT_RIPTIDE_1 = getById("item.trident_riptide_1");
    public static final Object TRIDENT_RIPTIDE_2 = getById("item.trident_riptide_2");
    public static final Object TRIDENT_RIPTIDE_3 = getById("item.trident.riptide_3");
    public static final Object TRIDENT_THROW = getById("item.trident.throw");
    public static final Object TOTEM_USE = getById("item.totem.use");

    private static Object getById(String id) {
        Object rl = FastNMS.INSTANCE.method$ResourceLocation$fromNamespaceAndPath("minecraft", id);
        return FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.SOUND_EVENT, rl);
    }
}
