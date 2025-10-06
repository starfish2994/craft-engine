package net.momirealms.craftengine.bukkit.plugin.reflection.minecraft;

import net.momirealms.craftengine.bukkit.nms.FastNMS;

import java.util.Objects;

public final class MTagKeys {
    private MTagKeys() {}

    public static final Object Item$WOOL = create(MRegistries.ITEM, "wool");
    public static final Object Block$WALLS = create(MRegistries.BLOCK, "walls");
    public static final Object Block$SHULKER_BOXES = create(MRegistries.BLOCK, "shulker_boxes");
    public static final Object Block$FENCES = create(MRegistries.BLOCK, "fences");
    public static final Object Block$WOODEN_FENCES = create(MRegistries.BLOCK, "wooden_fences");
    public static final Object Block$DIRT = create(MRegistries.BLOCK, "dirt");

    private static Object create(Object registry, String location) {
        Object resourceLocation = FastNMS.INSTANCE.method$ResourceLocation$fromNamespaceAndPath("minecraft", location);
        Object tagKey = FastNMS.INSTANCE.method$TagKey$create(registry, resourceLocation);
        return Objects.requireNonNull(tagKey);
    }
}
