package net.momirealms.craftengine.bukkit.plugin.reflection.minecraft;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.util.VersionHelper;

public final class MBlocks {
    private MBlocks() {}

    public static final Object AIR = getById("air");
    public static final Object AIR$defaultState = FastNMS.INSTANCE.method$Block$defaultState(AIR);
    public static final Object STONE = getById("stone");
    public static final Object STONE$defaultState = FastNMS.INSTANCE.method$Block$defaultState(STONE);
    public static final Object FIRE = getById("fire");
    public static final Object SOUL_FIRE = getById("soul_fire");
    public static final Object ICE = getById("ice");
    public static final Object SHORT_GRASS = getById(VersionHelper.isOrAbove1_20_3() ? "short_grass" : "grass");
    public static final Object SHORT_GRASS$defaultState = FastNMS.INSTANCE.method$Block$defaultState(SHORT_GRASS);
    public static final Object SHULKER_BOX = getById("shulker_box");
    public static final Object COMPOSTER = getById("composter");
    public static final Object SNOW = getById("snow");
    public static final Object WATER = getById("water");
    public static final Object WATER$defaultState = FastNMS.INSTANCE.method$Block$defaultState(WATER);
    public static final Object TNT = getById("tnt");
    public static final Object TNT$defaultState = FastNMS.INSTANCE.method$Block$defaultState(TNT);
    public static final Object BARRIER = getById("barrier");
    public static final Object CARVED_PUMPKIN = getById("carved_pumpkin");
    public static final Object JACK_O_LANTERN = getById("jack_o_lantern");
    public static final Object MELON = getById("melon");
    public static final Object PUMPKIN = getById("pumpkin");
    public static final Object FARMLAND = getById("farmland");
    public static final Object LODESTONE = getById("lodestone");
    public static final Object BEDROCK = getById("bedrock");
    public static final Object OBSIDIAN = getById("obsidian");
    public static final Object END_PORTAL_FRAME = getById("end_portal_frame");

    private static Object getById(String id) {
        Object rl = FastNMS.INSTANCE.method$ResourceLocation$fromNamespaceAndPath("minecraft", id);
        return FastNMS.INSTANCE.method$Registry$getValue(MBuiltInRegistries.BLOCK, rl);
    }
}
