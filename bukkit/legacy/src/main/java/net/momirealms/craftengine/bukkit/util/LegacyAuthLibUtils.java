package net.momirealms.craftengine.bukkit.util;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public final class LegacyAuthLibUtils {
    private LegacyAuthLibUtils() {}

    public static String getName(GameProfile profile) {
        return profile.getName();
    }

    public static UUID getId(GameProfile profile) {
        return profile.getId();
    }
}
